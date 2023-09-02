package me.alphamode.star.extensions.fabric;

import me.alphamode.star.client.models.FluidBakedModel;
import org.jetbrains.annotations.Nullable;

public interface FluidRenderHandlerExtension {
    @Nullable
    default FluidBakedModel getFluidModel() {
        return null;
    }
}
