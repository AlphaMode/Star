package me.alphamode.star.world.fluids;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.IceBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.EnumMap;
import java.util.Map;

// TODO: Rewrite (Currently a vanila copy of FlowableFluid modified to support semi directional flow)
public abstract class DirectionalFluid extends FlowableFluid {
    protected final Direction flowDirection;

    public DirectionalFluid(Direction flowDirection) {
        this.flowDirection = flowDirection;
    }

    public Direction getFlowDirection() {
        return flowDirection;
    }

    @Override
    public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
        double d = 0.0;
        double e = 0.0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction direction : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
            mutable.set(pos, direction);
            FluidState fluidState = world.getFluidState(mutable);
            if (!this.isEmptyOrThis(fluidState)) continue;
            float f = fluidState.getHeight();
            float g = 0.0f;
            if (f == 0.0f) {
                FluidState fluidState2;
                if (this.isEmptyOrThis(fluidState2 = world.getFluidState(mutable.move(flowDirection))) && (f = fluidState2.getHeight()) > 0.0f) {
                    g = state.getHeight() - (f - 0.8888889f);
                }
            } else if (f > 0.0f) {
                g = state.getHeight() - f;
            }
            if (g == 0.0f) continue;
            d += (float)direction.getOffsetX() * g;
            e += (float)direction.getOffsetZ() * g;
        }
        Vec3d vec3d = new Vec3d(d, 0.0, e);
        if (state.get(FALLING)) {
            for (Direction direction2 : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
                mutable.set(pos, direction2);
                if (!this.isFlowBlocked(world, mutable, direction2) && !this.isFlowBlocked(world, mutable.move(flowDirection.getOpposite()), direction2)) continue;
                vec3d = vec3d.normalize().add(0.0, -6.0, 0.0);
                break;
            }
        }
        return vec3d.normalize();
    }

    @Override
    protected boolean isFlowBlocked(BlockView world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid().matchesType(this)) {
            return false;
        }
        if (direction == flowDirection.getOpposite()) {
            return true;
        }
        return !(blockState.getBlock() instanceof IceBlock) && blockState.isSideSolidFullSquare(world, pos, direction);
    }

    @Override
    protected void tryFlow(World world, BlockPos fluidPos, FluidState state) {
        if (state.isEmpty()) {
            return;
        }
        BlockState blockState = world.getBlockState(fluidPos);
        BlockPos blockPos = fluidPos.offset(flowDirection);
        BlockState blockState2 = world.getBlockState(blockPos);
        FluidState fluidState = this.getUpdatedState(world, blockPos, blockState2);
        if (this.canFlow(world, fluidPos, blockState, flowDirection, blockPos, blockState2, world.getFluidState(blockPos), fluidState.getFluid())) {
            this.flow(world, blockPos, blockState2, flowDirection, fluidState);
            if (this.countNeighboringSources(world, fluidPos) >= 3) {
                this.flowToSides(world, fluidPos, state, blockState);
            }
        } else if (state.isStill() || !this.canFlowDownTo(world, fluidState.getFluid(), fluidPos, blockState, blockPos, blockState2)) {
            this.flowToSides(world, fluidPos, state, blockState);
        }
    }

    @Override
    protected FluidState getUpdatedState(World world, BlockPos pos, BlockState state) {
        BlockPos blockPos2;
        BlockState blockState3;
        FluidState fluidState3;
        int i = 0;
        int j = 0;
        for (Direction direction : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
            BlockPos blockPos = pos.offset(direction);
            BlockState blockState = world.getBlockState(blockPos);
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.getFluid().matchesType(this) || !this.receivesFlow(direction, world, pos, state, blockPos, blockState)) continue;
            if (fluidState.isStill()) {
                ++j;
            }
            i = Math.max(i, fluidState.getLevel());
        }
        if (this.isInfinite(world) && j >= 2) {
            BlockState blockState2 = world.getBlockState(pos.offset(flowDirection));
            FluidState fluidState2 = blockState2.getFluidState();
            if (blockState2.isSolid() || this.isMatchingAndStill(fluidState2)) {
                return this.getStill(false);
            }
        }
        if (!(fluidState3 = (blockState3 = world.getBlockState(blockPos2 = pos.offset(flowDirection.getOpposite()))).getFluidState()).isEmpty() && fluidState3.getFluid().matchesType(this) && this.receivesFlow(flowDirection.getOpposite(), world, pos, state, blockPos2, blockState3)) {
            return this.getFlowing(8, true);
        }
        int k = i - this.getLevelDecreasePerBlock(world);
        if (k <= 0) {
            return Fluids.EMPTY.getDefaultState();
        }
        return this.getFlowing(k, false);
    }

    @Override
    protected int getFlowSpeedBetween(WorldView world, BlockPos blockPos, int i, Direction direction, BlockState blockState, BlockPos blockPos2, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
        int j = 1000;
        for (Direction direction2 : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
            int k;
            if (direction2 == direction) continue;
            BlockPos blockPos3 = blockPos.offset(direction2);
            short s2 = FlowableFluid.packXZOffset(blockPos2, blockPos3);
            Pair pair = short2ObjectMap.computeIfAbsent(s2, s -> {
                BlockState blockState1 = world.getBlockState(blockPos3);
                return Pair.of(blockState1, blockState1.getFluidState());
            });
            BlockState blockState2 = (BlockState)pair.getFirst();
            FluidState fluidState = (FluidState)pair.getSecond();
            if (!this.canFlowThrough(world, this.getFlowing(), blockPos, blockState, direction2, blockPos3, blockState2, fluidState)) continue;
            boolean bl = short2BooleanMap.computeIfAbsent(s2, s -> {
                BlockPos blockPos4 = blockPos3.offset(flowDirection);
                BlockState blockState4 = world.getBlockState(blockPos4);
                return this.canFlowDownTo(world, this.getFlowing(), blockPos3, blockState2, blockPos4, blockState4);
            });
            if (bl) {
                return i;
            }
            if (i >= this.getFlowSpeed(world) || (k = this.getFlowSpeedBetween(world, blockPos3, i + 1, direction2.getOpposite(), blockState2, blockPos2, short2ObjectMap, short2BooleanMap)) >= j) continue;
            j = k;
        }
        return j;
    }

    @Override
    public boolean canFlowDownTo(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        if (!this.receivesFlow(flowDirection, world, pos, state, fromPos, fromState)) {
            return false;
        }
        if (fromState.getFluidState().getFluid().matchesType(this)) {
            return true;
        }
        return this.canFill(world, fromPos, fromState, fluid);
    }

    @Override
    public int countNeighboringSources(WorldView world, BlockPos pos) {
        int i = 0;
        for (Direction direction : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
            BlockPos blockPos = pos.offset(direction);
            FluidState fluidState = world.getFluidState(blockPos);
            if (!this.isMatchingAndStill(fluidState)) continue;
            ++i;
        }
        return i;
    }

    @Override
    protected Map<Direction, FluidState> getSpread(World world, BlockPos pos, BlockState state) {
        int i = 1000;
        EnumMap<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectOpenHashMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap<Pair<BlockState, FluidState>>();
        Short2BooleanOpenHashMap short2BooleanMap = new Short2BooleanOpenHashMap();
        for (Direction direction : Direction.Type.HORIZONTAL) { // TODO: Sideways fluid support
            BlockPos blockPos = pos.offset(direction);
            short s2 = FlowableFluid.packXZOffset(pos, blockPos);
            Pair pair = short2ObjectMap.computeIfAbsent(s2, s -> {
                BlockState blockState = world.getBlockState(blockPos);
                return Pair.of(blockState, blockState.getFluidState());
            });
            BlockState blockState = (BlockState)pair.getFirst();
            FluidState fluidState = (FluidState)pair.getSecond();
            FluidState fluidState2 = this.getUpdatedState(world, blockPos, blockState);
            if (!this.canFlowThrough(world, fluidState2.getFluid(), pos, state, direction, blockPos, blockState, fluidState)) continue;
            BlockPos blockPos2 = blockPos.offset(flowDirection);
            boolean bl = short2BooleanMap.computeIfAbsent(s2, s -> {
                BlockState blockState2 = world.getBlockState(blockPos2);
                return this.canFlowDownTo(world, this.getFlowing(), blockPos, blockState, blockPos2, blockState2);
            });
            int j = bl ? 0 : this.getFlowSpeedBetween(world, blockPos, 1, direction.getOpposite(), blockState, pos, short2ObjectMap, short2BooleanMap);
            if (j < i) {
                map.clear();
            }
            if (j > i) continue;
            map.put(direction, fluidState2);
            i = j;
        }
        return map;
    }

    public static boolean isFluidInDirectionEqual(FluidState state, BlockView world, BlockPos pos) {
        if(state.getFluid() instanceof DirectionalFluid directionalFluid)
            return state.getFluid().matchesType(world.getFluidState(pos.offset(directionalFluid.getFlowDirection())).getFluid());
        return state.getFluid().matchesType(world.getFluidState(pos.up()).getFluid());
    }

    @Override
    public float getHeight(FluidState state, BlockView world, BlockPos pos) {
        if (isFluidInDirectionEqual(state, world, pos)) {
            return 1.0f;
        }
        return state.getHeight();
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
        if (state.getLevel() == 9 && isFluidInDirectionEqual(state, world, pos)) {
            return VoxelShapes.fullCube();
        }
        return this.shapeCache.computeIfAbsent(state, fluidState -> VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, fluidState.getHeight(world, pos), 1.0));
    }

    @Environment(EnvType.CLIENT)
    public boolean useDefaultRenderer() {
        return true;
    }
}
