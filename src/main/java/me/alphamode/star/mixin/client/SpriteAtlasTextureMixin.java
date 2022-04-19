package me.alphamode.star.mixin.client;

import me.alphamode.star.events.client.UploadSpritesStitchCallback;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin {
    @Inject(method = "upload", at = @At("TAIL"))
    public void star$onUploadStitch(SpriteAtlasTexture.Data data, CallbackInfo ci) {
        UploadSpritesStitchCallback.STITCH.invoker().onSpritesStitch(data, (SpriteAtlasTexture) (Object) this);
    }
}
