package me.alphamode.star.extensions.indium;

import me.alphamode.star.client.models.FluidBakedModel;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface TerrainRenderContextExtension {
    void tessellateFluid(WorldSlice worldSlice, BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos origin, FluidBakedModel model, Vec3d modelOffset);
}
