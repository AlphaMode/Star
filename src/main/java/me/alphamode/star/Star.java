package me.alphamode.star;

import me.alphamode.star.transfer.FluidTank;
import me.alphamode.star.world.block.entity.FluidTankBlockEntity;
import me.alphamode.star.world.block.entity.Transferable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("UnstableApiUsage")
public class Star implements ModInitializer {

    public static final String MOD_ID = "star";

    @Override
    public void onInitialize() {
        RegistryEntryAddedCallback.event(Registry.BLOCK_ENTITY_TYPE).register((rawId, id, blockEntityType) -> {
            if(blockEntityType.instantiate(BlockPos.ORIGIN, Blocks.AIR.getDefaultState()) instanceof Transferable transferable) {
                if(transferable.getStorage(null) instanceof FluidTank)
                    FluidStorage.SIDED.registerForBlockEntity(((blockEntity, direction) -> {
                        FluidTankBlockEntity fluidTankBlockEntity = (FluidTankBlockEntity) blockEntity;
                        return fluidTankBlockEntity.getStorage(direction);
                    }), blockEntityType);
                else if(transferable.getStorage(null) instanceof InventoryStorage)
                    ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> transferable.getStorage(direction), blockEntityType);
            }
        });
    }
}
