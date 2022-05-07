package me.alphamode.star.mixin.common;

import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.EntityExtension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.TagKey;
import net.minecraft.world.World;
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
    public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        throw new RuntimeException();
    }

    @Shadow protected boolean firstUpdate;

    @Shadow
    protected void onSwimmingStart() {
    }

    @Shadow
    public void onLanding() {

    }

    @Shadow
    public void extinguish() {
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
        if (this.getVehicle() instanceof BoatEntity) {
            this.touchingUpsideDownFluid = false;
        } else if (this.updateMovementInFluid(StarTags.Fluids.UPSIDE_DOWN_FLUID, -0.014)) {
            if (!this.touchingUpsideDownFluid && !this.firstUpdate) {
                this.onSwimmingStart();
            }

            this.onLanding();
            this.touchingUpsideDownFluid = true;
            this.extinguish();
        } else {
            this.touchingUpsideDownFluid = false;
        }
    }

    @Inject(method = "updateWaterState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;checkWaterState()V"), cancellable = true)
    public void star$upsideDownFluidCheck(CallbackInfoReturnable<Boolean> cir) {
        checkUpsideDownState();
    }

    @Inject(method = "updateWaterState", at = @At("RETURN"), cancellable = true)
    public void star$fixReturn(CallbackInfoReturnable<Boolean> cir) {
        if(!cir.getReturnValue() && isTouchingUpsideDownFluid())
            cir.setReturnValue(true);
    }
}
