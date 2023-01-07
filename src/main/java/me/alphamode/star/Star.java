package me.alphamode.star;

import me.alphamode.star.world.fluids.DirectionalFluid;
import me.alphamode.star.world.fluids.TestFluid;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("UnstableApiUsage")
public class Star implements ModInitializer {

    public static final String MOD_ID = "star";

    public static Identifier getResource(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static final DirectionalFluid STILL = Registry.register(Registry.FLUID, getResource("test_still"), new TestFluid.Still());
    public static final DirectionalFluid FLOWING = Registry.register(Registry.FLUID, getResource("test_flowing"), new TestFluid.Flowing());

    public static final Block FLUID = Registry.register(Registry.BLOCK, getResource("test_fluid"), new FluidBlock(STILL, FabricBlockSettings.copy(Blocks.WATER)));

    @Override
    public void onInitialize() {

    }
}
