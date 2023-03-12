package me.alphamode.star.mixin.frex;

import io.vram.frex.api.buffer.QuadSink;
import io.vram.frex.api.model.BlockModel;
import io.vram.frex.api.model.fluid.FluidModel;
import io.vram.frex.api.model.fluid.SimpleFluidModel;
import io.vram.frex.fabric.compat.FabricContextWrapper;
import me.alphamode.star.client.models.FluidBakedModel;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = SimpleFluidModel.class, remap = false)
public class FluidBakedModelMixin {
    @Inject(method = "renderAsBlock", at = @At("HEAD"), cancellable = true)
    private void starFluidRendering(BlockModel.BlockInputContext input, QuadSink output, CallbackInfo ci) {
        if (MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(input.blockState()) instanceof FluidBakedModel fluidBakedModel) {
            fluidBakedModel.emitFluidQuads(input.blockView(), input.blockState(), input.blockState().getFluidState(), input.pos(), input::random, FabricContextWrapper.wrap(input, output));
            ci.cancel();
        }
    }
}
