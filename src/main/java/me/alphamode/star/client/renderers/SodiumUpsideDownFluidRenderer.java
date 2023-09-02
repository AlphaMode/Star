package me.alphamode.star.client.renderers;

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
public class SodiumUpsideDownFluidRenderer {
    protected final FluidRendererAccessor fluidRenderer;

    public SodiumUpsideDownFluidRenderer(FluidRenderer fluidRenderer) {
        this.fluidRenderer = (FluidRendererAccessor) fluidRenderer;
    }

    private float fluidHeightUpsideDown(BlockRenderView world, DirectionalFluid fluid, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        FluidState fluidState = blockState.getFluidState();
        if (fluid.matchesType(fluidState.getFluid())) {
            FluidState fluidStateUp = world.getFluidState(blockPos.offset(fluid.getFlowDirection().getOpposite()));
            return fluid.matchesType(fluidStateUp.getFluid()) ? 1.0F : fluidState.getHeight();
        } else {
            return !blockState.isSolid() ? 0.0F : -1.0F;
        }
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

            float fluidHeight = this.fluidHeightUpsideDown(world, fluid, pos);
            float h1, h2, h3, h4;
            if (fluidHeight >= 1.0f) {
                h1 = 1.0f;
                h2 = 1.0f;
                h3 = 1.0f;
                h4 = 1.0f;
            } else {
                float north1 = this.fluidHeightUpsideDown(world, fluid, pos.north());
                float south1 = this.fluidHeightUpsideDown(world, fluid, pos.south());
                float east1 = this.fluidHeightUpsideDown(world, fluid, pos.east());
                float west1 = this.fluidHeightUpsideDown(world, fluid, pos.west());
                h1 = fluidRenderer.callFluidCornerHeight(world, fluid, fluidHeight, north1, west1, pos.offset(Direction.NORTH).offset(Direction.WEST));
                h2 = fluidRenderer.callFluidCornerHeight(world, fluid, fluidHeight, south1, west1, pos.offset(Direction.SOUTH).offset(Direction.WEST));
                h3 = fluidRenderer.callFluidCornerHeight(world, fluid, fluidHeight, south1, east1, pos.offset(Direction.SOUTH).offset(Direction.EAST));
                h4 = fluidRenderer.callFluidCornerHeight(world, fluid, fluidHeight, north1, east1, pos.offset(Direction.NORTH).offset(Direction.EAST));
            }

            final ModelQuadViewMutable quad = fluidRenderer.getQuad();

            LightMode lightMode = isWater && MinecraftClient.isAmbientOcclusionEnabled() ? LightMode.SMOOTH : LightMode.FLAT;
            LightPipeline lighter = fluidRenderer.getLighters().getLighter(lightMode);

            quad.setFlags(0);

            if (!sfUp && fluidRenderer.callIsSideExposed(world, posX, posY, posZ, fluid.getFlowDirection(), Math.min(Math.min(h1, h2), Math.min(h3, h4)))) {
                h1 -= FluidRendererAccessor.getEPSILON();
                h2 -= FluidRendererAccessor.getEPSILON();
                h3 -= FluidRendererAccessor.getEPSILON();
                h4 -= FluidRendererAccessor.getEPSILON();

                Vec3d velocity = fluidState.getVelocity(world, pos);

                Sprite sprite;
                ModelQuadFacing facing;
                float u1, u2, u3, u4;
                float v1, v2, v3, v4;

                if (velocity.x == 0.0D && velocity.z == 0.0D) {
                    sprite = sprites[0];
                    facing = ModelQuadFacing.POS_Y;
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
                    facing = ModelQuadFacing.UNASSIGNED;
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

                quad.setSprite(sprite);

                fluidRenderer.callSetVertex(quad, 0, 0.0f, 1 - h1, 0.0f, u1, v1);
                fluidRenderer.callSetVertex(quad, 1, 0.0f, 1 - h2, 1.0F, u2, v2);
                fluidRenderer.callSetVertex(quad, 2, 1.0F, 1 - h3, 1.0F, u3, v3);
                fluidRenderer.callSetVertex(quad, 3, 1.0F, 1 - h4, 0.0f, u4, v4);

                fluidRenderer.callUpdateQuad(quad, world, pos, lighter, fluid.getFlowDirection().getOpposite(), 1.0F, colorProvider, fluidState);

                fluidRenderer.callWriteQuad(meshBuilder, material, offset, quad, facing, false);

                if (fluidState.canFlowTo(world, fluidRenderer.getScratchPos().set(posX, posY + 1, posZ))) {
                    fluidRenderer.callWriteQuad(meshBuilder, material, offset, quad, ModelQuadFacing.NEG_Y, false);
                }
            }

            if (!sfDown) {
                float yOffset = 1.0F - FluidRendererAccessor.getEPSILON();
                Sprite sprite = sprites[0];

                float minU = sprite.getMinU();
                float maxU = sprite.getMaxU();
                float minV = sprite.getMinV();
                float maxV = sprite.getMaxV();
                quad.setSprite(sprite);

                fluidRenderer.callSetVertex(quad, 0, 0.0f, yOffset, 1.0F, minU, maxV);
                fluidRenderer.callSetVertex(quad, 1, 0.0f, yOffset, 0.0f, minU, minV);
                fluidRenderer.callSetVertex(quad, 2, 1.0F, yOffset, 0.0f, maxU, minV);
                fluidRenderer.callSetVertex(quad, 3, 1.0F, yOffset, 1.0F, maxU, maxV);

                fluidRenderer.callUpdateQuad(quad, world, pos, lighter, fluid.getFlowDirection(), 1.0F, colorProvider, fluidState);

                fluidRenderer.callWriteQuad(meshBuilder, material, offset, quad, ModelQuadFacing.NEG_Y, false);
            }

            fluidRenderer.getQuad().setFlags(ModelQuadFlags.IS_ALIGNED);

            for (Direction dir : DirectionUtil.HORIZONTAL_DIRECTIONS) {
                float yOffset = 1.0F;
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

                        c1 = h4;
                        c2 = h1;
                        x1 = 1.0f;
                        x2 = 0.0F;
                        z1 = FluidRendererAccessor.getEPSILON();
                        z2 = z1;
                        break;
                    case SOUTH:
                        if (sfSouth) {
                            continue;
                        }

                        c1 = h2;
                        c2 = h3;
                        x1 = 0.0F;
                        x2 = 1.0f;
                        z1 = 1.0f - FluidRendererAccessor.getEPSILON();
                        z2 = z1;
                        break;
                    case WEST:
                        if (sfWest) {
                            continue;
                        }

                        c1 = h1;
                        c2 = h2;
                        x1 = FluidRendererAccessor.getEPSILON();
                        x2 = x1;
                        z1 = 0.0F;
                        z2 = 1.0f;
                        break;
                    case EAST:
                        if (sfEast) {
                            continue;
                        }

                        c1 = h3;
                        c2 = h4;
                        x1 = 1.0f - FluidRendererAccessor.getEPSILON();
                        x2 = x1;
                        z1 = 1.0f;
                        z2 = 0.0F;
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

                    float u1 = sprite.getFrameU(0.0D);
                    float u2 = sprite.getFrameU(8.0D);
                    float v1 = sprite.getFrameV((1.0F - c1) * 16.0F * 0.5F);
                    float v2 = sprite.getFrameV((1.0F - c2) * 16.0F * 0.5F);
                    float v3 = sprite.getFrameV(8.0D);

                    quad.setSprite(sprite);

                    fluidRenderer.callSetVertex(quad, 0, x2, 1 - c2, z2, u2, v2);
                    fluidRenderer.callSetVertex(quad, 1, x2, yOffset, z2, u2, v3);
                    fluidRenderer.callSetVertex(quad, 2, x1, yOffset, z1, u1, v3);
                    fluidRenderer.callSetVertex(quad, 3, x1, 1 - c1, z1, u1, v1);

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
