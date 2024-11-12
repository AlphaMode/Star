package me.alphamode.star.test.client;

import me.alphamode.star.client.renderers.UpsideDownFluidRenderer;
import me.alphamode.star.test.StarTest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.render.RenderLayer;

public class StarTestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FluidRenderHandlerRegistry.INSTANCE.register(StarTest.STILL, StarTest.FLOWING, new UpsideDownFluidRenderer());
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), StarTest.STILL, StarTest.FLOWING);
//        ModelSwapper.swapBlockModel(Blocks.GRASS_BLOCK, bakedModel -> new UpsideDownFluidModel());
    }
}
