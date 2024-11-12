package me.alphamode.star;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Star implements ModInitializer {

    public static final String MOD_ID = "star";

    public static Identifier getResource(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {

    }
}
