package me.alphamode.star.world.block;

import me.alphamode.star.world.block.entity.TickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BasicBlockEntityBlock<T extends BlockEntity> extends Block implements EntityBlock {
    private final BlockEntityType<T> blockEntityType;

    public BasicBlockEntityBlock(Properties settings, BlockEntityType<T> blockEntityType) {
        super(settings);
        this.blockEntityType = blockEntityType;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if(type.create(BlockPos.ZERO, Blocks.AIR.defaultBlockState()) instanceof TickableBlockEntity)
            return ((world1, pos, state1, blockEntity) -> ((TickableBlockEntity)blockEntity).tick());
        return null;
    }
}
