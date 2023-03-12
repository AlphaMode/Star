package me.alphamode.star.extensions.indium;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface TerrainRenderContextExtension {
    boolean tessellateFluid(BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos origin, BakedModel model, Vec3d modelOffset);
}
