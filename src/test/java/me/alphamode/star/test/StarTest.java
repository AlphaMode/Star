package me.alphamode.star.test;

import me.alphamode.star.world.block.StarFluidBlock;
import me.alphamode.star.world.fluids.DirectionalFluid;
import me.alphamode.star.test.fluids.TestFluid;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class StarTest implements ModInitializer {

    public static final String MOD_ID = "star_test";

    public static Identifier getResource(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static final DirectionalFluid STILL = Registry.register(Registries.FLUID, getResource("test_still"), new TestFluid.Still(Direction.NORTH));
    public static final DirectionalFluid FLOWING = Registry.register(Registries.FLUID, getResource("test_flowing"), new TestFluid.Flowing(Direction.NORTH));
    public static final DirectionalFluid STILL_UP = Registry.register(Registries.FLUID, getResource("test_still_up"), new TestFluid.Still(Direction.UP));
    public static final DirectionalFluid FLOWING_UP = Registry.register(Registries.FLUID, getResource("test_flowing_up"), new TestFluid.Flowing(Direction.UP));

    public static final DirectionalFluid STILL_NORMAL = Registry.register(Registries.FLUID, getResource("test_still_normal"), new TestFluid.Still(Direction.DOWN));
    public static final DirectionalFluid FLOWING_NORMAL = Registry.register(Registries.FLUID, getResource("test_flowing_normal"), new TestFluid.Flowing(Direction.DOWN));

    public static final Block FLUID = Registry.register(Registries.BLOCK, getResource("test_fluid"), new StarFluidBlock(STILL, FabricBlockSettings.copy(Blocks.WATER)));
    public static final Block FLUID_UP = Registry.register(Registries.BLOCK, getResource("test_fluid_up"), new StarFluidBlock(STILL_UP, FabricBlockSettings.copy(Blocks.WATER)));
    public static final Block FLUID_NORMAL = Registry.register(Registries.BLOCK, getResource("test_fluid_normal"), new StarFluidBlock(STILL_NORMAL, FabricBlockSettings.copy(Blocks.WATER)));

    @Override
    public void onInitialize() {

    }
}
