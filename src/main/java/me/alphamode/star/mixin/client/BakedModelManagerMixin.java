package me.alphamode.star.mixin.client;

import com.google.common.collect.Multimap;
import me.alphamode.star.events.client.ModelBakeEvent;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
    @Inject(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/DefaultedRegistry;iterator()Ljava/util/Iterator;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void star$modelLoad(Profiler profiler, Map<Identifier, SpriteAtlasManager.AtlasPreparation> preparations, ModelLoader modelLoader, CallbackInfoReturnable<Object> cir, Multimap multimap, Map<Identifier, BakedModel> models) {
        ModelBakeEvent.ON_MODEL_BAKE.invoker().onModelBake((BakedModelManager) (Object) this, models, modelLoader);
    }
}
