package me.alphamode.star.mixin.indium;

import link.infra.indium.Indium;
import link.infra.indium.other.AccessChunkRenderCacheLocal;
import link.infra.indium.renderer.render.TerrainRenderContext;
import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.extensions.indium.TerrainRenderContextExtension;
import me.jellysquid.mods.sodium.client.gl.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(ChunkRenderRebuildTask.class)
public class ChunkRenderRebuildTaskMixin {
    @Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/pipeline/FluidRenderer;render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/fluid/FluidState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;)Z"))
    private boolean onRenderFluid(FluidRenderer fluidRenderer, BlockRenderView world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkModelBuilder buffers, ChunkBuildContext buildContext, CancellationSource cancellationSource) {
        // We need to get the model with a bit more context than BlockRenderer has, so we do it here
        var blockState = world.getBlockState(pos);
        final BakedModel model = buildContext.cache.getBlockModels().getModel(blockState);
        if (!Indium.ALWAYS_TESSELLATE_INDIUM && ((FabricBakedModel) model).isVanillaAdapter() && !(model instanceof FluidBakedModel)) {
            return fluidRenderer.render(world, fluidState, pos, offset, buffers);
        } else {
            TerrainRenderContext context = ((AccessChunkRenderCacheLocal)buildContext.cache).indium$getTerrainRenderContext();
            Vec3d modelOffset = blockState.getModelOffset(world, pos);
            return ((TerrainRenderContextExtension)context).tessellateFluid(world.getBlockState(pos), fluidState, pos, offset, model, modelOffset);
        }
    }
}
