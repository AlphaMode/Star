package me.alphamode.star.mixin.client;

import me.alphamode.star.extensions.fabric.FluidRenderHandlerExtension;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FluidRenderHandler.class, remap = false)
public interface FluidRenderHandlerMixin extends FluidRenderHandlerExtension {
}