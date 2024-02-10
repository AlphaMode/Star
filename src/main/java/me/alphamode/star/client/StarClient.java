package me.alphamode.star.client;

import io.vram.frex.api.model.fluid.FluidModel;
import me.alphamode.star.Star;
import me.alphamode.star.client.models.*;
import me.alphamode.star.client.renderers.FluidModelRenderer;
import me.alphamode.star.client.renderers.UpsideDownFluidRenderer;
import me.alphamode.star.world.fluids.DirectionalFluid;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class StarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RegistryEntryAddedCallback.event(Registries.FLUID).register((rawId, id, fluid) -> {
            if(fluid instanceof DirectionalFluid directionalFluid && directionalFluid.useDefaultRenderer())
                FluidRenderHandlerRegistry.INSTANCE.register(fluid, new FluidModelRenderer(new NorthFluidModel()));
        });
        ModelSwapper.init();
        CTModelRegistry.init();
//        if (FabricLoader.getInstance().isModLoaded("frex")) {
//            FluidModel.registerFactory(fluid -> {
//                if (fluid instanceof DirectionalFluid directionalFluid && directionalFluid.getFlowDirection() == Direction.UP)
//                    return new FrexUpsideDownFluidModel();
//                return null;
//            }, Registries.FLUID.getId(Star.STILL));
//            FluidModel.registerFactory(fluid -> {
//                if (fluid instanceof DirectionalFluid directionalFluid && directionalFluid.getFlowDirection() == Direction.UP)
//                    return new FrexUpsideDownFluidModel();
//                return null;
//            }, Registries.FLUID.getId(Star.FLOWING));
//        }
    }
}
