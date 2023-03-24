package me.alphamode.star.client.models;

import me.alphamode.star.events.client.ModelBakeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
public class ModelSwapper {
    private static final HashMap<Block, Function<BakedModel, BakedModel>> registered = new HashMap<>();

    public static void init() {
        ModelBakeEvent.ON_MODEL_BAKE.register((bakedModelManager, existingModels, modelLoader) -> {
            registered.forEach((block, swapFunction) -> {
                getAllBlockStateModelLocations(block).forEach(modelId -> {
                    existingModels.put(modelId, swapFunction.apply(existingModels.get(modelId)));
                });
            });
        });
    }

    public static void swapBlockModel(Block block, Function<BakedModel, BakedModel> oldToNewFunction) {
        registered.put(block, oldToNewFunction);
    }

    public static List<ModelIdentifier> getAllBlockStateModelLocations(Block block) {
        List<ModelIdentifier> models = new ArrayList<>();
        Identifier blockRl = Registries.BLOCK.getId(block);
        block.getStateManager()
                .getStates()
                .forEach(state -> {
                    models.add(BlockModels.getModelId(blockRl, state));
                });
        return models;
    }
}
