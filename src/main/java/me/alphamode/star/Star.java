package me.alphamode.star;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import me.alphamode.star.world.fluids.DirectionalFluid;
import me.alphamode.star.world.fluids.TestFluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@SuppressWarnings("UnstableApiUsage")
@Mod(Star.MOD_ID)
public class Star {

    public static final String MOD_ID = "star";

    public static ResourceLocation getResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static final Supplier<DirectionalFluid> STILL = Suppliers.memoize(TestFluid.Still::new);
    public static final Supplier<DirectionalFluid> FLOWING = Suppliers.memoize(TestFluid.Still::new);

    public static final Supplier<Block> FLUID = Suppliers.memoize(() -> new LiquidBlock(STILL.get(), BlockBehaviour.Properties.copy(Blocks.WATER)));

    public Star() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::registerTestFluids);
    }

    public void registerTestFluids(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.FLUIDS, getResource("test_still"), STILL::get);
        event.register(ForgeRegistries.Keys.FLUIDS, getResource("test_flowing"), FLOWING::get);
        event.register(ForgeRegistries.Keys.BLOCKS, getResource("test_fluid"), FLUID::get);
    }
}
