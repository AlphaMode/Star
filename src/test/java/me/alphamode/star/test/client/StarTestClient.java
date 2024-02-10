package me.alphamode.star.test.client;

import me.alphamode.star.Star;
import me.alphamode.star.client.models.NorthFluidModel;
import me.alphamode.star.client.models.UpsideDownFluidModel;
import me.alphamode.star.client.renderers.FluidModelRenderer;
import me.alphamode.star.test.StarTest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.render.RenderLayer;

public class StarTestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FluidRenderHandlerRegistry.INSTANCE.register(StarTest.STILL, StarTest.FLOWING, new FluidModelRenderer(new NorthFluidModel()));
        FluidRenderHandlerRegistry.INSTANCE.register(StarTest.STILL_UP, StarTest.FLOWING_UP, new FluidModelRenderer(new UpsideDownFluidModel()));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), StarTest.STILL, StarTest.FLOWING);
    }
}
