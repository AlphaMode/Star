package me.alphamode.star.mixin.sodium;

import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadViewMutable;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
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

    @Invoker
    static FluidRenderHandler callGetFluidRenderHandler(FluidState fluidState) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    ModelQuadViewMutable getQuad();

    @Invoker
    boolean callIsFluidOccluded(BlockRenderView world, int x, int y, int z, Direction dir, Fluid fluid);

    @Invoker
    boolean callIsSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height);

    @Invoker
    void callUpdateQuad(ModelQuadView quad, WorldSlice world, BlockPos pos, LightPipeline lighter, Direction dir, float brightness, ColorProvider<FluidState> colorSampler, FluidState fluidState);

    @Invoker
    static void callSetVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v) {}

    @Accessor
    BlockPos.Mutable getScratchPos();

    @Accessor
    LightPipelineProvider getLighters();

    @Invoker
    void callWriteQuad(ChunkModelBuilder builder, Material material, BlockPos offset, ModelQuadView quad, ModelQuadFacing facing, boolean flip);

    @Invoker
    ColorProvider<FluidState> callGetColorProvider(Fluid fluid, FluidRenderHandler handler);
}
