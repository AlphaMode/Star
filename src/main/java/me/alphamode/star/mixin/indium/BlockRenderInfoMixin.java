package me.alphamode.star.mixin.indium;

import link.infra.indium.renderer.render.BlockRenderInfo;
import me.alphamode.star.extensions.indium.BlockRenderInfoExtension;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(BlockRenderInfo.class)
public class BlockRenderInfoMixin implements BlockRenderInfoExtension {
    @Shadow public BlockPos blockPos;
    @Shadow public BlockState blockState;
    @Shadow public long seed;
    @Shadow
    boolean defaultAo;
    @Shadow
    RenderLayer defaultLayer;
    public FluidState star_fluidState;

    @Override
    public FluidState star_getFluidState() {
        return this.star_fluidState;
    }

    @Override
    public void star_prepareForFluid(BlockState blockState, FluidState fluidState, BlockPos blockPos, boolean modelAO) {
        this.blockPos = blockPos;
        this.blockState = blockState;
        // in the unlikely case seed actually matches this, we'll simply retrieve it more than one
        seed = -1L;
        defaultAo = modelAO && MinecraftClient.isAmbientOcclusionEnabled() && blockState.getLuminance() == 0;

        defaultLayer = RenderLayers.getFluidLayer(fluidState);
        this.star_fluidState = fluidState;
    }
}
