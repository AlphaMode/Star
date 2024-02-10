package me.alphamode.star.client.models;

import me.alphamode.star.client.renderers.UpsideDownFluidRenderer;
import me.alphamode.star.world.fluids.DirectionalFluid;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;

import java.util.function.Supplier;

public class NorthFluidModel implements FluidBakedModel {
    private static final float EPSILON = 0F;

    public static boolean isSideExposed(BlockRenderView world, int x, int y, int z, Direction dir, float height) {
        BlockPos pos = new BlockPos(x + dir.getOffsetX(), y + dir.getOffsetY(), z + dir.getOffsetZ());
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOpaque()) {
            VoxelShape shape = blockState.getCullingShape(world, pos);
            if (shape == VoxelShapes.fullCube()) {
                return dir == Direction.NORTH;
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

    public static boolean isFluidOccluded(BlockRenderView world, int x, int y, int z, Direction dir, Fluid fluid) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState blockState = world.getBlockState(pos);
        BlockPos adjPos = new BlockPos(x + dir.getOffsetX(), y + dir.getOffsetY(), z + dir.getOffsetZ());
        if (!blockState.isOpaque()) {
            return world.getFluidState(adjPos).getFluid().matchesType(fluid);
        } else {
            return world.getFluidState(adjPos).getFluid().matchesType(fluid) || blockState.isSideSolid(world, pos, dir, SideShapeType.FULL);
        }
    }

    @Override
    public void emitFluidQuads(BlockRenderView world, BlockState state, FluidState fluidState, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        DirectionalFluid fluid = (DirectionalFluid) fluidState.getFluid();
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);

        int fluidColor = handler.getFluidColor(world, pos, fluidState);

        boolean sfUp = isFluidOccluded(world, posX, posY, posZ, fluid.getFlowDirection(world, fluidState, pos).getOpposite(), fluid);
        boolean sfDown = isFluidOccluded(world, posX, posY, posZ, fluid.getFlowDirection(world, fluidState, pos), fluid) ||
                !isSideExposed(world, posX, posY, posZ, fluid.getFlowDirection(world, fluidState, pos), 0.8888889F);
        boolean sfNorth = isFluidOccluded(world, posX, posY, posZ, Direction.UP, fluid);
        boolean sfSouth = isFluidOccluded(world, posX, posY, posZ, Direction.DOWN, fluid);
        boolean sfWest = isFluidOccluded(world, posX, posY, posZ, Direction.WEST, fluid);
        boolean sfEast = isFluidOccluded(world, posX, posY, posZ, Direction.EAST, fluid);

        if (sfUp && sfDown && sfEast && sfWest && sfNorth && sfSouth) {
            return;
        }

        Sprite[] sprites = handler.getFluidSprites(world, pos, fluidState);

        // Calculate the height of are fluid
        float fluidHeight = UpsideDownFluidRenderer.getFluidHeight(world, fluid, pos);
        float h1, h2, h3, h4;
        if (fluidHeight >= 1.0f) {
            h1 = 1.0f;
            h2 = 1.0f;
            h3 = 1.0f;
            h4 = 1.0f;
        } else {
            float north1 = UpsideDownFluidRenderer.getFluidHeight(world, fluid, pos.up());
            float south1 = UpsideDownFluidRenderer.getFluidHeight(world, fluid, pos.down());
            float east1 = UpsideDownFluidRenderer.getFluidHeight(world, fluid, pos.east());
            float west1 = UpsideDownFluidRenderer.getFluidHeight(world, fluid, pos.west());
            h1 = UpsideDownFluidRenderer.getHeightToRenderFluid(world, fluid, fluidHeight, north1, west1, pos.offset(Direction.UP).offset(Direction.WEST));
            h2 = UpsideDownFluidRenderer.getHeightToRenderFluid(world, fluid, fluidHeight, south1, west1, pos.offset(Direction.DOWN).offset(Direction.WEST));
            h3 = UpsideDownFluidRenderer.getHeightToRenderFluid(world, fluid, fluidHeight, south1, east1, pos.offset(Direction.DOWN).offset(Direction.EAST));
            h4 = UpsideDownFluidRenderer.getHeightToRenderFluid(world, fluid, fluidHeight, north1, east1, pos.offset(Direction.UP).offset(Direction.EAST));
        }
        final QuadEmitter quad = context.getEmitter();

        if (!sfUp && isSideExposed(world, posX, posY, posZ, fluid.getFlowDirection(world, fluidState, pos), Math.min(Math.min(h1, h2), Math.min(h3, h4)))) {
            h1 -= EPSILON;
            h2 -= EPSILON;
            h3 -= EPSILON;
            h4 -= EPSILON;

            Vec3d velocity = fluidState.getVelocity(world, pos);

            Sprite sprite;
            float u1, u2, u3, u4;
            float v1, v2, v3, v4;

            if (velocity.x == 0.0D && velocity.z == 0.0D) {
                sprite = sprites[0];
                u1 = sprite.getFrameU(0.0D);
                v1 = sprite.getFrameV(0.0D);
                u2 = u1;
                v2 = sprite.getFrameV(16.0D);
                u3 = sprite.getFrameU(16.0D);
                v3 = v2;
                u4 = u3;
                v4 = v1;
            } else {
                sprite = sprites[1];
                float dir = (float) MathHelper.atan2(velocity.z, velocity.x) - (1.5707964f);
                float sin = MathHelper.sin(dir) * 0.25F;
                float cos = MathHelper.cos(dir) * 0.25F;
                u1 = sprite.getFrameU(8.0F + (-cos - sin) * 16.0F);
                v1 = sprite.getFrameV(8.0F + (-cos + sin) * 16.0F);
                u2 = sprite.getFrameU(8.0F + (-cos + sin) * 16.0F);
                v2 = sprite.getFrameV(8.0F + (cos + sin) * 16.0F);
                u3 = sprite.getFrameU(8.0F + (cos + sin) * 16.0F);
                v3 = sprite.getFrameV(8.0F + (cos - sin) * 16.0F);
                u4 = sprite.getFrameU(8.0F + (cos - sin) * 16.0F);
                v4 = sprite.getFrameV(8.0F + (-cos - sin) * 16.0F);
            }

            float uAvg = (u1 + u2 + u3 + u4) / 4.0F;
            float vAvg = (v1 + v2 + v3 + v4) / 4.0F;
            float s1 = (float) sprites[0].getContents().getWidth() / (sprites[0].getMaxU() - sprites[0].getMinU());
            float s2 = (float) sprites[0].getContents().getHeight() / (sprites[0].getMaxV() - sprites[0].getMinV());
            float s3 = 4.0F / Math.max(s2, s1);

            u1 = MathHelper.lerp(s3, u1, uAvg);
            u2 = MathHelper.lerp(s3, u2, uAvg);
            u3 = MathHelper.lerp(s3, u3, uAvg);
            u4 = MathHelper.lerp(s3, u4, uAvg);
            v1 = MathHelper.lerp(s3, v1, vAvg);
            v2 = MathHelper.lerp(s3, v2, vAvg);
            v3 = MathHelper.lerp(s3, v3, vAvg);
            v4 = MathHelper.lerp(s3, v4, vAvg);

            // Bake the fluid slope

            // Top side (Render outside)
            if (fluidState.canFlowTo(world, pos.up())) {
                quad.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);

                setVertex(quad, 0, 0.0f, 0.0f, h2, u2, v2, fluid.getFlowDirection(world, fluidState, pos));
                setVertex(quad, 1, 1.0F, 0.0f, h3, u3, v3, fluid.getFlowDirection(world, fluidState, pos));
                setVertex(quad, 2, 1.0F, 1.0F, h4, u4, v4, fluid.getFlowDirection(world, fluidState, pos));
                setVertex(quad, 3, 0.0f, 1.0F, h1, u1, v1, fluid.getFlowDirection(world, fluidState, pos));
                quad.nominalFace();
                quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);
                quad.emit();
            }

