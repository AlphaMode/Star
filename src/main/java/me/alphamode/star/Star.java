package me.alphamode.star;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public class Star implements ModInitializer {

    public static final String MOD_ID = "star";

    public static Identifier getResource(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
    }
}
