package me.alphamode.star.world.fluids;

import me.alphamode.star.mixin.EntityAccessor;
import me.alphamode.star.mixin.LivingEntityAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

public abstract class StarFluid extends DirectionalFluid {
    public StarFluid(Direction flowDirection) {
        super(flowDirection);
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    @Override
    protected boolean isInfinite(World world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected int getFlowSpeed(WorldView worldView) {
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView worldView) {
        return 1;
    }

    @Override
    public int getTickRate(WorldView worldView) {
        return 5;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }

    @Environment(EnvType.CLIENT)
    public int calculateSubmergedVisibility(PlayerEntity player, int currentVisibilityTicks) {
        int i = player.isSpectator() ? 10 : 1;
        return MathHelper.clamp(currentVisibilityTicks + i, 0, 600);
    }

    public void onSwimmingStart(Entity entity) {
        Entity passenger = entity.hasPassengers() && entity.getControllingPassenger() != null ? entity.getControllingPassenger() : entity;
        float f = passenger == entity ? 0.2F : 0.9F;
        Vec3d velocity = passenger.getVelocity();
        float g = Math.min(1.0F, (float)Math.sqrt(velocity.x * velocity.x * 0.2F + velocity.y * velocity.y + velocity.z * velocity.z * 0.2F) * f);
        if (g < 0.25F) {
            entity.playSound(getSplashSound(entity), g, 1.0F + (entity.random.nextFloat() - entity.random.nextFloat()) * 0.4F);
        } else {
            entity.playSound(getHighSpeedSplashSound(entity), g, 1.0F + (entity.random.nextFloat() - entity.random.nextFloat()) * 0.4F);
        }

        float h = (float)MathHelper.floor(entity.getY());

        for(int i = 0; (float)i < 1.0F + entity.getWidth() * 20.0F; ++i) {
            double d = (entity.random.nextDouble() * 2.0 - 1.0) * entity.getWidth();
            double e = (entity.random.nextDouble() * 2.0 - 1.0) * entity.getWidth();
            entity.getWorld()
                    .addParticle(getBubbleParticle(entity), entity.getX() + d, h + 1.0F, entity.getZ() + e, velocity.x, velocity.y - entity.random.nextDouble() * 0.2F, velocity.z);
        }

        for(int i = 0; (float)i < 1.0F + entity.getWidth() * 20.0F; ++i) {
            double d = (entity.random.nextDouble() * 2.0 - 1.0) * entity.getWidth();
            double e = (entity.random.nextDouble() * 2.0 - 1.0) * entity.getWidth();
            entity.getWorld().addParticle(getSplashParticle(entity), entity.getX() + d, h + 1.0F, entity.getZ() + e, velocity.x, velocity.y, velocity.z);
        }

        entity.emitGameEvent(GameEvent.SPLASH);
    }

    public void knockInFlowDirection(Entity entity) {
        entity.setVelocity(entity.getVelocity().add(0.0, -0.04F, 0.0));
    }

    public void travelInFluid(LivingEntity entity, Vec3d movementInput, double gravity, boolean falling) {
        double y = entity.getY();
        float movementSpeed = entity.isSprinting() ? 0.9F : ((LivingEntityAccessor) entity).callGetBaseMovementSpeedMultiplier();
        float yVelocity = 0.02F;
        float depthStrider = (float) EnchantmentHelper.getDepthStrider(entity);
        if (depthStrider > 3.0F) {
            depthStrider = 3.0F;
        }

        if (!entity.isOnGround()) {
            depthStrider *= 0.5F;
        }

        if (depthStrider > 0.0F) {
            movementSpeed += (0.54600006F - movementSpeed) * depthStrider / 3.0F;
            yVelocity += (entity.getMovementSpeed() - yVelocity) * depthStrider / 3.0F;
        }

        if (entity.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
            movementSpeed = 0.96F;
        }

        entity.updateVelocity(yVelocity, movementInput);
        entity.move(MovementType.SELF, entity.getVelocity());
        Vec3d velocity = entity.getVelocity();
        if (entity.horizontalCollision && entity.isClimbing()) {
            velocity = new Vec3d(velocity.x, 0.2, velocity.z);
        }

        entity.setVelocity(velocity.multiply(movementSpeed, 0.8F, movementSpeed));
        Vec3d fluidSpeed = entity.applyFluidMovingSpeed(gravity, falling, entity.getVelocity());
        entity.setVelocity(fluidSpeed);
        if (entity.horizontalCollision && entity.doesNotCollide(fluidSpeed.x, fluidSpeed.y + 0.6F - entity.getY() + y, fluidSpeed.z)) {
            entity.setVelocity(fluidSpeed.x, 0.3F, fluidSpeed.z);
        }
    }

    public void swim(LivingEntity entity) {
        entity.setVelocity(entity.getVelocity().add(0.0, 0.04F, 0.0));
    }

    public boolean canSwimIn(Entity entity) {
        return true;
    }

    public boolean canWalkOn(LivingEntity entity, Vec3d movementInput, FluidState state) {
        return entity.canWalkOnFluid(state);
    }

    public abstract ParticleEffect getBubbleParticle(Entity entity);

    public abstract ParticleEffect getSplashParticle(Entity entity);

    public double getMovementSpeed() {
        return Entity.SPEED_IN_WATER;
    }

    public SoundEvent getSplashSound(Entity entity) {
        return ((EntityAccessor) entity).callGetSplashSound();
    }

    public SoundEvent getHighSpeedSplashSound(Entity entity) {
        return ((EntityAccessor) entity).callGetHighSpeedSplashSound();
    }

    /**
     * See {@link LivingEntity#canBreatheInWater} for vanilla behavior.
     * @param entity The entity in the fluid
     * @return Returns if the entity can breathe in the current fluid
     */
    public boolean canBreathe(LivingEntity entity) {
        return true;
    }

    /**
     * See {@link ClientPlayerEntity#getUnderwaterVisibility()} for vanilla behavior.
     * @param player The local player
     * @return The current submerged visibility which ranges between 0.0 - 1.0
     */
    @Environment(EnvType.CLIENT)
    public float getSubmergedVisibility(ClientPlayerEntity player) {
        return 0.0f;
    }
}
