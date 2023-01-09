package me.alphamode.star.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.alphamode.star.client.renderers.UpsideDownFluidRenderer;
import me.alphamode.star.world.fluids.DirectionalFluid;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlockRenderer.class)
public class FluidRendererMixin {
    private UpsideDownFluidRenderer star$fluidRenderer = new UpsideDownFluidRenderer();
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void star$renderUpsidedownFluid(BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (fluidState.getType() instanceof DirectionalFluid directionalFluid && directionalFluid.useDefaultRenderer() && directionalFluid.getFlowDirection() == Direction.UP)
            star$fluidRenderer.renderFluid(pos, world, vertexConsumer, blockState, fluidState);
    }
}
