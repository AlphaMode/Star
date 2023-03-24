package me.alphamode.star.mixin.common;

import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.EntityExtension;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements EntityExtension {
    @Shadow public abstract Vec3d applyFluidMovingSpeed(double d, boolean bl, Vec3d vec3d);

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow public abstract float getMovementSpeed();

    @Shadow protected abstract float getBaseMovementSpeedMultiplier();

    @Shadow protected abstract boolean shouldSwimInFluids();

    @Shadow public abstract boolean canWalkOnFluid(FluidState fluidState);

    @Shadow public abstract boolean isClimbing();

    @Shadow protected abstract void swimUpward(TagKey<Fluid> fluid);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "fall", at = @At("HEAD"))
    public void star$fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition, CallbackInfo ci) {
        if (!this.isTouchingUpsideDownFluid()) {
            this.checkUpsideDownState();
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;swimUpward(Lnet/minecraft/registry/tag/TagKey;)V", ordinal = 0))
    public void star$swim(CallbackInfo ci) {
        swimUpward(StarTags.Fluids.UPSIDE_DOWN_FLUID);
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void star$fluidMovement(Vec3d movementInput, CallbackInfo ci, double d, boolean bl) {
        FluidState fluidState = this.world.getFluidState(this.getBlockPos());
        if (this.isTouchingUpsideDownFluid() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState)) {
            double entityY = this.getY();
            float f = this.isSprinting() ? 0.9f : this.getBaseMovementSpeedMultiplier();
            float speed = 0.02f;
            float h = EnchantmentHelper.getDepthStrider((LivingEntity) (Object) this);
            if (h > 3.0f) {
                h = 3.0f;
            }
            if (!this.onGround) {
                h *= 0.5f;
            }
            if (h > 0.0f) {
                f += (0.54600006f - f) * h / 3.0f;
                speed += (this.getMovementSpeed() - speed) * h / 3.0f;
            }
            if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
                f = 0.96f;
            }
            this.updateVelocity(speed, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            Vec3d vec3d = this.getVelocity();
            if (this.horizontalCollision && this.isClimbing()) {
                vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
            }
            this.setVelocity(vec3d.multiply(f, 0.8f, f));
            Vec3d vec3d2 = this.applyFluidMovingSpeed(d, bl, this.getVelocity());
            this.setVelocity(vec3d2);
            if (this.horizontalCollision && this.doesNotCollide(vec3d2.x, vec3d2.y + (double)0.6f - this.getY() + entityY, vec3d2.z)) {
                this.setVelocity(vec3d2.x, 0.3f, vec3d2.z);
            }
        }
    }
}
