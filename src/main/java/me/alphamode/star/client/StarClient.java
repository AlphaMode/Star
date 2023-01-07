package me.alphamode.star.client;

import me.alphamode.star.Star;
import me.alphamode.star.client.models.CTModelRegistry;
import me.alphamode.star.client.models.ModelSwapper;
import me.alphamode.star.client.renderers.UpsideDownFluidRenderer;
import me.alphamode.star.world.fluids.DirectionalFluid;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class StarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RegistryEntryAddedCallback.event(Registry.FLUID).register((rawId, id, fluid) -> {
            if(fluid instanceof DirectionalFluid directionalFluid && directionalFluid.useDefaultRenderer())
                FluidRenderHandlerRegistry.INSTANCE.register(fluid, new UpsideDownFluidRenderer());
        });
        ModelSwapper.init();
        CTModelRegistry.init();
        FluidRenderHandlerRegistry.INSTANCE.register(Star.STILL, Star.FLOWING, new UpsideDownFluidRenderer());
    }
}
