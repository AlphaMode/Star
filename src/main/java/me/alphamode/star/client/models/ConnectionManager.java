package me.alphamode.star.client.models;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class ConnectionManager {

    private final ConnectionData[] connections = new ConnectionData[6];;
    private class ConnectionData {
        private final Direction direction;

        private boolean up, down, left, right, topRight, topLeft, bottomRight, bottomLeft;

        public ConnectionData(Direction direction) {
            this.direction = direction;
        }
    }

    public ConnectionManager(BlockRenderView blockView, BlockState state, BlockPos pos) {
        for(Direction face : Direction.values()) {
            ConnectionData data = new ConnectionData(face);
            boolean positive = face.getDirection() == Direction.AxisDirection.POSITIVE;
            Direction h = getRightDirection(blockView, pos, state, face);
            Direction v = getUpDirection(blockView, pos, state, face);
            h = positive ? h.getOpposite() : h;
            if (face == Direction.DOWN) {
                v = v.getOpposite();
                h = h.getOpposite();
            }

            final Direction horizontal = h;
            final Direction vertical = v;

            data.up = canConnect(blockView, pos, state, face, horizontal, vertical, 0, 1);
            data.down = canConnect(blockView, pos, state, face, horizontal, vertical, 0, -1);

            data.left = canConnect(blockView, pos, state, face, horizontal, vertical, -1, 0);
            data.right = canConnect(blockView, pos, state, face, horizontal, vertical, 1,0);

            data.topLeft =
                    data.up && data.left && canConnect(blockView, pos, state, face, horizontal, vertical, -1, 1);
            data.topRight =
                    data.up && data.right && canConnect(blockView, pos, state, face, horizontal, vertical, 1, 1);
            data.bottomLeft = data.down && data.left
                    && canConnect(blockView, pos, state, face, horizontal, vertical, -1, -1);
            data.bottomRight = data.down && data.right
                    && canConnect(blockView, pos, state, face, horizontal, vertical, 1, -1);
            connections[face.getId()] = data;
        }
    }

    public boolean canConnect(BlockRenderView blockView, BlockPos pos, BlockState state, Direction face,
                              final Direction horizontal, final Direction vertical, int sh, int sv) {
        BlockPos p = pos.offset(horizontal, sh)
                .offset(vertical, sv);
        boolean test = connectsTo(state, blockView.getBlockState(p), blockView, pos, p, face, horizontal, vertical);
        return test;
    }

    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos,
                              BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
        return connectsTo(state, other, reader, pos, otherPos, face);
    }

    public boolean connectsTo(BlockState state, BlockState other, BlockRenderView reader, BlockPos pos,
                              BlockPos otherPos, Direction face) {
        return !isBeingBlocked(state, reader, pos, otherPos, face) && state.getBlock() == other.getBlock();
    }

    protected boolean isBeingBlocked(BlockState state, BlockRenderView reader, BlockPos pos, BlockPos otherPos,
                                     Direction face) {
        BlockPos blockingPos = otherPos.offset(face);
        return face.getAxis()
                .choose(pos.getX(), pos.getY(), pos.getZ()) == face.getAxis()
                .choose(otherPos.getX(), otherPos.getY(), otherPos.getZ())
                && connectsTo(state, reader.getBlockState(blockingPos), reader, pos, blockingPos, face);
    }

    protected Direction getUpDirection(BlockRenderView reader, BlockPos pos, BlockState state, Direction face) {
        Direction.Axis axis = face.getAxis();
        return axis.isHorizontal() ? Direction.UP : Direction.NORTH;
    }

    protected Direction getRightDirection(BlockRenderView blockView, BlockPos pos, BlockState state, Direction face) {
        Direction.Axis axis = face.getAxis();
        return axis == Direction.Axis.X ? Direction.SOUTH : Direction.WEST;
    }

    public int getTextureIndex(Direction direction) {
        ConnectionData data = connections[direction.getId()];
        int tileX = 0, tileY = 0;
        int borders = (!data.up ? 1 : 0) + (!data.down ? 1 : 0) + (!data.left ? 1 : 0) + (!data.right ? 1 : 0);

        if (data.up)
            tileX++;
        if (data.down)
            tileX += 2;
        if (data.left)
            tileY++;
        if (data.right)
            tileY += 2;

        if (borders == 0) {
            if (data.topRight)
                tileX++;
            if (data.topLeft)
                tileX += 2;
            if (data.bottomRight)
                tileY += 2;
            if (data.bottomLeft)
                tileY++;
        }

        if (borders == 1) {
            if (!data.right) {
                if (data.topLeft || data.bottomLeft) {
                    tileY = 4;
                    tileX = -1 + (data.bottomLeft ? 1 : 0) + (data.topLeft ? 1 : 0) * 2;
                }
            }
            if (!data.left) {
                if (data.topRight || data.bottomRight) {
                    tileY = 5;
                    tileX = -1 + (data.bottomRight ? 1 : 0) + (data.topRight ? 1 : 0) * 2;
                }
            }
            if (!data.down) {
                if (data.topLeft || data.topRight) {
                    tileY = 6;
                    tileX = -1 + (data.topLeft ? 1 : 0) + (data.topRight ? 1 : 0) * 2;
                }
            }
            if (!data.up) {
                if (data.bottomLeft || data.bottomRight) {
                    tileY = 7;
                    tileX = -1 + (data.bottomLeft ? 1 : 0) + (data.bottomRight ? 1 : 0) * 2;
                }
            }
        }

        if (borders == 2) {
            if ((data.up && data.left && data.topLeft) || (data.down && data.left && data.bottomLeft)
                    || (data.up && data.right && data.topRight) || (data.down && data.right && data.bottomRight))
                tileX += 3;
        }

        return tileX + 8 * tileY;
    }
}
