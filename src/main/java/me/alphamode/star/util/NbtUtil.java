package me.alphamode.star.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * {@link net.minecraft.nbt.NbtUtils}
 */
@Deprecated(forRemoval = true)
public class NbtUtil {
    public static BlockPos fromNbt(CompoundTag nbt) {
        return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
    }

    public static void toNbt(BlockPos pos, CompoundTag nbt) {
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
    }
}
