package me.alphamode.star.data;

import me.alphamode.star.Star;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class StarTags {

    public static class Fluids {
        public static final TagKey<Fluid> UPSIDE_DOWN_FLUID = TagKey.create(Registry.FLUID_REGISTRY, Star.getResource("upside_down_fluid"));
    }
}
