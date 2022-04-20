package me.alphamode.star.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FluidTank extends SingleVariantStorage<FluidVariant> {

    public static final String DEFAULT_KEY = "tank";

    private final long capacity;

    public FluidTank(long capacity) {
        this.capacity = capacity;
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return capacity;
    }

    public void toNbt(NbtCompound nbt, @Nullable String key) {
        NbtCompound tank = new NbtCompound();

        tank.put("variant", getResource().toNbt());
        tank.putLong("amount", getAmount());

        nbt.put(key != null ? key : DEFAULT_KEY, tank);
    }

    public void fromNbt(NbtCompound nbt, @Nullable String key) {
        NbtCompound tank = nbt.getCompound(key != null ? key : DEFAULT_KEY);

        variant = FluidVariant.fromNbt(tank);
        amount = tank.getLong("amount");
    }
}
