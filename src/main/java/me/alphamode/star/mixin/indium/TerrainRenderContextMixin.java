package me.alphamode.star.mixin.indium;

import link.infra.indium.renderer.aocalc.AoCalculator;
import link.infra.indium.renderer.render.ChunkRenderInfo;
import link.infra.indium.renderer.render.TerrainBlockRenderInfo;
import link.infra.indium.renderer.render.TerrainRenderContext;
import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.extensions.indium.BlockRenderInfoExtension;
import me.alphamode.star.extensions.indium.TerrainRenderContextExtension;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(TerrainRenderContext.class)
public class TerrainRenderContextMixin implements TerrainRenderContextExtension {
    @Shadow @Final private ChunkRenderInfo chunkInfo;

    @Shadow @Final private AoCalculator aoCalc;

    @Shadow private Vec3d modelOffset;

    @Shadow private Vec3i origin;

    @Shadow @Final private TerrainBlockRenderInfo blockInfo;

    @Override
    public boolean tessellateFluid(BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos origin, BakedModel model, Vec3d modelOffset) {
        this.origin = origin;
        this.modelOffset = modelOffset;

        try {
            ((ChunkRenderInfoAccessor)this.chunkInfo).setDidOutput(false);
            this.aoCalc.clear();
            ((BlockRenderInfoExtension)this.blockInfo).star_prepareForFluid(blockState, fluidState, blockPos, model.useAmbientOcclusion());
            ((FluidBakedModel)model).emitFluidQuads(this.blockInfo.blockView, this.blockInfo.blockState, ((BlockRenderInfoExtension) this.blockInfo).star_getFluidState(), this.blockInfo.blockPos, this.blockInfo.randomSupplier, (TerrainRenderContext) (Object) this);
        } catch (Throwable var9) {
            CrashReport crashReport = CrashReport.create(var9, "Tessellating liquid in world - Indium Renderer (Star)");
            CrashReportSection crashReportSection = crashReport.addElement("Block being tessellated");
            CrashReportSection.addBlockInfo(crashReportSection, ((ChunkRenderInfoAccessor)this.chunkInfo).getBlockView(), blockPos, blockState);
            throw new CrashException(crashReport);
        }

        return ((ChunkRenderInfoAccessor)this.chunkInfo).isDidOutput();
    }
}
