package me.alphamode.star.client.models;

import me.alphamode.star.Star;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Utility class to allow swapping vanilla block models
 * with modded ones.
 *
 * Example {@link CTModelRegistry}
 */
@Mod.EventBusSubscriber(modid = Star.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModelSwapper {
    private static final HashMap<Block, Function<BakedModel, BakedModel>> registered = new HashMap<>();

    @SubscribeEvent
    public static void onModelsBake(ModelEvent.BakingCompleted event) {
        registered.forEach((block, swapFunction) -> {
            getAllBlockStateModelLocations(block).forEach(modelId -> {
                event.getModels().put(modelId, swapFunction.apply(event.getModels().get(modelId)));
            });
        });
    }

    public static void swapBlockModel(Block block, Function<BakedModel, BakedModel> oldToNewFunction) {
        registered.put(block, oldToNewFunction);
    }

    public static List<ModelResourceLocation> getAllBlockStateModelLocations(Block block) {
        List<ModelResourceLocation> models = new ArrayList<>();
        ResourceLocation blockRl = Registry.BLOCK.getKey(block);
        block.getStateDefinition()
                .getPossibleStates()
                .forEach(state -> {
                    models.add(BlockModelShaper.stateToModelLocation(blockRl, state));
                });
        return models;
    }
}
