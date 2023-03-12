package me.alphamode.star.mixin.client;

import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.extensions.indium.BlockRenderInfoExtension;
import me.alphamode.star.extensions.fabric.TerrainRenderContextExtension;
import me.alphamode.star.mixin.ChunkRenderInfoAccessor;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ChunkRenderInfo;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainBlockRenderInfo;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TerrainRenderContext.class)
public class TerrainRenderContextMixin implements TerrainRenderContextExtension {
    @Shadow @Final private AoCalculator aoCalc;

    @Shadow @Final private TerrainBlockRenderInfo blockInfo;

    @Shadow @Final private ChunkRenderInfo chunkInfo;

    @Override
    public boolean tessellateFluid(BlockState blockState, FluidState fluidState, BlockPos blockPos, BakedModel model, MatrixStack matrixStack) {
        ((AbstractRenderContextAccessor)this).setMatrix(matrixStack.peek().getPositionMatrix());
        ((AbstractRenderContextAccessor)this).setNormalMatrix(matrixStack.peek().getNormalMatrix());

        try {
            aoCalc.clear();
            ((BlockRenderInfoExtension)blockInfo).star_prepareForFluid(blockState, fluidState, blockPos, model.useAmbientOcclusion());
            ((FluidBakedModel) model).emitFluidQuads(blockInfo.blockView, blockInfo.blockState, ((BlockRenderInfoExtension)blockInfo).star_getFluidState(), blockInfo.blockPos, blockInfo.randomSupplier, (RenderContext) this);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Tessellating liquid in world - Indigo Renderer (Star)");
            CrashReportSection crashReportSection = crashReport.addElement("Block being tessellated");
            CrashReportSection.addBlockInfo(crashReportSection, ((ChunkRenderInfoAccessor)chunkInfo).getBlockView(), blockPos, blockState);
            throw new CrashException(crashReport);
        }

        // false because we've already marked the chunk as populated - caller doesn't need to
        return false;
    }
}
