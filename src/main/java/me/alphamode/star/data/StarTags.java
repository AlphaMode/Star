package me.alphamode.star.data;

import me.alphamode.star.Star;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class StarTags {

    public static class Fluids {
        public static final TagKey<Fluid> UPSIDE_DOWN_FLUID = TagKey.of(RegistryKeys.FLUID, Star.getResource("upside_down_fluid"));
    }
}
