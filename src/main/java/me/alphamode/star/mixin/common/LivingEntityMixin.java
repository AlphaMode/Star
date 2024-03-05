package me.alphamode.star.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.StarEntity;
import me.alphamode.star.world.fluids.StarFluid;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements StarEntity {
    @Shadow
    public abstract Vec3d applyFluidMovingSpeed(double d, boolean bl, Vec3d vec3d);

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow
    public abstract float getMovementSpeed();

    @Shadow
    protected abstract float getBaseMovementSpeedMultiplier();

    @Shadow
    protected abstract boolean shouldSwimInFluids();

    @Shadow
    public abstract boolean canWalkOnFluid(FluidState fluidState);

    @Shadow
    public abstract boolean isClimbing();

    @Shadow
    protected abstract void swimUpward(TagKey<Fluid> fluid);

    @Shadow
    private int jumpingCooldown;

    @Shadow
    protected abstract void jump();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "fall", at = @At("HEAD"))
    public void star$fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition, CallbackInfo ci) {
        if (!this.isTouchingStarFluid()) {
            this.checkStarFluidState();
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isTouchingWater()Z"))
    public void star$swim(CallbackInfo ci) {
        FluidState fluid = getStarTouchingFluid();
        if (fluid == null)
            return;
        double fluidHeight = this.getFluidHeight(StarTags.Fluids.STAR_FLUID);

        boolean touchingFluid = fluid.getFluid() instanceof StarFluid && fluidHeight > 0.0;
        double l = this.getSwimHeight();
        if (!touchingFluid || (fluidHeight > l)) {
            if (this.isOnGround() && !(fluidHeight > l)) {
                if ((this.isOnGround() || touchingFluid && fluidHeight <= l) && this.jumpingCooldown == 0) {
                    this.jump();
                    this.jumpingCooldown = 10;
                }
            }
            if (fluid.getFluid() instanceof StarFluid starFluid)
                starFluid.swim((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void star$fluidMovement(Vec3d movementInput, CallbackInfo ci, double gravity, boolean falling, @Share("star") LocalBooleanRef handled) {
        FluidState fluidState = getStarTouchingFluid();
        if (fluidState == null)
            return;
        if (fluidState.getFluid() instanceof StarFluid fluid && this.shouldSwimInFluids() && !fluid.canWalkOn((LivingEntity) (Object) this, movementInput, fluidState)) {
            handled.set(fluid.travelInFluid((LivingEntity) (Object) this, movementInput, gravity, falling));
        }
    }

    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyMovementInput(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d starFluidTravel(LivingEntity instance, Vec3d movementInput, float slipperiness, Operation<Vec3d> original, @Share("star") LocalBooleanRef handled) {
        if (handled.get())
            return Vec3d.ZERO;
        return original.call(instance, movementInput, slipperiness);
    }

    @WrapWithCondition(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"))
    private boolean shouldCancelVelocityUpdate(LivingEntity instance, double x, double y, double z, @Share("star") LocalBooleanRef handled) {
        return !handled.get();
    }

    @ModifyReturnValue(method = "canBreatheInWater", at = @At("RETURN"))
    private boolean checkIfCustomFluidIsBreathable(boolean original) {
        var state = getStarTouchingFluid();
        if (state != null && state.getFluid() instanceof StarFluid fluid && fluid.canBreathe((LivingEntity) (Object) this))
            return true;
        return original;
    }

    @ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean starFluidBaseTick(boolean original) {
        return original || isSubmergedIn(StarTags.Fluids.STAR_FLUID);
    }

    @ModifyArg(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), index = 0)
    private ParticleEffect getSplashParticle(ParticleEffect parameters) {
        var touching = getStarTouchingFluid();
        if (touching != null && touching.getFluid() instanceof StarFluid starFluid)
            return starFluid.getBubbleParticle(this);
        return parameters;
    }
}
