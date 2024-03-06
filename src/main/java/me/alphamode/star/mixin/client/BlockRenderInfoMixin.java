package me.alphamode.star.mixin.client;

import me.alphamode.star.extensions.BlockRenderInfoExtension;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderInfo.class)
public abstract class BlockRenderInfoMixin implements BlockRenderInfoExtension {
    @Shadow public BlockPos blockPos;
    @Shadow public BlockState blockState;
    @Shadow long seed;
    @Shadow boolean defaultAo;
    @Shadow RenderLayer defaultLayer;
    @Shadow boolean useAo;
    @Shadow private int cullCompletionFlags;
    @Shadow private int cullResultFlags;
    public FluidState star_fluidState;

    @Override
    public FluidState star_getFluidState() {
        return this.star_fluidState;
    }

    @Override
    public void star_prepareForFluid(BlockState blockState, FluidState fluidState, BlockPos blockPos) {
        this.blockPos = blockPos;
        this.blockState = blockState;
        // in the unlikely case seed actually matches this, we'll simply retrieve it more than one
        seed = -1L;
        useAo = MinecraftClient.isAmbientOcclusionEnabled();
        defaultAo = useAo && blockState.getLuminance() == 0;

        defaultLayer = RenderLayers.getFluidLayer(fluidState);
        this.star_fluidState = fluidState;

        cullCompletionFlags = 0;
        cullResultFlags = 0;
    }

    @Inject(method = "release", at = @At("TAIL"), remap = false)
    private void releaseFluid(CallbackInfo ci) {
        star_fluidState = null;
    }
}
