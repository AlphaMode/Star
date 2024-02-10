package me.alphamode.star.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements StarEntity {
    @Shadow public abstract Vec3d applyFluidMovingSpeed(double d, boolean bl, Vec3d vec3d);

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow public abstract float getMovementSpeed();

    @Shadow protected abstract float getBaseMovementSpeedMultiplier();

    @Shadow protected abstract boolean shouldSwimInFluids();

    @Shadow public abstract boolean canWalkOnFluid(FluidState fluidState);

    @Shadow public abstract boolean isClimbing();

    @Shadow protected abstract void swimUpward(TagKey<Fluid> fluid);

    @Shadow private int jumpingCooldown;

    @Shadow protected abstract void jump();

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
        FluidState fluid = getTouchingFluid();
        if (fluid == null)
            return;
        double fluidHeight = this.getFluidHeight(StarTags.Fluids.STAR_FLUID);

        boolean touchingFluid = fluid.getFluid() instanceof StarFluid && fluidHeight > 0.0;
        double l = this.getSwimHeight();
        if (touchingFluid || (fluidHeight > l)) {
            ((StarFluid)fluid.getFluid()).swim((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void star$fluidMovement(Vec3d movementInput, CallbackInfo ci, double gravity, boolean falling) {
        FluidState fluidState = getTouchingFluid();
        if (fluidState == null)
            return;
        if (fluidState.getFluid() instanceof StarFluid fluid && this.shouldSwimInFluids() && !fluid.canWalkOn((LivingEntity) (Object) this, movementInput, fluidState)) {
            fluid.travelInFluid((LivingEntity) (Object) this, movementInput, gravity, falling);
        }
    }

    @Inject(method = "canBreatheInWater", at = @At("RETURN"), cancellable = true)
    private void checkIfCustomFluidIsBreathable(CallbackInfoReturnable<Boolean> cir) {
        var state = getTouchingFluid();
        if (state != null && state.getFluid() instanceof StarFluid fluid && fluid.canBreathe((LivingEntity) (Object) this))
            cir.setReturnValue(true);
    }

    @ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean starFluidBaseTick(boolean original) {
        return original || isSubmergedIn(StarTags.Fluids.STAR_FLUID);
    }

    @ModifyArg(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), index = 0)
    private ParticleEffect getSplashParticle(ParticleEffect parameters) {
        var touching = getTouchingFluid();
        if (touching != null && touching.getFluid() instanceof StarFluid starFluid)
            return starFluid.getBubbleParticle(this);
        return parameters;
    }
}