            // Render inside
            quad.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);

            setVertex(quad, 0, 0.0f, 0.0f, h2, u1, v1, fluid.getFlowDirection(world, fluidState, pos).getOpposite());
            setVertex(quad, 1, 0.0F, 1.0f, h1, u4, v4, fluid.getFlowDirection(world, fluidState, pos).getOpposite());
            setVertex(quad, 2, 1.0F, 1.0F, h4, u3, v3, fluid.getFlowDirection(world, fluidState, pos).getOpposite());
            setVertex(quad, 3, 1.0f, 0.0F, h3, u2, v2, fluid.getFlowDirection(world, fluidState, pos).getOpposite());
            quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);
            quad.emit();
        }

        // Bake bottom of the fluid (not always visible)
        if (!sfDown) {
            float yOffset = EPSILON;
            Sprite sprite = sprites[0];

            float minU = sprite.getMinU();
            float maxU = sprite.getMaxU();
            float minV = sprite.getMinV();
            float maxV = sprite.getMaxV();
            quad.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);

            setVertex(quad, 0, 1.0F, 1.0f, yOffset, minU, maxV, fluid.getFlowDirection(world, fluidState, pos));
            setVertex(quad, 1, 1.0f, 0.0f, yOffset, minU, minV, fluid.getFlowDirection(world, fluidState, pos));
            setVertex(quad, 2, 0.0f, 0.0F, yOffset, maxU, minV, fluid.getFlowDirection(world, fluidState, pos));
            setVertex(quad, 3, 0.0F, 1.0F, yOffset, maxU, maxV, fluid.getFlowDirection(world, fluidState, pos));
            quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);

            quad.emit();
        }

        float yOffset = sfDown ? EPSILON : 0.0F;
        for (Direction direction : fluid.getDirections(world, pos, fluidState)) {
            float sideY, endSideY;
            float startX, startZ, endX, endZ;
            switch (direction) { // Handles how each side should look when rendering
                case UP:
                    if (sfNorth) {
                        continue;
                    }

                    sideY = h1;
                    endSideY = h4;
                    startX = 0;
                    endX = 1.0F;
                    startZ = 1.0F - EPSILON;
                    endZ = 1.0F - EPSILON;
                    break;
                case DOWN:
                    if (sfSouth) {
                        continue;
                    }

                    sideY = h3;
                    endSideY = h2;
                    startX = 1.0F;
                    endX = 0;
                    startZ = EPSILON;
                    endZ = EPSILON;
                    break;
                case WEST:
                    if (sfWest) {
                        continue;
                    }

                    sideY = h2;
                    endSideY = h1;
                    startX = EPSILON;
                    endX = EPSILON;
                    startZ = 1.0F;
                    endZ = 0;
                    break;
                default:
                    if (sfEast) {
                        continue;
                    }

                    sideY = h4;
                    endSideY = h3;
                    startX = 1.0F - EPSILON;
                    endX = 1.0F - EPSILON;
                    startZ = 0;
                    endZ = 1.0F;
            }

            if (isSideExposed(world, posX, posY, posZ, direction, Math.max(sideY, endSideY))) {
                Sprite sprite = sprites[1];

                boolean isOverlay = false;

                if (sprites.length > 2) {
                    BlockPos adjPos = pos.offset(direction);
                    BlockState adjBlock = world.getBlockState(adjPos);

                    if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(adjBlock.getBlock())) {
                        sprite = sprites[2];
                        isOverlay = true;
                    }
                }

                float startU = sprite.getFrameU(0.0);
                float endV = sprite.getFrameV((1.0F - sideY) * 16.0F * 0.5F);

                float ap = sprite.getFrameU(8.0);
                float ax = sprite.getFrameV((1.0F - endSideY) * 16.0F * 0.5F);
                float ay = sprite.getFrameV(8.0);

                setVertex(quad, 0, startX, endZ, sideY, ap, ax, direction);
                setVertex(quad, 1, endX, startZ, endSideY, startU, endV, direction);
                setVertex(quad, 2, endX, startZ, yOffset, startU, ay, direction);
                setVertex(quad, 3, startX, endZ, yOffset, ap, ay, direction);

                quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);
                quad.emit();

                if (!isOverlay) { // Render overlay (inside of the fluid)
                    setVertex(quad, 0, startX, endZ, yOffset, startU, ay, direction);
                    setVertex(quad, 1, endX, startZ, yOffset, ap, ay, direction);
                    setVertex(quad, 2, endX, startZ, endSideY, ap, endV, direction);
                    setVertex(quad, 3, startX, endZ, sideY, startU, ax, direction);

                    quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);
                    quad.emit();
                }
            }
        }
    }

    public void setVertex(QuadEmitter emitter, int vertexIndex, float x, float y, float z, float u, float v, Direction normal) {
        emitter.pos(vertexIndex, x, y, z);
        emitter.uv(vertexIndex, u, v);
        emitter.nominalFace(normal);
    }
}
