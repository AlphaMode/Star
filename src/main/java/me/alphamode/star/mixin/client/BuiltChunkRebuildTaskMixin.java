package me.alphamode.star.mixin.client;

import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.extensions.fabric.FluidRenderHandlerExtension;
import me.alphamode.star.extensions.fabric.TerrainRenderContextExtension;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRendererRegion;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkBuilder.BuiltChunk.RebuildTask.class)
public class BuiltChunkRebuildTaskMixin {
    private MatrixStack star_matrixStack;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderFluid(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void captureMatrixStack(float cameraX, float cameraY, float cameraZ, BlockBufferBuilderStorage blockBufferBuilderStorage, CallbackInfoReturnable<ChunkBuilder.BuiltChunk.RebuildTask.RenderData> cir, ChunkBuilder.BuiltChunk.RebuildTask.RenderData renderData, int i, BlockPos blockPos, BlockPos blockPos2, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, ChunkRendererRegion chunkRendererRegion, MatrixStack matrixStack) {
        this.star_matrixStack = matrixStack;
    }

//    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderFluid(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V"))
//    private void replaceFluidRenderer(BlockRenderManager renderManager, BlockPos blockPos, BlockRenderView blockView, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
//        var renderer = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getFluid());
//        final FluidBakedModel model = renderer != null ? ((FluidRenderHandlerExtension) renderer).getFluidModel() : null;
//
//        if (model != null) {
//            this.star_matrixStack.push();
//            this.star_matrixStack.translate(blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15);
//
//            ((TerrainRenderContextExtension) ((AccessChunkRendererRegion) blockView).fabric_getRenderer()).tessellateFluid(blockState, fluidState, blockPos, model, this.star_matrixStack);
//            this.star_matrixStack.pop();
//            this.star_matrixStack = null;
//            return;
//        }
//        this.star_matrixStack = null;
//
//        renderManager.renderFluid(blockPos, blockView, vertexConsumer, blockState, fluidState);
//    }
}
