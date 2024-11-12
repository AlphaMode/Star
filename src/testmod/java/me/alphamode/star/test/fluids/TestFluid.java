package me.alphamode.star.test.fluids;

import me.alphamode.star.test.StarTest;
import me.alphamode.star.world.fluids.StarFluid;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.Direction;

public abstract class TestFluid extends StarFluid {
    public TestFluid() {
        super(Direction.UP);
    }

    @Override
    public Fluid getFlowing() {
        return StarTest.FLOWING;
    }

    @Override
    public Fluid getStill() {
        return StarTest.STILL;
    }

    @Override
    public Item getBucketItem() {
        return Items.BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return StarTest.FLUID.getDefaultState().with(FluidBlock.LEVEL, FlowableFluid.getBlockStateLevel(state));
    }

    @Override
    public ParticleEffect getBubbleParticle(Entity entity) {
        return ParticleTypes.BUBBLE;
    }

    @Override
    public ParticleEffect getSplashParticle(Entity entity) {
        return ParticleTypes.SPLASH;
    }

    public static class Flowing extends TestFluid {
        public Flowing() {
        }

        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(FlowableFluid.LEVEL);
        }

        public int getLevel(FluidState state) {
            return state.get(FlowableFluid.LEVEL);
        }

        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends TestFluid {
        public Still() {
        }

        public int getLevel(FluidState state) {
            return 8;
        }

        public boolean isStill(FluidState state) {
            return true;
        }
    }
}
