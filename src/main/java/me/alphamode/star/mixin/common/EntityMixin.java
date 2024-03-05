package me.alphamode.star.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class EntityMixin implements StarEntity {

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

    @Shadow @Final
    public static double SPEED_IN_WATER;

    @Shadow public abstract boolean isSwimming();

    @Shadow public abstract void setSwimming(boolean swimming);

    @Shadow public abstract boolean isSprinting();

    @Shadow public abstract boolean hasVehicle();

    @Shadow public abstract World getWorld();

    @Shadow private BlockPos blockPos;

    @Shadow public abstract boolean isSubmergedIn(TagKey<Fluid> fluidTag);

    protected boolean star$submergedInStarFluid;

    @Unique
    @Override
    public boolean isTouchingStarFluid() {
        return this.touchingFluid != null && this.touchingFluid.getFluid() instanceof StarFluid;
    }

    @Override
    public boolean isSubmergedInStarFluid() {
        return this.star$submergedInStarFluid && this.isTouchingStarFluid();
    }

    @Override
    public boolean star$getSubmergedStarState() {
        return this.star$submergedInStarFluid;
    }

    @Unique
    private boolean touchingStarFluid;

    @Unique
    private FluidState touchingFluid;

    @Unique
    @Override
    public void checkStarFluidState() {
        if (this.getVehicle() instanceof BoatEntity) {
            this.touchingStarFluid = false;
        } else if (this.updateMovementInFluid(StarTags.Fluids.STAR_FLUID, SPEED_IN_WATER)) {
            if (!this.touchingStarFluid && !this.firstUpdate) {
                if (this.touchingFluid.getFluid() instanceof StarFluid starFluid)
                    starFluid.onSwimmingStart((Entity) (Object) this);
                else
                    this.onSwimmingStart();
            }

            this.onLanding();
            this.touchingStarFluid = true;
            this.extinguish();
        } else {
            this.touchingStarFluid = false;
        }
    }

    @Override
    public FluidState getStarTouchingFluid() {
        return this.touchingFluid;
    }

    @ModifyArg(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;multiply(D)Lnet/minecraft/util/math/Vec3d;", ordinal = 2))
    private double getStarSpeed(double value) {
        if (this.touchingFluid != null && this.touchingFluid.getFluid() instanceof StarFluid starFluid)
            return starFluid.getMovementSpeed();
        return value;
    }

    @Inject(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void captureTouchingFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir, Box box, int i, int j, int k, int l, int m, int n, double d, boolean bl, boolean bl2, Vec3d vec3d, int o, BlockPos.Mutable mutable, int p, int q, int r, FluidState fluidState) {
        this.touchingFluid = fluidState;
    }

    @Inject(method = "updateMovementInFluid", at = @At("RETURN"))
    private void checkIfActuallyTouching(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue())
            this.touchingFluid = null;
    }

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void canSwimInCustomFluid(CallbackInfo ci) {
        if (this.touchingFluid != null && this.touchingFluid.getFluid() instanceof StarFluid starFluid && starFluid.canSwimIn((Entity) (Object) this)) {
            if (this.isSwimming()) {
                this.setSwimming(this.isSprinting() && this.isTouchingStarFluid() && !this.hasVehicle());
            } else {
                this.setSwimming(this.isSprinting() && this.isSubmergedInStarFluid() && !this.hasVehicle() && this.getWorld().getFluidState(this.blockPos).isIn(StarTags.Fluids.STAR_FLUID));
            }
            ci.cancel();;
        }
    }

    @Inject(method = "updateSubmergedInWaterState", at = @At("HEAD"))
    private void setIsSubmergedInStarFluid(CallbackInfo ci) {
        this.star$submergedInStarFluid = this.isSubmergedIn(StarTags.Fluids.STAR_FLUID);
    }

    @Inject(method = "updateSubmergedInWaterState", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;streamTags()Ljava/util/stream/Stream;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void captureTagsFluid(CallbackInfo ci, double d, Entity entity, BlockPos blockPos, FluidState fluidState, double e) {
        this.touchingFluid = fluidState;
    }

    @Inject(method = "updateWaterState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;checkWaterState()V"))
    public void star$upsideDownFluidCheck(CallbackInfoReturnable<Boolean> cir) {
        checkStarFluidState();
    }

    @ModifyReturnValue(method = "updateWaterState", at = @At("RETURN"))
    public boolean star$fixReturn(boolean original) {
        return original || isTouchingStarFluid();
    }

    @ModifyReturnValue(method = "isTouchingWater", at = @At("RETURN"))
    public boolean star$touchingWater(boolean original) {
        return original || isTouchingStarFluid();
    }
}
