package me.alphamode.star.client;

import me.alphamode.star.client.models.CTModelRegistry;
import me.alphamode.star.client.models.ModelSwapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class StarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelSwapper.init();
        CTModelRegistry.init();
    }
}
