package me.alphamode.star.mixin.sodium;

import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadViewMutable;
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorSampler;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.pipeline.FluidRenderer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FluidRenderer.class)
public interface FluidRendererAccessor {
    @Accessor
    static float getEPSILON() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    ModelQuadViewMutable getQuad();

    @Invoker
    boolean callIsFluidOccluded(BlockRenderView world, int x, int y, int z, Direction dir, Fluid fluid);

    @Invoker
    boolean callIsSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height);

    @Invoker
    ColorSampler<FluidState> callCreateColorProviderAdapter(FluidRenderHandler handler);

    @Invoker
    void callCalculateQuadColors(ModelQuadView quad, BlockRenderView world, BlockPos pos, LightPipeline lighter, Direction dir, float brightness, ColorSampler<FluidState> colorSampler, FluidState fluidState);

    @Invoker
    int callWriteVertices(ChunkModelBuilder builder, BlockPos offset, ModelQuadView quad);

    @Invoker
    void callSetVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v);

    @Invoker
    float callFluidCornerHeight(BlockRenderView world, Fluid fluid, float fluidHeight, float fluidHeightX, float fluidHeightY, BlockPos blockPos);

    @Accessor
    BlockPos.Mutable getScratchPos();

    @Accessor
    LightPipelineProvider getLighters();
}
