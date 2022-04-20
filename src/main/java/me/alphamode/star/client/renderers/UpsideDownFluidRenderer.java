package me.alphamode.star.client.renderers;

import me.alphamode.star.world.fluids.DirectionalFluid;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

import static net.minecraft.client.render.block.FluidRenderer.isSameFluid;
import static net.minecraft.client.render.block.FluidRenderer.shouldRenderSide;

/**
 * A Modified version of vanilla's fluid renderer to support
 * upside-down fluid rendering
 */
public class UpsideDownFluidRenderer extends SimpleFluidRenderHandler {

    public UpsideDownFluidRenderer(Identifier stillTexture, Identifier flowingTexture, Identifier overlayTexture, int tint) {
        super(stillTexture, flowingTexture, overlayTexture, tint);
    }

    public UpsideDownFluidRenderer(Identifier stillTexture, Identifier flowingTexture, Identifier overlayTexture) {
        this(stillTexture, flowingTexture, overlayTexture, -1);
    }

    public UpsideDownFluidRenderer(int tint) {
        this(WATER_STILL, WATER_FLOWING, WATER_OVERLAY, tint);
    }

    public UpsideDownFluidRenderer() {
        this(WATER_STILL, WATER_FLOWING, WATER_OVERLAY);
    }

    @Override
    public boolean renderFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        DirectionalFluid fluid = (DirectionalFluid) fluidState.getFluid();
        if(fluid.getFlowDirection() == Direction.DOWN)
            return super.renderFluid(pos, world, vertexConsumer, blockState, fluidState);
        boolean isInLava = fluidState.isIn(FluidTags.LAVA);
        Sprite[] sprites = isInLava ? FluidRenderHandlerRegistry.INSTANCE.get(Fluids.LAVA).getFluidSprites(world, pos, fluidState) : getFluidSprites(world, pos, fluidState);
        int fluidColor = isInLava ? 16777215 : BiomeColors.getWaterColor(world, pos);
        float r = (fluidColor >> 16 & 0xFF) / 255.0F;
        float g = (fluidColor >> 8 & 0xFF) / 255.0F;
        float b = (fluidColor & 0xFF) / 255.0F;
        BlockState downState = world.getBlockState(pos.offset(Direction.DOWN));
        FluidState downFluidState = downState.getFluidState();
        BlockState upState = world.getBlockState(pos.offset(Direction.UP));
        FluidState upFluidState = upState.getFluidState();
        BlockState northState = world.getBlockState(pos.offset(Direction.NORTH));
        FluidState northFluidState = northState.getFluidState();
        BlockState southState = world.getBlockState(pos.offset(Direction.SOUTH));
        FluidState southFluidState = southState.getFluidState();
        BlockState westState = world.getBlockState(pos.offset(Direction.WEST));
        FluidState westFluidState = westState.getFluidState();
        BlockState eastState = world.getBlockState(pos.offset(Direction.EAST));
        FluidState eastFluidState = eastState.getFluidState();
        boolean isFluidTheSame = !isSameFluid(fluidState, downFluidState);
        boolean renderBottomSide = shouldRenderSide(world, pos, fluidState, blockState, fluid.getFlowDirection(), upFluidState)
                && !isSideCovered(world, pos, fluid.getFlowDirection(), 0.8888889F, upState);
        boolean renderNorthSide = shouldRenderSide(world, pos, fluidState, blockState, Direction.NORTH, northFluidState);
        boolean renderSouthSide = shouldRenderSide(world, pos, fluidState, blockState, Direction.SOUTH, southFluidState);
        boolean renderWestSide = shouldRenderSide(world, pos, fluidState, blockState, Direction.WEST, westFluidState);
        boolean renderEastSide = shouldRenderSide(world, pos, fluidState, blockState, Direction.EAST, eastFluidState);
        if (!isFluidTheSame && !renderBottomSide && !renderEastSide && !renderWestSide && !renderNorthSide && !renderSouthSide) {
            return false;
        } else {
            boolean bl8 = false;
            float downBrightness = world.getBrightness(Direction.DOWN, true);
            float upBrightness = world.getBrightness(Direction.UP, true);
            float northBrightness = world.getBrightness(Direction.NORTH, true);
            float westBrightness = world.getBrightness(Direction.WEST, true);
            float fluidHeight = this.getFluidHeight(world, fluid, pos, blockState, fluidState);
            float northEastHeight, northWestHeight, southEastHeight, southWestHeight;
            if (fluidHeight >= 1.0F) {
                northEastHeight = 1.0F;
                northWestHeight = 1.0F;
                southEastHeight = 1.0F;
                southWestHeight = 1.0F;
            } else {
                float s = this.getFluidHeight(world, fluid, pos.north(), northState, northFluidState);
                float t = this.getFluidHeight(world, fluid, pos.south(), southState, southFluidState);
                float u = this.getFluidHeight(world, fluid, pos.east(), eastState, eastFluidState);
                float v = this.getFluidHeight(world, fluid, pos.west(), westState, westFluidState);
                northEastHeight = this.getHeightToRenderFluid(world, fluid, fluidHeight, s, u, pos.offset(Direction.NORTH).offset(Direction.EAST));
                northWestHeight = this.getHeightToRenderFluid(world, fluid, fluidHeight, s, v, pos.offset(Direction.NORTH).offset(Direction.WEST));
                southEastHeight = this.getHeightToRenderFluid(world, fluid, fluidHeight, t, u, pos.offset(Direction.SOUTH).offset(Direction.EAST));
                southWestHeight = this.getHeightToRenderFluid(world, fluid, fluidHeight, t, v, pos.offset(Direction.SOUTH).offset(Direction.WEST));
            }

            double chunkX = pos.getX() & 15;
            double chunkY = pos.getY() & 15;
            double chunkZ = pos.getZ() & 15;
            float y = renderBottomSide ? 0.001F : 0.0F;
            if (isFluidTheSame /*&& !isSideCovered(world, pos, fluid.getFlowDirection().getOpposite(), Math.min(Math.min(northWestHeight, southWestHeight), Math.min(southEastHeight, northEastHeight)), downState)*/) {
                bl8 = true;
                northWestHeight -= 0.001F;
                southWestHeight -= 0.001F;
                southEastHeight -= 0.001F;
                northEastHeight -= 0.001F;
                Vec3d vec3d = fluidState.getVelocity(world, pos);
                float z, ab, ad, af, aa, ac, ae, ag;
                if (vec3d.x == 0.0 && vec3d.z == 0.0) {
                    Sprite sprite = sprites[0];
                    z = sprite.getFrameU(0.0);
                    aa = sprite.getFrameV(0.0);
                    ab = z;
                    ac = sprite.getFrameV(16.0);
                    ad = sprite.getFrameU(16.0);
                    ae = ac;
                    af = ad;
                    ag = aa;
                } else {
                    Sprite sprite = sprites[1];
                    float ah = (float) MathHelper.atan2(vec3d.z, vec3d.x) - (float) (Math.PI / 2);
                    float ai = MathHelper.sin(ah) * 0.25F;
                    float aj = MathHelper.cos(ah) * 0.25F;
                    float ak = 8.0F;
                    z = sprite.getFrameU(8.0F + (-aj - ai) * 16.0F);
                    aa = sprite.getFrameV(8.0F + (-aj + ai) * 16.0F);
                    ab = sprite.getFrameU(8.0F + (-aj + ai) * 16.0F);
                    ac = sprite.getFrameV(8.0F + (aj + ai) * 16.0F);
                    ad = sprite.getFrameU(8.0F + (aj + ai) * 16.0F);
                    ae = sprite.getFrameV(8.0F + (aj - ai) * 16.0F);
                    af = sprite.getFrameU(8.0F + (aj - ai) * 16.0F);
                    ag = sprite.getFrameV(8.0F + (-aj - ai) * 16.0F);
                }

                float al = (z + ab + ad + af) / 4.0F;
                float ah = (aa + ac + ae + ag) / 4.0F;
                float ai = sprites[0].getWidth() / (sprites[0].getMaxU() - sprites[0].getMinU());
                float aj = sprites[0].getHeight() / (sprites[0].getMaxV() - sprites[0].getMinV());
                float ak = 4.0F / Math.max(aj, ai);
                z = MathHelper.lerp(ak, z, al);
                ab = MathHelper.lerp(ak, ab, al);
                ad = MathHelper.lerp(ak, ad, al);
                af = MathHelper.lerp(ak, af, al);
                aa = MathHelper.lerp(ak, aa, ah);
                ac = MathHelper.lerp(ak, ac, ah);
                ae = MathHelper.lerp(ak, ae, ah);
                ag = MathHelper.lerp(ak, ag, ah);
                int am = this.getLight(world, pos);
                float an = upBrightness * r;
                float ao = upBrightness * g;
                float ap = upBrightness * b; // The code below renders the fluid slope
                this.vertex(vertexConsumer, chunkX + 0.0, chunkY - northWestHeight + 1, chunkZ + 0.0, an, ao, ap, z, aa, am);
                this.vertex(vertexConsumer, chunkX + 0.0, chunkY - southWestHeight + 1, chunkZ + 1.0, an, ao, ap, ab, ac, am);
                this.vertex(vertexConsumer, chunkX + 1.0, chunkY - southEastHeight + 1, chunkZ + 1.0, an, ao, ap, ad, ae, am);
                this.vertex(vertexConsumer, chunkX + 1.0, chunkY - northEastHeight + 1, chunkZ + 0.0, an, ao, ap, af, ag, am);
//                if (fluidState.method_15756(world, pos.offset(fluid.getFlowDirection().getOpposite()))) {
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY - northWestHeight + 1, chunkZ + 0.0, an, ao, ap, z, aa, am);
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY - northEastHeight + 1, chunkZ + 0.0, an, ao, ap, af, ag, am);
                    this.vertex(vertexConsumer, chunkX + 1.0, chunkY - southEastHeight + 1, chunkZ + 1.0, an, ao, ap, ad, ae, am);
                    this.vertex(vertexConsumer, chunkX + 0.0, chunkY - southWestHeight + 1, chunkZ + 1.0, an, ao, ap, ab, ac, am);
//                }
            }

            if (renderBottomSide) {
                float z = sprites[0].getMinU();
                float ab = sprites[0].getMaxU();
                float ad = sprites[0].getMinV();
                float af = sprites[0].getMaxV();
                int aq = this.getLight(world, pos.offset(fluid.getFlowDirection()));
                float ac = downBrightness * r;
                float ae = downBrightness * g;
                float ag = downBrightness * b;
                this.vertex(vertexConsumer, chunkX + 1.0, chunkY + y + 1, chunkZ, ac, ae, ag, z, af, aq);
                this.vertex(vertexConsumer, chunkX, chunkY + y + 1, chunkZ, ac, ae, ag, ab, af, aq);
                this.vertex(vertexConsumer, chunkX, chunkY + y + 1, chunkZ + 1.0, ac, ae, ag, ab, ad, aq);
                this.vertex(vertexConsumer, chunkX + 1.0, chunkY + y + 1, chunkZ + 1.0, ac, ae, ag, z, ad, aq);

                bl8 = true;
            }

            int light = this.getLight(world, pos);

            for (Direction direction : Direction.Type.HORIZONTAL) {
                float sideY, endSideY;
                double startX, startZ, endX, endZ;
                boolean shouldRender;
                switch (direction) { // Handles how each side should look when rendering
                    case NORTH:
                        sideY = northWestHeight;
                        endSideY = northEastHeight;
                        startX = chunkX;
                        endX = chunkX + 1.0;
                        startZ = chunkZ + 0.001F;
                        endZ = chunkZ + 0.001F;
                        shouldRender = renderNorthSide;
                        break;
                    case SOUTH:
                        sideY = southEastHeight;
                        endSideY = southWestHeight;
                        startX = chunkX + 1.0;
                        endX = chunkX;
                        startZ = chunkZ + 1.0 - 0.001F;
                        endZ = chunkZ + 1.0 - 0.001F;
                        shouldRender = renderSouthSide;
                        break;
                    case WEST:
                        sideY = southWestHeight;
                        endSideY = northWestHeight;
                        startX = chunkX + 0.001F;
                        endX = chunkX + 0.001F;
                        startZ = chunkZ + 1.0;
                        endZ = chunkZ;
                        shouldRender = renderWestSide;
                        break;
                    default:
                        sideY = northEastHeight;
                        endSideY = southEastHeight;
                        startX = chunkX + 1.0 - 0.001F;
                        endX = chunkX + 1.0 - 0.001F;
                        startZ = chunkZ;
                        endZ = chunkZ + 1.0;
                        shouldRender = renderEastSide;
                }

                if (shouldRender && !isSideCovered(world, pos, direction, Math.max(sideY, endSideY), world.getBlockState(pos.offset(direction)))) {
                    bl8 = true;
                    BlockPos blockPos = pos.offset(direction);
                    Sprite sprite2 = sprites[1];
                    if (!isInLava) {
                        Block block = world.getBlockState(blockPos).getBlock();
                        if (block instanceof TransparentBlock || block instanceof LeavesBlock) {
                            sprite2 = getFluidSprites(world, blockPos, fluidState)[2];
                        }
                    }

                    float startU = sprite2.getFrameU(0.0);
                    float endV = sprite2.getFrameV((1.0F - sideY) * 16.0F * 0.5F);

                    float ap = sprite2.getFrameU(8.0);
                    float ax = sprite2.getFrameV((1.0F - endSideY) * 16.0F * 0.5F);
                    float ay = sprite2.getFrameV(8.0);
                    float directionBrightness = direction.getAxis() == Direction.Axis.Z ? northBrightness : westBrightness;
                    float red = upBrightness * directionBrightness * r;
                    float blue = upBrightness * directionBrightness * g;
                    float green = upBrightness * directionBrightness * b;

                    this.vertex(vertexConsumer, endX, chunkY + 1 - endSideY, endZ, red, blue, green, ap, ax, light);
                    this.vertex(vertexConsumer, startX, chunkY + 1 - sideY, startZ, red, blue, green, startU, endV, light);
                    this.vertex(vertexConsumer, startX, chunkY + 1 - y, startZ, red, blue, green, startU, ay, light);
                    this.vertex(vertexConsumer, endX, chunkY + 1 - y, endZ, red, blue, green, ap, ay, light);
                    if (sprite2 != getFluidSprites(world, blockPos, fluidState)[2]) { // Render overlay (inside of the fluid)
                        this.vertex(vertexConsumer, endX, chunkY + 1 - y, endZ, red, blue, green, startU, ay, light);
                        this.vertex(vertexConsumer, startX, chunkY + 1 - y, startZ, red, blue, green, ap, ay, light);
                        this.vertex(vertexConsumer, startX, chunkY + 1 - sideY, startZ, red, blue, green, ap, ax, light);
                        this.vertex(vertexConsumer, endX, chunkY + 1 - endSideY, endZ, red, blue, green, startU, endV, light);
                    }
                }
            }

            return bl8;
        }
    }

    private float getFluidHeight(BlockRenderView blockRenderView, DirectionalFluid fluid, BlockPos blockPos) {
        BlockState blockState = blockRenderView.getBlockState(blockPos);
        return this.getFluidHeight(blockRenderView, fluid, blockPos, blockState, blockState.getFluidState());
    }

    private float getHeightToRenderFluid(BlockRenderView blockRenderView, DirectionalFluid fluid, float f, float g, float h, BlockPos blockPos) {
        if (!(h >= 1.0F) && !(g >= 1.0F)) {
            float[] fs = new float[2];
            if (h > 0.0F || g > 0.0F) {
                float i = this.getFluidHeight(blockRenderView, fluid, blockPos);
                if (i >= 1.0F) {
                    return 1.0F;
                }

                this.offsetHeight(fs, i);
            }

            this.offsetHeight(fs, f);
            this.offsetHeight(fs, h);
            this.offsetHeight(fs, g);
            return fs[0] / fs[1];
        } else {
            return 1.0F;
        }
    }

    private void offsetHeight(float[] fs, float f) {
        if (f >= 0.8F) {
            fs[0] += f * 10.0F;
            fs[1] += 10.0F;
        } else if (f >= 0.0F) {
            fs[0] += f;
            fs[1]++;
        }

    }

    private float getFluidHeight(BlockRenderView blockRenderView, DirectionalFluid fluid, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (fluid.matchesType(fluidState.getFluid())) {
            BlockState blockState2 = blockRenderView.getBlockState(blockPos.offset(fluid.getFlowDirection().getOpposite()));
            return fluid.matchesType(blockState2.getFluidState().getFluid()) ? 1.0F : fluidState.getHeight();
        } else {
            return !blockState.getMaterial().isSolid() ? 0.0F : -1.0F;
        }
    }

    private int getLight(BlockRenderView world, BlockPos pos) {
        int i = WorldRenderer.getLightmapCoordinates(world, pos);
        int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
        int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
        return (k > l ? k : l) | (m > n ? m : n) << 16;
    }

    private void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, 1.0F).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F).next();
    }

    private static boolean isSideCovered(BlockView world, Direction direction, float f, BlockPos pos, BlockState state) {
        if (state.isOpaque()) {
            VoxelShape voxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, f, 1.0);
            VoxelShape voxelShape2 = state.getCullingShape(world, pos);
            return VoxelShapes.isSideCovered(voxelShape, voxelShape2, direction);
        } else {
            return false;
        }
    }

    private static boolean isSideCovered(BlockView blockView, BlockPos blockPos, Direction direction, float maxDeviation, BlockState blockState) {
        return isSideCovered(blockView, direction, maxDeviation, blockPos.offset(direction), blockState);
    }

    private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state);
    }

}
