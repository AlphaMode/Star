package me.alphamode.star.mixin.sodium;

import me.alphamode.star.client.renderers.SodiumUpsideDownFluidRenderer;
import me.alphamode.star.client.renderers.UpsideDownFluidRenderer;
import me.alphamode.star.world.fluids.DirectionalFluid;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidRenderer.class, remap = false)
public abstract class FluidRendererMixin {
    @Unique
    private final SodiumUpsideDownFluidRenderer star$upsideDownFluidRenderer = new SodiumUpsideDownFluidRenderer((FluidRenderer) (Object) this);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void star$supportUpsideDownFluids(WorldSlice world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkBuildBuffers buffers, CallbackInfo ci) {
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getFluid());
        if (handler instanceof UpsideDownFluidRenderer renderer && ((DirectionalFluid) fluidState.getFluid()).getFlowDirection(world, fluidState, pos) != Direction.DOWN) {
            star$upsideDownFluidRenderer.renderUpsideDown(world, fluidState, pos, offset, buffers, renderer);
            ci.cancel();
        }
    }
}
