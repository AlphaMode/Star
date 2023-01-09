package me.alphamode.star.mixin.common;

import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.EntityExtension;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements EntityExtension {

    @Shadow
    public @Nullable Entity getVehicle() {
        return null;
    }

    @Shadow
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tag, double speed) {
        throw new RuntimeException();
    }

    @Shadow protected boolean firstTick;

    @Shadow
    protected void doWaterSplashEffect() {
    }

    @Shadow
    public void resetFallDistance() {

    }

    @Shadow
    public void clearFire() {
    }

    @Unique
    @Override
    public boolean isTouchingUpsideDownFluid() {
        return touchingUpsideDownFluid;
    }

    @Unique
    private boolean touchingUpsideDownFluid;

    @Unique
    @Override
    public void checkUpsideDownState() {
        if (this.getVehicle() instanceof Boat) {
            this.touchingUpsideDownFluid = false;
        } else if (this.updateFluidHeightAndDoFluidPushing(StarTags.Fluids.UPSIDE_DOWN_FLUID, -0.014)) {
            if (!this.touchingUpsideDownFluid && !this.firstTick) {
                this.doWaterSplashEffect();
            }

            this.resetFallDistance();
            this.touchingUpsideDownFluid = true;
            this.clearFire();
        } else {
            this.touchingUpsideDownFluid = false;
        }
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateInWaterStateAndDoWaterCurrentPushing()V"), cancellable = true)
    public void star$upsideDownFluidCheck(CallbackInfoReturnable<Boolean> cir) {
        checkUpsideDownState();
    }

    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("RETURN"), cancellable = true)
    public void star$fixReturn(CallbackInfoReturnable<Boolean> cir) {
        if(!cir.getReturnValue() && isTouchingUpsideDownFluid())
            cir.setReturnValue(true);
    }
}
