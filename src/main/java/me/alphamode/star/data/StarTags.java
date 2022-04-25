package me.alphamode.star.data;

import me.alphamode.star.Star;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class StarTags {


    public static class Fluids {
        public static final TagKey<Fluid> UPSIDE_DOWN_FLUID = TagKey.of(Registry.FLUID_KEY, Star.getResource("upside_down_fluid"));
    }
}
