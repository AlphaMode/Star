package me.alphamode.star.world.fluids;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.Map;

// TODO: Rewrite (Currently a vanila copy of FlowableFluid modified to support semi directional flow)
public abstract class DirectionalFluid extends FlowingFluid {
    protected final Direction flowDirection;

    public DirectionalFluid(Direction flowDirection) {
        this.flowDirection = flowDirection;
    }

    public Direction getFlowDirection() {
        return flowDirection;
    }

    @Override
    public Vec3 getFlow(BlockGetter world, BlockPos pos, FluidState state) {
        double d = 0.0;
        double e = 0.0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) { // TODO: Sideways fluid support
            mutable.setWithOffset(pos, direction);
            FluidState fluidState = world.getFluidState(mutable);
            if (!this.affectsFlow(fluidState)) continue;
            float f = fluidState.getOwnHeight();
            float g = 0.0f;
            if (f == 0.0f) {
                FluidState fluidState2;
                if (!world.getBlockState(mutable).getMaterial().blocksMotion() && this.affectsFlow(fluidState2 = world.getFluidState(mutable.move(flowDirection))) && (f = fluidState2.getOwnHeight()) > 0.0f) {
                    g = state.getOwnHeight() - (f - 0.8888889f);
                }
            } else if (f > 0.0f) {
                g = state.getOwnHeight() - f;
            }
            if (g == 0.0f) continue;
            d += (float)direction.getStepX() * g;
            e += (float)direction.getStepZ() * g;
        }
        Vec3 vec3d = new Vec3(d, 0.0, e);
        if (state.getValue(FALLING).booleanValue()) {
            for (Direction direction2 : Direction.Plane.HORIZONTAL) { // TODO: Sideways fluid support
                mutable.setWithOffset(pos, direction2);
                if (!this.isSolidFace(world, mutable, direction2) && !this.isSolidFace(world, mutable.move(flowDirection.getOpposite()), direction2)) continue;
                vec3d = vec3d.normalize().add(0.0, -6.0, 0.0);
                break;
            }
        }
        return vec3d.normalize();
    }

    @Override
    protected boolean isSolidFace(BlockGetter world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getType().isSame(this)) {
            return false;
        }
        if (direction == flowDirection.getOpposite()) {
            return true;
        }
        if (blockState.getMaterial() == Material.ICE) {
            return false;
        }
        return blockState.isFaceSturdy(world, pos, direction);
    }

    @Override
    protected void spread(LevelAccessor world, BlockPos fluidPos, FluidState state) {
        if (state.isEmpty()) {
            return;
        }
        BlockState blockState = world.getBlockState(fluidPos);
        BlockPos blockPos = fluidPos.relative(flowDirection);
        BlockState blockState2 = world.getBlockState(blockPos);
        FluidState fluidState = this.getNewLiquid(world, blockPos, blockState2);
        if (this.canSpreadTo(world, fluidPos, blockState, flowDirection, blockPos, blockState2, world.getFluidState(blockPos), fluidState.getType())) {
            this.spreadTo(world, blockPos, blockState2, flowDirection, fluidState);
            if (this.sourceNeighborCount(world, fluidPos) >= 3) {
                this.spreadToSides(world, fluidPos, state, blockState);
            }
        } else if (state.isSource() || !this.isWaterHole(world, fluidState.getType(), fluidPos, blockState, blockPos, blockState2)) {
            this.spreadToSides(world, fluidPos, state, blockState);
        }
    }

    @Override
    protected FluidState getNewLiquid(LevelReader world, BlockPos pos, BlockState state) {
        BlockPos blockPos2;
        BlockState blockState3;
        FluidState fluidState3;
        int i = 0;
        int j = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) { // TODO: Sideways fluid support
            BlockPos blockPos = pos.relative(direction);
            BlockState blockState = world.getBlockState(blockPos);
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.getType().isSame(this) || !this.canPassThroughWall(direction, world, pos, state, blockPos, blockState)) continue;
            if (fluidState.isSource()) {
                ++j;
            }
            i = Math.max(i, fluidState.getAmount());
        }
        if (this.canConvertToSource() && j >= 2) {
            BlockState blockState2 = world.getBlockState(pos.relative(flowDirection));
            FluidState fluidState2 = blockState2.getFluidState();
            if (blockState2.getMaterial().isSolid() || this.isSourceBlockOfThisType(fluidState2)) {
                return this.getSource(false);
            }
        }
        if (!(fluidState3 = (blockState3 = world.getBlockState(blockPos2 = pos.relative(flowDirection.getOpposite()))).getFluidState()).isEmpty() && fluidState3.getType().isSame(this) && this.canPassThroughWall(flowDirection.getOpposite(), world, pos, state, blockPos2, blockState3)) {
            return this.getFlowing(8, true);
        }
        int k = i - this.getDropOff(world);
        if (k <= 0) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.getFlowing(k, false);
    }

    @Override
    protected int getSlopeDistance(LevelReader world, BlockPos blockPos, int i, Direction direction, BlockState blockState, BlockPos blockPos2, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
        int j = 1000;
        for (Direction direction2 : Direction.Plane.HORIZONTAL) { // TODO: Sideways fluid support
            int k;
            if (direction2 == direction) continue;
            BlockPos blockPos3 = blockPos.relative(direction2);
            short s2 = FlowingFluid.getCacheKey(blockPos2, blockPos3);
            Pair pair = short2ObjectMap.computeIfAbsent(s2, s -> {
                BlockState blockState1 = world.getBlockState(blockPos3);
                return Pair.of(blockState1, blockState1.getFluidState());
            });
            BlockState blockState2 = (BlockState)pair.getFirst();
            FluidState fluidState = (FluidState)pair.getSecond();
            if (!this.canPassThrough(world, this.getFlowing(), blockPos, blockState, direction2, blockPos3, blockState2, fluidState)) continue;
            boolean bl = short2BooleanMap.computeIfAbsent(s2, s -> {
                BlockPos blockPos4 = blockPos3.relative(flowDirection);
                BlockState blockState4 = world.getBlockState(blockPos4);
                return this.isWaterHole(world, this.getFlowing(), blockPos3, blockState2, blockPos4, blockState4);
            });
            if (bl) {
                return i;
            }
            if (i >= this.getSlopeFindDistance(world) || (k = this.getSlopeDistance(world, blockPos3, i + 1, direction2.getOpposite(), blockState2, blockPos2, short2ObjectMap, short2BooleanMap)) >= j) continue;
            j = k;
        }
        return j;
    }

    @Override
    public boolean isWaterHole(BlockGetter world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        if (!this.canPassThroughWall(flowDirection, world, pos, state, fromPos, fromState)) {
            return false;
        }
        if (fromState.getFluidState().getType().isSame(this)) {
            return true;
        }
        return this.canHoldFluid(world, fromPos, fromState, fluid);
    }

    @Override
    public int sourceNeighborCount(LevelReader world, BlockPos pos) {
        int i = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) { // TODO: Sideways fluid support
            BlockPos blockPos = pos.relative(direction);
            FluidState fluidState = world.getFluidState(blockPos);
            if (!this.isSourceBlockOfThisType(fluidState)) continue;
            ++i;
        }
        return i;
    }

    @Override
    protected Map<Direction, FluidState> getSpread(LevelReader world, BlockPos pos, BlockState state) {
        int i = 1000;
        EnumMap<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectOpenHashMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap<Pair<BlockState, FluidState>>();
        Short2BooleanOpenHashMap short2BooleanMap = new Short2BooleanOpenHashMap();
        for (Direction direction : Direction.Plane.HORIZONTAL) { // TODO: Sideways fluid support
            BlockPos blockPos = pos.relative(direction);
            short s2 = FlowingFluid.getCacheKey(pos, blockPos);
            Pair pair = short2ObjectMap.computeIfAbsent(s2, s -> {
                BlockState blockState = world.getBlockState(blockPos);
                return Pair.of(blockState, blockState.getFluidState());
            });
            BlockState blockState = (BlockState)pair.getFirst();
            FluidState fluidState = (FluidState)pair.getSecond();
            FluidState fluidState2 = this.getNewLiquid(world, blockPos, blockState);
            if (!this.canPassThrough(world, fluidState2.getType(), pos, state, direction, blockPos, blockState, fluidState)) continue;
            BlockPos blockPos2 = blockPos.relative(flowDirection);
            boolean bl = short2BooleanMap.computeIfAbsent(s2, s -> {
                BlockState blockState2 = world.getBlockState(blockPos2);
                return this.isWaterHole(world, this.getFlowing(), blockPos, blockState, blockPos2, blockState2);
            });
            int j = bl ? 0 : this.getSlopeDistance(world, blockPos, 1, direction.getOpposite(), blockState, pos, short2ObjectMap, short2BooleanMap);
            if (j < i) {
                map.clear();
            }
            if (j > i) continue;
            map.put(direction, fluidState2);
            i = j;
        }
        return map;
    }

    public static boolean isFluidInDirectionEqual(FluidState state, BlockGetter world, BlockPos pos) {
        if(state.getType() instanceof DirectionalFluid directionalFluid)
            return state.getType().isSame(world.getFluidState(pos.relative(directionalFluid.getFlowDirection())).getType());
        return state.getType().isSame(world.getFluidState(pos.above()).getType());
    }

    @Override
    public float getHeight(FluidState state, BlockGetter world, BlockPos pos) {
        if (isFluidInDirectionEqual(state, world, pos)) {
            return 1.0f;
        }
        return state.getOwnHeight();
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter world, BlockPos pos) {
        if (state.getAmount() == 9 && isFluidInDirectionEqual(state, world, pos)) {
            return Shapes.block();
        }
        return this.shapes.computeIfAbsent(state, fluidState -> Shapes.box(0.0, 0.0, 0.0, 1.0, fluidState.getHeight(world, pos), 1.0));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean useDefaultRenderer() {
        return true;
    }
}
