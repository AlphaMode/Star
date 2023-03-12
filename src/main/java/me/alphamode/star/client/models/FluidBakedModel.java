package me.alphamode.star.client.models;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.function.Supplier;

public interface FluidBakedModel extends FabricBakedModel {
    void emitFluidQuads(BlockRenderView blockView, BlockState state, FluidState fluidState, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context);

    @Override
    default void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {}

    @Override
    default void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {}
}
