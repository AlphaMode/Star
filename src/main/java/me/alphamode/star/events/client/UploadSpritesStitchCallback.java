package me.alphamode.star.events.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;

public interface UploadSpritesStitchCallback {

    Event<UploadSpritesStitchCallback> STITCH = EventFactory.createArrayBacked(UploadSpritesStitchCallback.class, callbacks -> (data, atlasTexture) -> {
        for(UploadSpritesStitchCallback e : callbacks)
            e.onSpritesStitch(data, atlasTexture);
    });

    void onSpritesStitch(SpriteAtlasTexture.Data data, SpriteAtlasTexture atlasTexture);
}
