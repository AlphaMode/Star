package me.alphamode.star.client;

import me.alphamode.star.world.fluids.DirectionalFluid;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

// Helper class for rendering fluids based off of sodium's FluidRenderer class
public class StarFluidRenderer {
    protected static final float EPSILON = 0.0F;
    protected final BlockPos.Mutable scratchPos = new BlockPos.Mutable();
    protected final MutableFloat scratchHeight = new MutableFloat(0.0F);
    protected final MutableInt scratchSamples = new MutableInt();

    public float fluidHeight(BlockRenderView blockRenderView, DirectionalFluid fluid, BlockPos blockPos) {
        BlockState blockState = blockRenderView.getBlockState(blockPos);
        FluidState fluidState = blockState.getFluidState();
        if (fluid.matchesType(fluidState.getFluid())) {
            FluidState blockState2 = blockRenderView.getFluidState(blockPos.offset(fluid.getFlowDirection().getOpposite()));
            return fluid.matchesType(blockState2.getFluid()) ? 1.0F : fluidState.getHeight();
        } else {
            return !blockState.isSolid() ? 0.0F : -1.0F;
        }
    }

    protected boolean isFluidOccluded(BlockRenderView world, int x, int y, int z, Direction dir, Fluid fluid) {
        BlockPos pos = this.scratchPos.set(x, y, z);
        BlockState blockState = world.getBlockState(pos);
        BlockPos adjPos = this.scratchPos.set(x + dir.getOffsetX(), y + dir.getOffsetY(), z + dir.getOffsetZ());
        if (!blockState.isOpaque()) {
            return world.getFluidState(adjPos).getFluid().matchesType(fluid);
        } else {
            return world.getFluidState(adjPos).getFluid().matchesType(fluid) || blockState.isSideSolid(world, pos, dir, SideShapeType.FULL);
        }
    }

    protected boolean isSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height) {
        BlockPos pos = this.scratchPos.set(x + dir.getOffsetX(), y + dir.getOffsetY(), z + dir.getOffsetZ());
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOpaque()) {
            VoxelShape shape = blockState.getCullingShape(world, pos);
            if (shape == VoxelShapes.fullCube()) {
                return dir == Direction.DOWN;
            } else if (shape.isEmpty()) {
                return true;
            } else {
                VoxelShape threshold = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, height, 1.0);
                return !VoxelShapes.isSideCovered(threshold, shape, dir);
            }
        } else {
            return true;
        }
    }

    public float fluidCornerHeight(BlockRenderView world, DirectionalFluid fluid, float fluidHeight, float fluidHeightX, float fluidHeightY, BlockPos blockPos) {
        if (!(fluidHeightY >= 1.0F) && !(fluidHeightX >= 1.0F)) {
            float height;
            if (fluidHeightY > 0.0F || fluidHeightX > 0.0F) {
                height = fluidHeight(world, fluid, blockPos);
                if (height >= 1.0F) {
                    return 1.0F;
                }

                modifyHeight(this.scratchHeight, this.scratchSamples, height);
            }

            modifyHeight(this.scratchHeight, this.scratchSamples, fluidHeight);
            modifyHeight(this.scratchHeight, this.scratchSamples, fluidHeightY);
            modifyHeight(this.scratchHeight, this.scratchSamples, fluidHeightX);
            height = this.scratchHeight.floatValue() / (float)this.scratchSamples.intValue();
            this.scratchHeight.setValue(0.0F);
            this.scratchSamples.setValue(0);
            return height;
        } else {
            return 1.0F;
        }
    }

    private void modifyHeight(MutableFloat totalHeight, MutableInt samples, float target) {
        if (target >= 0.8F) {
            totalHeight.add(target * 10.0F);
            samples.add(10);
        } else if (target >= 0.0F) {
            totalHeight.add(target);
            samples.increment();
        }
    }
}
