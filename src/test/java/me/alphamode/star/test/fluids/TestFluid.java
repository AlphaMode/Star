package me.alphamode.star.test.fluids;

import me.alphamode.star.Star;
import me.alphamode.star.test.StarTest;
import me.alphamode.star.world.fluids.StarFluid;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.Direction;

public abstract class TestFluid extends StarFluid {
    public TestFluid(Direction flowDirection) {
        super(flowDirection);
    }

    @Override
    public Fluid getFlowing() {
        return getFlowDirection() == Direction.NORTH ? StarTest.FLOWING : getFlowDirection() == Direction.DOWN ? StarTest.FLOWING_NORMAL : StarTest.FLOWING_UP;
    }

    @Override
    public Fluid getStill() {
        return getFlowDirection() == Direction.NORTH ? StarTest.STILL : getFlowDirection() == Direction.DOWN ? StarTest.STILL_NORMAL : StarTest.STILL_UP;
    }

    @Override
    public Item getBucketItem() {
        return Items.BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return getFlowDirection() == Direction.NORTH ? StarTest.FLUID.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state)) : getFlowDirection() == Direction.DOWN ? StarTest.FLUID_NORMAL.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state)) : StarTest.FLUID_UP.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
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
        public Flowing(Direction flowDirection) {
            super(flowDirection);
        }

        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends TestFluid {
        public Still(Direction flowDirection) {
            super(flowDirection);
        }

        public int getLevel(FluidState state) {
            return 8;
        }

        public boolean isStill(FluidState state) {
            return true;
        }
    }
}
