package me.alphamode.star.extensions.fabric;

import me.alphamode.star.client.models.FluidBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

public interface TerrainRenderContextExtension {
    boolean tessellateFluid(BlockState blockState, FluidState fluidState, BlockPos blockPos, final FluidBakedModel model, MatrixStack matrixStack);
}
