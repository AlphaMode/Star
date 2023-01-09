package me.alphamode.star.world.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FluidTankBlockEntity extends BlockEntity {

    protected FluidTank tank;
    private LazyOptional<FluidTank> tankOptional;

    public FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int initialCapacity) {
        this(type, pos, state, new FluidTank(initialCapacity));
    }

    public FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, FluidTank tank) {
        super(type, pos, state);
        this.tank = tank;
        this.tankOptional = LazyOptional.of(() -> this.tank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.tankOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return tankOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag nbt) {
        tank.readFromNBT(nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        tank.writeToNBT(nbt);
    }
}
