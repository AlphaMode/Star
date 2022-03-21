package me.alphamode.star.world.block.entity;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.math.Direction;

public interface Transferable<T> {

    Storage<T> getStorage(Direction direction);
}
