package me.alphamode.star.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.extensions.fabric.FluidRenderHandlerExtension;
import me.alphamode.star.extensions.fabric.TerrainRenderContextExtension;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRendererRegion;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionBuilder.class)
public class SectionBuilderMixin {

    @WrapOperation(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderFluid(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V"))
    private void replaceFluidRenderer(BlockRenderManager instance, BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, Operation<Void> original, @Local(index = 9) MatrixStack matrixStack) {
        final FluidBakedModel model = ((FluidRenderHandlerExtension) FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getFluid())).getFluidModel();

        if (model != null) {
            matrixStack.push();
            matrixStack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

            ((TerrainRenderContextExtension) ((AccessChunkRendererRegion) world).fabric_getRenderer()).tessellateFluid(blockState, fluidState, pos, model, matrixStack);
            matrixStack.pop();
            return;
        }

        original.call(instance, pos, world, vertexConsumer, blockState, fluidState);
    }
}
