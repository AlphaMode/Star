package me.alphamode.star.world.fluids;

import me.alphamode.star.Star;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.Direction;

public abstract class TestFluid extends StarFluid {
    public TestFluid() {
        super(Direction.UP);
    }

    @Override
    public Fluid getFlowing() {
        return Star.FLOWING;
    }

    @Override
    public Fluid getStill() {
        return Star.STILL;
    }

    @Override
    public Item getBucketItem() {
        return Items.BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return Star.FLUID.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    public static class Flowing extends TestFluid {
        public Flowing() {
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
