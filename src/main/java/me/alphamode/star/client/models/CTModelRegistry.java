package me.alphamode.star.client.models;

import me.alphamode.star.events.client.ModelBakeEvent;
import me.alphamode.star.events.client.UploadSpritesStitchCallback;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTModelRegistry {

    private static final List<Block> registered = new ArrayList<>();
    private static final Map<Block, Sprite> cachedSpites = new HashMap<>();

    public static void init() {
        ModelBakeEvent.ON_MODEL_BAKE.register((bakedModelManager, map, modelLoader) -> {
            registered.forEach(block -> {
                getAllBlockStateModelLocations(block).forEach(modelId -> {
                    map.put(modelId, new ConnectedModel(map.get(modelId)));
                });
            });
        });
        UploadSpritesStitchCallback.STITCH.register((data, spriteAtlasTexture) -> {
            for(Block block : registered) {
                Identifier id = Registry.BLOCK.getId(block);
                cachedSpites.putIfAbsent(block, spriteAtlasTexture.getSprite(new Identifier(id.getNamespace(), "block/" + id.getPath() + "_connected")));
            }
        });
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            for(Block block : registered) {
                Identifier id = Registry.BLOCK.getId(block);
                registry.register(new Identifier(id.getNamespace(), "block/" + id.getPath() + "_connected"));
            }
        });
    }

    public static void registerCTModel(Block block) {
        registered.add(block);
    }

    public static Sprite getCTSprite(Block block) {
        return cachedSpites.get(block);
    }

    public static List<ModelIdentifier> getAllBlockStateModelLocations(Block block) {
        List<ModelIdentifier> models = new ArrayList<>();
        Identifier blockRl = Registry.BLOCK.getId(block);
        block.getStateManager()
                .getStates()
                .forEach(state -> {
                    models.add(BlockModels.getModelId(blockRl, state));
                });
        return models;
    }
}
