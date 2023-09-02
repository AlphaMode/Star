package me.alphamode.star.mixin.indium;

import link.infra.indium.renderer.render.AbstractBlockRenderContext;
import link.infra.indium.renderer.render.TerrainRenderContext;
import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.extensions.BlockRenderInfoExtension;
import me.alphamode.star.extensions.indium.TerrainRenderContextExtension;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(value = TerrainRenderContext.class, remap = false)
public abstract class TerrainRenderContextMixin extends AbstractBlockRenderContext implements TerrainRenderContextExtension {
    @Shadow private Vector3fc origin;

    @Shadow private Vec3d modelOffset;

    @Override
    public void tessellateFluid(WorldSlice world, BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos origin, FluidBakedModel model, Vec3d modelOffset) {
        try {
            this.origin = new Vector3f(origin.getX(), origin.getY(), origin.getZ());
            this.modelOffset = modelOffset;
            this.aoCalc.clear();
            ((BlockRenderInfoExtension)this.blockInfo).star_prepareForFluid(blockState, fluidState, blockPos);
            model.emitFluidQuads(this.blockInfo.blockView, this.blockInfo.blockState, ((BlockRenderInfoExtension) this.blockInfo).star_getFluidState(), this.blockInfo.blockPos, this.blockInfo.randomSupplier, (TerrainRenderContext) (Object) this);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Tessellating liquid in world - Indium Renderer (Star)");
            CrashReportSection crashReportSection = crashReport.addElement("Block being tessellated");
            CrashReportSection.addBlockInfo(crashReportSection, world, blockPos, blockState);
            throw new CrashException(crashReport);
        }
    }
}