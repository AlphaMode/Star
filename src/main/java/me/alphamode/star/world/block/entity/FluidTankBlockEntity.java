package me.alphamode.star.world.block.entity;

import me.alphamode.star.transfer.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@SuppressWarnings("UnstableApiUsage")
public class FluidTankBlockEntity extends BlockEntity implements Transferable<FluidVariant> {

    protected FluidTank tank;

    public FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long initialCapacity) {
        this(type, pos, state, new FluidTank(initialCapacity));
    }

    public FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, FluidTank tank) {
        super(type, pos, state);
        this.tank = tank;
    }

    @Override
    public Storage<FluidVariant> getStorage(Direction direction) {
        return tank;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        tank.variant = FluidVariant.fromNbt(nbt.getCompound("variant"));
        tank.amount = nbt.getLong("amount");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.put("variant", tank.getResource().toNbt());
        nbt.putLong("amount", tank.getAmount());
    }
}
