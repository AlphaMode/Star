package me.alphamode.star.mixin.common;

import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.StarEntity;
import me.alphamode.star.world.fluids.StarFluid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public class EntityMixin implements StarEntity {

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

    @Shadow @Final private static double SPEED_IN_WATER;

    @Unique
    @Override
    public boolean isTouchingUpsideDownFluid() {
        return this.touchingFluid != null && this.touchingFluid.getFluid() instanceof StarFluid;
    }

    @Unique
    private boolean touchingUpsideDownFluid;

    @Unique
    private FluidState touchingFluid;

    @Unique
    @Override
    public void checkUpsideDownState() {
        if (this.getVehicle() instanceof BoatEntity) {
            this.touchingUpsideDownFluid = false;
        } else if (this.updateMovementInFluid(StarTags.Fluids.UPSIDE_DOWN_FLUID, SPEED_IN_WATER)) {
            if (!this.touchingUpsideDownFluid && !this.firstUpdate) {
                if (this.touchingFluid.getFluid() instanceof StarFluid starFluid)
                    starFluid.onSwimmingStart((Entity) (Object) this);
                else
                    this.onSwimmingStart();
            }

            this.onLanding();
            this.touchingUpsideDownFluid = true;
            this.extinguish();
        } else {
            this.touchingUpsideDownFluid = false;
        }
    }

    @Override
    public FluidState getTouchingFluid() {
        return this.touchingFluid;
    }

    @Inject(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void captureTouchingFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir, Box box, int i, int j, int k, int l, int m, int n, double d, boolean bl, boolean bl2, Vec3d vec3d, int o, BlockPos.Mutable mutable, int p, int q, int r, FluidState fluidState) {
        if (fluidState.isIn(tag))
            this.touchingFluid = fluidState;
    }

    @Inject(method = "updateMovementInFluid", at = @At("RETURN"))
    private void checkIfActuallyTouching(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue())
            this.touchingFluid = null;
    }

    @Inject(method = "updateSubmergedInWaterState", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;streamTags()Ljava/util/stream/Stream;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void captureTagsFluid(CallbackInfo ci, double d, Entity entity, BlockPos blockPos, FluidState fluidState, double e) {
        this.touchingFluid = fluidState;
    }

    @Inject(method = "updateWaterState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;checkWaterState()V"))
    public void star$upsideDownFluidCheck(CallbackInfoReturnable<Boolean> cir) {
        checkUpsideDownState();
    }

    @Inject(method = "updateWaterState", at = @At("RETURN"), cancellable = true)
    public void star$fixReturn(CallbackInfoReturnable<Boolean> cir) {
        if(!cir.getReturnValue() && isTouchingUpsideDownFluid())
            cir.setReturnValue(true);
    }
}
