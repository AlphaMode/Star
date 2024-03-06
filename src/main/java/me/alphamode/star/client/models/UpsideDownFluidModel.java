package me.alphamode.star.client.models;

import me.alphamode.star.client.StarFluidRenderer;
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

public class UpsideDownFluidModel extends StarFluidRenderer implements FluidBakedModel {
    @Override
    public void emitFluidQuads(BlockRenderView world, BlockState state, FluidState fluidState, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        
        DirectionalFluid fluid = (DirectionalFluid) fluidState.getFluid();
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);

        int fluidColor = handler.getFluidColor(world, pos, fluidState);

        boolean sfUp = isFluidOccluded(world, posX, posY, posZ, fluid.getFlowDirection().getOpposite(), fluid);
        boolean sfDown = isFluidOccluded(world, posX, posY, posZ, fluid.getFlowDirection(), fluid) ||
                !isSideExposed(world, posX, posY, posZ, fluid.getFlowDirection(), 0.8888889F);
        boolean sfNorth = isFluidOccluded(world, posX, posY, posZ, Direction.NORTH, fluid);
        boolean sfSouth = isFluidOccluded(world, posX, posY, posZ, Direction.SOUTH, fluid);
        boolean sfWest = isFluidOccluded(world, posX, posY, posZ, Direction.WEST, fluid);
        boolean sfEast = isFluidOccluded(world, posX, posY, posZ, Direction.EAST, fluid);

        if (sfUp && sfDown && sfEast && sfWest && sfNorth && sfSouth) {
            return;
        }
        
        Sprite[] sprites = handler.getFluidSprites(world, pos, fluidState);

        // Calculate the height of our fluid
        float fluidHeight = this.fluidHeight(world, fluid, pos);
        float northWestHeight, southWestHeight, southEastHeight, northEastHeight;
        if (fluidHeight >= 1.0f) {
            northWestHeight = 1.0f;
            southWestHeight = 1.0f;
            southEastHeight = 1.0f;
            northEastHeight = 1.0f;
        } else {
            float heightNorth = this.fluidHeight(world, fluid, scratchPos.set(pos, Direction.NORTH));
            float heightSouth = this.fluidHeight(world, fluid, scratchPos.set(pos, Direction.SOUTH));
            float heightEast = this.fluidHeight(world, fluid, scratchPos.set(pos, Direction.EAST));
            float heightWest = this.fluidHeight(world, fluid, scratchPos.set(pos, Direction.WEST));
            northWestHeight = this.fluidCornerHeight(world, fluid, fluidHeight, heightNorth, heightWest, scratchPos.set(pos).move(Direction.NORTH).move(Direction.WEST));
            southWestHeight = this.fluidCornerHeight(world, fluid, fluidHeight, heightSouth, heightWest, scratchPos.set(pos).move(Direction.SOUTH).move(Direction.WEST));
            southEastHeight = this.fluidCornerHeight(world, fluid, fluidHeight, heightSouth, heightEast, scratchPos.set(pos).move(Direction.SOUTH).move(Direction.EAST));
            northEastHeight = this.fluidCornerHeight(world, fluid, fluidHeight, heightNorth, heightEast, scratchPos.set(pos).move(Direction.NORTH).move(Direction.EAST));
        }
        final QuadEmitter quad = context.getEmitter();

        if (!sfUp && isSideExposed(world, posX, posY, posZ, fluid.getFlowDirection(), Math.min(Math.min(northWestHeight, southWestHeight), Math.min(southEastHeight, northEastHeight)))) {
            northWestHeight -= EPSILON;
            southWestHeight -= EPSILON;
            southEastHeight -= EPSILON;
            northEastHeight -= EPSILON;

            Vec3d velocity = fluidState.getVelocity(world, pos);

            Sprite sprite;
            float u1, u2, u3, u4;
            float v1, v2, v3, v4;

            if (velocity.x == 0.0D && velocity.z == 0.0D) {
                sprite = sprites[0];
                u1 = sprite.getFrameU(0.0F);
                v1 = sprite.getFrameV(0.0F);
                u2 = u1;
                v2 = sprite.getFrameV(1.0F);
                u3 = sprite.getFrameU(1.0F);
                v3 = v2;
                u4 = u3;
                v4 = v1;
            } else {
                sprite = sprites[1];
                float dir = (float) MathHelper.atan2(velocity.z, velocity.x) - (1.5707964f);
                float sin = MathHelper.sin(dir) * 0.25F;
                float cos = MathHelper.cos(dir) * 0.25F;
                u1 = sprite.getFrameU(0.5F + (-cos - sin));
                v1 = sprite.getFrameV(0.5F + (-cos + sin));
                u2 = sprite.getFrameU(0.5F + (-cos + sin));
                v2 = sprite.getFrameV(0.5F + (cos + sin));
                u3 = sprite.getFrameU(0.5F + (cos + sin));
                v3 = sprite.getFrameV(0.5F + (cos - sin));
                u4 = sprite.getFrameU(0.5F + (cos - sin));
                v4 = sprite.getFrameV(0.5F + (-cos - sin));
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

            // Top side
            if (fluidState.canFlowTo(world, pos.up())) {
                quad.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);

                setVertex(quad, 0, 0.0f, 1 - northWestHeight, 0.0f, u1, v1, fluid.getFlowDirection());
                setVertex(quad, 1, 0.0f, 1 - southWestHeight, 1.0F, u2, v2, fluid.getFlowDirection());
                setVertex(quad, 2, 1.0F, 1 - southEastHeight, 1.0F, u3, v3, fluid.getFlowDirection());
                setVertex(quad, 3, 1.0F, 1 - northEastHeight, 0.0f, u4, v4, fluid.getFlowDirection());
                quad.nominalFace();
                quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);
                quad.emit();
            }

