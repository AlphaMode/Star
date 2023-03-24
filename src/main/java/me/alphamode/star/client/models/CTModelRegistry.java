package me.alphamode.star.client.models;

import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A model registry to allow easy creation of connected textures models using a CTM format
 */
public class CTModelRegistry {

    private static final List<Block> registered = new ArrayList<>();
    private static final Map<Block, Sprite> cachedSpites = new HashMap<>();

    public static void init() {
//        UploadSpritesStitchCallback.STITCH.register((data, spriteAtlasTexture) -> {
//            for(Block block : registered) {
//                Identifier id = Registries.BLOCK.getId(block);
//                cachedSpites.putIfAbsent(block, spriteAtlasTexture.getSprite(new Identifier(id.getNamespace(), "block/" + id.getPath() + "_connected")));
//            }
//        });
    }

    public static void registerCTModel(Block block) {
        ModelSwapper.swapBlockModel(block, ConnectedModel::new);
        registered.add(block);
    }

    public static Sprite getCTSprite(Block block) {
        return cachedSpites.get(block);
    }

    /**
     * Deprecated
     * Moved to {@link ModelSwapper#getAllBlockStateModelLocations(Block)}
     */
    @Deprecated(forRemoval = true)
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
