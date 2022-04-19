package me.alphamode.star.events.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface ModelBakeEvent {

    Event<ModelBakeEvent> ON_MODEL_BAKE = EventFactory.createArrayBacked(ModelBakeEvent.class, callbacks -> (modelManager, existingModels, loader) -> {
        for(ModelBakeEvent e : callbacks)
            e.onModelBake(modelManager, existingModels, loader);
    });

    void onModelBake(BakedModelManager modelManager, Map<Identifier, BakedModel> existingModels, ModelLoader loader);
}
