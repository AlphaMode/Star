package me.alphamode.star.world.block;

import me.alphamode.star.world.block.entity.TickableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BasicBlockEntityBlock<T extends BlockEntity> extends Block implements BlockEntityProvider {
    private final BlockEntityType<T> blockEntityType;

    public BasicBlockEntityBlock(Settings settings, BlockEntityType<T> blockEntityType) {
        super(settings);
        this.blockEntityType = blockEntityType;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType.instantiate(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if(type.instantiate(BlockPos.ORIGIN, Blocks.AIR.getDefaultState()) instanceof TickableBlockEntity)
            return ((world1, pos, state1, blockEntity) -> ((TickableBlockEntity)blockEntity).tick());
        return null;
    }
}