            // Bottom side
            quad.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);

            setVertex(quad, 0, 0.0f, 1 - northWestHeight, 0.0f, u1, v1, fluid.getFlowDirection().getOpposite());
            setVertex(quad, 1, 1.0f, 1 - northEastHeight, 0.0F, u4, v4, fluid.getFlowDirection().getOpposite());
            setVertex(quad, 2, 1.0F, 1 - southEastHeight, 1.0F, u3, v3, fluid.getFlowDirection().getOpposite());
            setVertex(quad, 3, 0.0F, 1 - southWestHeight, 1.0f, u2, v2, fluid.getFlowDirection().getOpposite());
            quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);
            quad.emit();
        }

        // Bake bottom of the fluid (not always visible)
        if (!sfDown) {
            float yOffset = 1.0F - EPSILON;
            Sprite sprite = sprites[0];

            float minU = sprite.getMinU();
            float maxU = sprite.getMaxU();
            float minV = sprite.getMinV();
            float maxV = sprite.getMaxV();
            quad.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);

            setVertex(quad, 0, 1.0F, yOffset, 0.0F, minU, maxV, fluid.getFlowDirection());
            setVertex(quad, 1, 0.0F, yOffset, 0.0F, minU, minV, fluid.getFlowDirection());
            setVertex(quad, 2, 0.0F, yOffset, 1.0F, maxU, minV, fluid.getFlowDirection());
            setVertex(quad, 3, 1.0F, yOffset, 1.0F, maxU, maxV, fluid.getFlowDirection());
            quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);

            quad.emit();
        }

        float yOffset = sfDown ? EPSILON : 0.0F;
        for (Direction dir : Direction.Type.HORIZONTAL) {
            float c1, c2;
            float x1, z1, x2, z2;
            switch (dir) { // Handles how each side should look when rendering
                case NORTH:
                    if (sfNorth) {
                        continue;
                    }

                    c1 = northWestHeight;
                    c2 = northEastHeight;
                    x1 = 0.0F;
                    x2 = 1.0F;
                    z1 = EPSILON;
                    z2 = EPSILON;
                    break;
                case SOUTH:
                    if (sfSouth) {
                        continue;
                    }

                    c1 = southEastHeight;
                    c2 = southWestHeight;
                    x1 = 1.0F;
                    x2 = 0.0F;
                    z1 = 1.0F - EPSILON;
                    z2 = 1.0F - EPSILON;
                    break;
                case WEST:
                    if (sfWest) {
                        continue;
                    }

                    c1 = southWestHeight;
                    c2 = northWestHeight;
                    x1 = EPSILON;
                    x2 = EPSILON;
                    z1 = 1.0F;
                    z2 = 0.0F;
                    break;
                default:
                    if (sfEast) {
                        continue;
                    }

                    c1 = northEastHeight;
                    c2 = southEastHeight;
                    x1 = 1.0F - EPSILON;
                    x2 = 1.0F - EPSILON;
                    z1 = 0.0F;
                    z2 = 1.0F;
            }

            if (isSideExposed(world, posX, posY, posZ, dir, Math.max(c1, c2))) {
                int adjX = posX + dir.getOffsetX();
                int adjY = posY + dir.getOffsetY();
                int adjZ = posZ + dir.getOffsetZ();

                Sprite sprite = sprites[1];

                boolean isOverlay = false;

                if (sprites.length > 2) {
                    BlockPos adjPos = scratchPos.set(adjX, adjY, adjZ);
                    BlockState adjBlock = world.getBlockState(adjPos);

                    if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(adjBlock.getBlock())) {
                        sprite = sprites[2];
                        isOverlay = true;
                    }
                }

                float u1 = sprite.getFrameU(0.0F);
                float u2 = sprite.getFrameU(0.5F);
                float v1 = sprite.getFrameV((1.0F - c1) * 0.5F);
                float v2 = sprite.getFrameV((1.0F - c2) * 0.5F);
                float v3 = sprite.getFrameV(0.5F);

                setVertex(quad, 0, x2, 1 - c2, z2, u2, v2, dir);
                setVertex(quad, 1, x1, 1 - c1, z1, u1, v1, dir);
                setVertex(quad, 2, x1, 1 - yOffset, z1, u1, v3, dir);
                setVertex(quad, 3, x2, 1 - yOffset, z2, u2, v3, dir);

                quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);
                quad.emit();

                if (!isOverlay) { // Render overlay (inside of the fluid)
                    setVertex(quad, 0, x2, 1 - yOffset, z2, u1, v3, dir);
                    setVertex(quad, 1, x1, 1 - yOffset, z1, u2, v3, dir);
                    setVertex(quad, 2, x1, 1 - c1, z1, u2, v1, dir);
                    setVertex(quad, 3, x2, 1 - c2, z2, u1, v2, dir);

                    quad.spriteColor(0, fluidColor, fluidColor, fluidColor, fluidColor);
                    quad.emit();
                }
            }
        }
    }

    public void setVertex(QuadEmitter emitter, int vertexIndex, float x, float y, float z, float u, float v, Direction normal) {
        emitter.pos(vertexIndex, x, y, z);
        emitter.sprite(vertexIndex, 0, u, v);
        emitter.nominalFace(normal);
    }
}
