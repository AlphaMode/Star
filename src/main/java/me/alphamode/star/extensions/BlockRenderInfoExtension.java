package me.alphamode.star.extensions;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

public interface BlockRenderInfoExtension {

    FluidState star_getFluidState();

    void star_prepareForFluid(BlockState blockState, FluidState fluidState, BlockPos blockPos);
}
