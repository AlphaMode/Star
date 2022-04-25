package me.alphamode.star.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

/**
 * {@link net.minecraft.nbt.NbtHelper}
 */
@Deprecated(forRemoval = true)
public class NbtUtil {
    public static BlockPos fromNbt(NbtCompound nbt) {
        return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
    }

    public static void toNbt(BlockPos pos, NbtCompound nbt) {
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
    }
}
