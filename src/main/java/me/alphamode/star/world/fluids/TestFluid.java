package me.alphamode.star.world.fluids;

import me.alphamode.star.Star;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;

public abstract class TestFluid extends StarFluid {
    public TestFluid() {
        super(Direction.UP);
    }

    @Override
    public Fluid getFlowing() {
        return Star.FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return Star.STILL.get();
    }

    @Override
    public Item getBucket() {
        return Items.BUCKET;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return Star.FLUID.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public FluidType getFluidType() {
        return new FluidType(FluidType.Properties.create());
    }

    public static class Flowing extends TestFluid {
        public Flowing() {
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Still extends TestFluid {
        public Still() {
        }

        public int getAmount(FluidState state) {
            return 8;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
