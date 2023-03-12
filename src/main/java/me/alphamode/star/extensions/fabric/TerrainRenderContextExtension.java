package me.alphamode.star.extensions.fabric;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

public interface TerrainRenderContextExtension {
    boolean tessellateFluid(BlockState blockState, FluidState fluidState, BlockPos blockPos, final BakedModel model, MatrixStack matrixStack);
}
