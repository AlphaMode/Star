package me.alphamode.star.client.renderers;

import me.alphamode.star.client.StarFluidRenderer;
import me.alphamode.star.mixin.sodium.FluidRendererAccessor;
import me.alphamode.star.world.fluids.DirectionalFluid;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadViewMutable;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.util.DirectionUtil;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

/**
 * A Modified version of sodium's fluid renderer to support
 * upside-down fluid rendering with sodium
 */
public class SodiumUpsideDownFluidRenderer extends StarFluidRenderer {
    protected final FluidRendererAccessor fluidRenderer;

    public SodiumUpsideDownFluidRenderer(FluidRenderer fluidRenderer) {
        this.fluidRenderer = (FluidRendererAccessor) fluidRenderer;
    }

    public void renderUpsideDown(WorldSlice world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkBuildBuffers buffers, FluidRenderHandler handler) {
        Material material = DefaultMaterials.forFluidState(fluidState);
        ChunkModelBuilder meshBuilder = buffers.get(material);
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        DirectionalFluid fluid = (DirectionalFluid) fluidState.getFluid();

        boolean sfUp = fluidRenderer.callIsFluidOccluded(world, posX, posY, posZ, fluid.getFlowDirection().getOpposite(), fluid);
        boolean sfDown = fluidRenderer.callIsFluidOccluded(world, posX, posY, posZ, fluid.getFlowDirection(), fluid) ||
                !fluidRenderer.callIsSideExposed(world, posX, posY, posZ, fluid.getFlowDirection(), 0.8888889F);
        boolean sfNorth = fluidRenderer.callIsFluidOccluded(world, posX, posY, posZ, Direction.NORTH, fluid);
        boolean sfSouth = fluidRenderer.callIsFluidOccluded(world, posX, posY, posZ, Direction.SOUTH, fluid);
        boolean sfWest = fluidRenderer.callIsFluidOccluded(world, posX, posY, posZ, Direction.WEST, fluid);
        boolean sfEast = fluidRenderer.callIsFluidOccluded(world, posX, posY, posZ, Direction.EAST, fluid);

        if (!sfUp || !sfDown || !sfEast || !sfWest || !sfNorth || !sfSouth) {
            boolean isWater = fluidState.isIn(FluidTags.WATER);

            ColorProvider<FluidState> colorProvider = fluidRenderer.callGetColorProvider(fluid, handler);

            Sprite[] sprites = handler.getFluidSprites(world, pos, fluidState);

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
            float yOffset = sfDown ? EPSILON : 0.0F;

            final ModelQuadViewMutable quad = fluidRenderer.getQuad();

            LightMode lightMode = isWater && MinecraftClient.isAmbientOcclusionEnabled() ? LightMode.SMOOTH : LightMode.FLAT;
            LightPipeline lighter = fluidRenderer.getLighters().getLighter(lightMode);

            quad.setFlags(0);

            if (!sfUp && this.isSideExposed(world, posX, posY, posZ, fluid.getFlowDirection().getOpposite(), Math.min(Math.min(northWestHeight, southWestHeight), Math.min(southEastHeight, northEastHeight)))) {
                northWestHeight -= EPSILON;
                southWestHeight -= EPSILON;
                southEastHeight -= EPSILON;
                northEastHeight -= EPSILON;

                Vec3d velocity = fluidState.getVelocity(world, pos);

                Sprite sprite;
                ModelQuadFacing facing;
                float u1, u2, u3, u4;
                float v1, v2, v3, v4;

                if (velocity.x == 0.0D && velocity.z == 0.0D) {
                    sprite = sprites[0];
                    facing = ModelQuadFacing.POS_Y;
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
                    facing = ModelQuadFacing.UNASSIGNED;
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

                quad.setSprite(sprite);

                FluidRendererAccessor.callSetVertex(quad, 0, 0.0f, 1 - northWestHeight, 0.0f, u1, v1);
                FluidRendererAccessor.callSetVertex(quad, 1, 1.0f, 1 - northEastHeight, 0.0F, u4, v4);
                FluidRendererAccessor.callSetVertex(quad, 2, 1.0F, 1 - southEastHeight, 1.0F, u3, v3);
                FluidRendererAccessor.callSetVertex(quad, 3, 0.0F, 1 - southWestHeight, 1.0f, u2, v2);

                fluidRenderer.callUpdateQuad(quad, world, pos, lighter, fluid.getFlowDirection().getOpposite(), 1.0F, colorProvider, fluidState);

                fluidRenderer.callWriteQuad(meshBuilder, material, offset, quad, facing, false);

                if (fluidState.canFlowTo(world, fluidRenderer.getScratchPos().set(pos, fluid.getFlowDirection().getOpposite()))) {
                    fluidRenderer.callWriteQuad(meshBuilder, material, offset, quad, ModelQuadFacing.NEG_Y, true);
                }
            }

            if (!sfDown) {
                float lastYOffset = yOffset;
                yOffset = 1.0F - EPSILON;
                Sprite sprite = sprites[0];

                float minU = sprite.getMinU();
                float maxU = sprite.getMaxU();
                float minV = sprite.getMinV();
                float maxV = sprite.getMaxV();
                quad.setSprite(sprite);

                FluidRendererAccessor.callSetVertex(quad, 0, 1.0F, yOffset, 0.0F, minU, maxV);
                FluidRendererAccessor.callSetVertex(quad, 1, 0.0f, yOffset, 0.0F, minU, minV);
                FluidRendererAccessor.callSetVertex(quad, 2, 0.0F, yOffset, 1.0F, maxU, minV);
                FluidRendererAccessor.callSetVertex(quad, 3, 1.0F, yOffset, 1.0F, maxU, maxV);

                fluidRenderer.callUpdateQuad(quad, world, pos, lighter, fluid.getFlowDirection(), 1.0F, colorProvider, fluidState);

                fluidRenderer.callWriteQuad(meshBuilder, material, offset, quad, ModelQuadFacing.POS_Y, false);
                yOffset = lastYOffset;
            }

            fluidRenderer.getQuad().setFlags(ModelQuadFlags.IS_ALIGNED);

            for (Direction dir : DirectionUtil.HORIZONTAL_DIRECTIONS) {
                float c1;
                float c2;
                float x1;
                float z1;
                float x2;
                float z2;

                switch (dir) {
                    case NORTH:
                        if (sfNorth) {
                            continue;
                        }

                        c1 = northWestHeight;
                        c2 = northEastHeight;
                        x1 = 0.0F;
                        x2 = 1.0F;
                        z1 = FluidRendererAccessor.getEPSILON();
                        z2 = FluidRendererAccessor.getEPSILON();
                        break;
                    case SOUTH:
                        if (sfSouth) {
                            continue;
                        }

                        c1 = southEastHeight;
                        c2 = southWestHeight;
                        x1 = 1.0F;
                        x2 = 0.0F;
                        z1 = 1.0F - FluidRendererAccessor.getEPSILON();
                        z2 = 1.0F - FluidRendererAccessor.getEPSILON();
                        break;
                    case WEST:
                        if (sfWest) {
                            continue;
                        }

                        c1 = southWestHeight;
                        c2 = northWestHeight;
                        x1 = FluidRendererAccessor.getEPSILON();
                        x2 = FluidRendererAccessor.getEPSILON();
                        z1 = 1.0F;
                        z2 = 0.0F;
                        break;
                    case EAST:
                        if (sfEast) {
                            continue;
                        }

                        c1 = northEastHeight;
                        c2 = southEastHeight;
                        x1 = 1.0F - FluidRendererAccessor.getEPSILON();
                        x2 = 1.0F - FluidRendererAccessor.getEPSILON();
                        z1 = 0.0F;
                        z2 = 1.0F;
                        break;
                    default:
                        continue;
                }

                if (fluidRenderer.callIsSideExposed(world, posX, posY, posZ, dir, Math.max(c1, c2))) {
                    int adjX = posX + dir.getOffsetX();
                    int adjY = posY + dir.getOffsetY();
                    int adjZ = posZ + dir.getOffsetZ();

                    Sprite sprite = sprites[1];

                    boolean isOverlay = false;

                    if (sprites.length > 2) {
                        BlockPos adjPos = fluidRenderer.getScratchPos().set(adjX, adjY, adjZ);
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

                    quad.setSprite(sprite);

                    FluidRendererAccessor.callSetVertex(quad, 0, x2, 1 - c2, z2, u2, v2);
                    FluidRendererAccessor.callSetVertex(quad, 1, x1, 1 - c1, z1, u1, v1);
                    FluidRendererAccessor.callSetVertex(quad, 2, x1, 1 - yOffset, z1, u1, v3);
                    FluidRendererAccessor.callSetVertex(quad, 3, x2, 1 - yOffset, z2, u2, v3);

                    float br = dir.getAxis() == Direction.Axis.Z ? 0.8F : 0.6F;

                    ModelQuadFacing facing = ModelQuadFacing.fromDirection(dir);

                    fluidRenderer.callUpdateQuad(quad, world, pos, lighter, dir, br, colorProvider, fluidState);

                    fluidRenderer.callWriteQuad(meshBuilder, material, offset, quad, facing, false);

                    if (!isOverlay) {
                        fluidRenderer.callWriteQuad(meshBuilder, material, offset, quad, facing.getOpposite(), true);
                    }
                }
            }
        }
    }
}
