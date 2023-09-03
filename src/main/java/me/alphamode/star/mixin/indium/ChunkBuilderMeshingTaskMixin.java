package me.alphamode.star.mixin.indium;

import link.infra.indium.Indium;
import link.infra.indium.renderer.render.TerrainRenderContext;
import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.extensions.fabric.FluidRenderHandlerExtension;
import me.alphamode.star.extensions.indium.TerrainRenderContextExtension;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public abstract class ChunkBuilderMeshingTaskMixin extends ChunkBuilderTask<ChunkBuildOutput> {
    @Redirect(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/FluidRenderer;render(Lme/jellysquid/mods/sodium/client/world/WorldSlice;Lnet/minecraft/fluid/FluidState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;)V", remap = true), remap = true)
    public void onRenderBlock(FluidRenderer fluidRenderer, WorldSlice worldSlice, FluidState fluidState, BlockPos pos, BlockPos modelOffset, ChunkBuildBuffers buffers, ChunkBuildContext buildContext) {
        // We need to get the model with a bit more context than BlockRenderer has, so we do it here
        var blockState = worldSlice.getBlockState(pos);
        final FluidBakedModel model = ((FluidRenderHandlerExtension) FluidRenderHandlerRegistry.INSTANCE.get(worldSlice.getFluidState(pos).getFluid())).getFluidModel();
        if (Indium.ALWAYS_TESSELLATE_INDIUM || model != null) {
            ((TerrainRenderContextExtension) TerrainRenderContext.get(buildContext)).tessellateFluid(worldSlice, blockState, fluidState, pos, modelOffset, model, blockState.getModelOffset(worldSlice, pos));
        } else {
            fluidRenderer.render(worldSlice, fluidState, pos, modelOffset, buffers);
        }
    }
}