package me.alphamode.star.client.renderers;

import me.alphamode.star.client.StarFluidRenderer;
import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.client.models.UpsideDownFluidModel;
import me.alphamode.star.extensions.fabric.FluidRenderHandlerExtension;
import me.alphamode.star.world.fluids.DirectionalFluid;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.*;
import static net.minecraft.client.render.block.FluidRenderer.isSameFluid;
import static net.minecraft.client.render.block.FluidRenderer.shouldRenderSide;

/**
 * A Modified version of vanilla's fluid renderer to support
 * upside-down fluid rendering
 */
public class UpsideDownFluidRenderer extends StarFluidRenderer implements FluidRenderHandler, FluidRenderHandlerExtension {
    private final FluidBakedModel model;
    protected final Supplier<Identifier> stillGetter, flowingGetter, overlayGetter;
    protected final Sprite[] sprites;

    protected final int tint;

    public UpsideDownFluidRenderer(Supplier<Identifier> stillTexture, Supplier<Identifier> flowingTexture, Supplier<Identifier> overlayTexture, int tint) {
        this.stillGetter = stillTexture;
        this.flowingGetter = flowingTexture;
        this.overlayGetter = overlayTexture;
        this.sprites = new Sprite[overlayTexture == null ? 2 : 3];
        this.tint = tint;
        this.model = new UpsideDownFluidModel();
    }

    public UpsideDownFluidRenderer(Identifier stillTexture, Identifier flowingTexture, int tint) {
        this(() -> stillTexture, () -> flowingTexture, null, tint);
    }

    public UpsideDownFluidRenderer(Identifier stillTexture, Identifier flowingTexture, Identifier overlayTexture) {
        this(() -> stillTexture, () -> flowingTexture, () -> overlayTexture, -1);
    }

    public UpsideDownFluidRenderer(Identifier stillTexture, Identifier flowingTexture) {
        this(() -> stillTexture, () -> flowingTexture, null, -1);
    }

    public UpsideDownFluidRenderer(int tint) {
        this(() -> WATER_STILL, () -> WATER_FLOWING, () -> WATER_OVERLAY, tint);
    }

    public UpsideDownFluidRenderer() {
        this(WATER_STILL, WATER_FLOWING, WATER_OVERLAY);
    }

    @Override
    public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
        return sprites;
    }

    @Override
    public void reloadTextures(SpriteAtlasTexture textureAtlas) {
        sprites[0] = textureAtlas.getSprite(stillGetter.get());
        sprites[1] = textureAtlas.getSprite(flowingGetter.get());
        if (overlayGetter != null)
            sprites[2] = textureAtlas.getSprite(overlayGetter.get());
    }

    @Override
    public void renderFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        DirectionalFluid fluid = (DirectionalFluid) fluidState.getFluid();
        if(fluid.getFlowDirection() == Direction.DOWN) {
            FluidRenderHandler.super.renderFluid(pos, world, vertexConsumer, blockState, fluidState);
            return;
        }
        boolean isInLava = fluidState.isIn(FluidTags.LAVA);
        Sprite[] sprites = isInLava ? FluidRenderHandlerRegistry.INSTANCE.get(Fluids.LAVA).getFluidSprites(world, pos, fluidState) : getFluidSprites(world, pos, fluidState);
        int fluidColor = getFluidColor(world, pos, fluidState);
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
        if (isFluidTheSame || renderBottomSide || renderEastSide || renderWestSide || renderNorthSide || renderSouthSide) {
            float downBrightness = world.getBrightness(Direction.DOWN, true);
            float upBrightness = world.getBrightness(Direction.UP, true);
            float northBrightness = world.getBrightness(Direction.NORTH, true);
            float westBrightness = world.getBrightness(Direction.WEST, true);
            float fluidHeight = fluidHeight(world, fluid, pos);
            float northEastHeight, northWestHeight, southEastHeight, southWestHeight;
            if (fluidHeight >= 1.0F) {
                northEastHeight = 1.0F;
                northWestHeight = 1.0F;
                southEastHeight = 1.0F;
                southWestHeight = 1.0F;
            } else {
                float heightNorth = fluidHeight(world, fluid, scratchPos.set(pos, Direction.NORTH));
                float heightSouth = fluidHeight(world, fluid, scratchPos.set(pos, Direction.SOUTH));
                float heightEast = fluidHeight(world, fluid, scratchPos.set(pos, Direction.EAST));
                float heightWest = fluidHeight(world, fluid, scratchPos.set(pos, Direction.WEST));
                northWestHeight = fluidCornerHeight(world, fluid, fluidHeight, heightNorth, heightWest, scratchPos.set(pos).move(Direction.NORTH).move(Direction.WEST));
                southWestHeight = fluidCornerHeight(world, fluid, fluidHeight, heightSouth, heightWest, scratchPos.set(pos).move(Direction.SOUTH).move(Direction.WEST));
                southEastHeight = fluidCornerHeight(world, fluid, fluidHeight, heightSouth, heightEast, scratchPos.set(pos).move(Direction.SOUTH).move(Direction.EAST));
                northEastHeight = fluidCornerHeight(world, fluid, fluidHeight, heightNorth, heightEast, scratchPos.set(pos).move(Direction.NORTH).move(Direction.EAST));
            }

            double chunkX = pos.getX() & 15;
            double chunkY = pos.getY() & 15;
            double chunkZ = pos.getZ() & 15;
            float yOffset = renderBottomSide ? 0.001F : 0.0F;
            if (isFluidTheSame /*&& !isSideCovered(world, pos, fluid.getFlowDirection().getOpposite(), Math.min(Math.min(northWestHeight, southWestHeight), Math.min(southEastHeight, northEastHeight)), downState)*/) {
                northWestHeight -= 0.001F;
                southWestHeight -= 0.001F;
                southEastHeight -= 0.001F;
                northEastHeight -= 0.001F;
                Vec3d vec3d = fluidState.getVelocity(world, pos);
                float z, ab, ad, af, aa, ac, ae, ag;
                if (vec3d.x == 0.0 && vec3d.z == 0.0) {
                    Sprite sprite = sprites[0];
                    z = sprite.getFrameU(0.0F);
                    aa = sprite.getFrameV(0.0F);
                    ab = z;
                    ac = sprite.getFrameV(1.0F);
                    ad = sprite.getFrameU(1.0F);
                    ae = ac;
                    af = ad;
                    ag = aa;
                } else {
                    Sprite sprite = sprites[1];
                    float ah = (float) MathHelper.atan2(vec3d.z, vec3d.x) - (float) (Math.PI / 2);
                    float ai = MathHelper.sin(ah) * 0.25F;
                    float aj = MathHelper.cos(ah) * 0.25F;
                    float ak = 0.5F;
                    z = sprite.getFrameU(0.5F + (-aj - ai));
                    aa = sprite.getFrameV(0.5F + (-aj + ai));
                    ab = sprite.getFrameU(0.5F + (-aj + ai));
                    ac = sprite.getFrameV(0.5F + (aj + ai));
                    ad = sprite.getFrameU(0.5F + (aj + ai));
                    ae = sprite.getFrameV(0.5F + (aj - ai));
                    af = sprite.getFrameU(0.5F + (aj - ai));
                    ag = sprite.getFrameV(0.5F + (-aj - ai));
                }

                float al = (z + ab + ad + af) / 4.0F;
                float ah = (aa + ac + ae + ag) / 4.0F;
                float ai = sprites[0].getContents().getWidth() / (sprites[0].getMaxU() - sprites[0].getMinU());
                float aj = sprites[0].getContents().getHeight() / (sprites[0].getMaxV() - sprites[0].getMinV());
                float ak = 4.0F / Math.max(aj, ai);
                z = MathHelper.lerp(ak, z, al);
                ab = MathHelper.lerp(ak, ab, al);
                ad = MathHelper.lerp(ak, ad, al);
                af = MathHelper.lerp(ak, af, al);
                aa = MathHelper.lerp(ak, aa, ah);
                ac = MathHelper.lerp(ak, ac, ah);
                ae = MathHelper.lerp(ak, ae, ah);
                ag = MathHelper.lerp(ak, ag, ah);
                int am = getLight(world, pos);
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
                this.vertex(vertexConsumer, chunkX + 1.0, chunkY + yOffset + 1, chunkZ, ac, ae, ag, z, af, aq);
                this.vertex(vertexConsumer, chunkX, chunkY + yOffset + 1, chunkZ, ac, ae, ag, ab, af, aq);
                this.vertex(vertexConsumer, chunkX, chunkY + yOffset + 1, chunkZ + 1.0, ac, ae, ag, ab, ad, aq);
                this.vertex(vertexConsumer, chunkX + 1.0, chunkY + yOffset + 1, chunkZ + 1.0, ac, ae, ag, z, ad, aq);
            }

            int light = this.getLight(world, pos);

            for (Direction direction : Direction.Type.HORIZONTAL) {
                float c1, c2;
                double x1, z1, x2, z2;
                boolean shouldRender;
                switch (direction) { // Handles how each side should look when rendering
                    case NORTH:
                        c1 = northWestHeight;
                        c2 = northEastHeight;
                        x1 = chunkX;
                        x2 = chunkX + 1.0;
                        z1 = chunkZ + EPSILON;
                        z2 = chunkZ + EPSILON;
                        shouldRender = renderNorthSide;
                        break;
                    case SOUTH:
                        c1 = southEastHeight;
                        c2 = southWestHeight;
                        x1 = chunkX + 1.0;
                        x2 = chunkX;
                        z1 = chunkZ + 1.0 - EPSILON;
                        z2 = chunkZ + 1.0 - EPSILON;
                        shouldRender = renderSouthSide;
                        break;
                    case WEST:
                        c1 = southWestHeight;
                        c2 = northWestHeight;
                        x1 = chunkX + EPSILON;
                        x2 = chunkX + EPSILON;
                        z1 = chunkZ + 1.0;
                        z2 = chunkZ;
                        shouldRender = renderWestSide;
                        break;
                    default:
                        c1 = northEastHeight;
                        c2 = southEastHeight;
                        x1 = chunkX + 1.0 - EPSILON;
                        x2 = chunkX + 1.0 - EPSILON;
                        z1 = chunkZ;
                        z2 = chunkZ + 1.0;
                        shouldRender = renderEastSide;
                }

                if (shouldRender && !isSideCovered(world, pos, direction, Math.max(c1, c2), world.getBlockState(pos.offset(direction)))) {
                    BlockPos blockPos = pos.offset(direction);
                    Sprite sprite2 = sprites[1];
                    if (!isInLava) {
                        Block block = world.getBlockState(blockPos).getBlock();
                        if (block instanceof TransparentBlock || block instanceof LeavesBlock) {
                            sprite2 = getFluidSprites(world, blockPos, fluidState)[2];
                        }
                    }

                    float startU = sprite2.getFrameU(0.0F);
                    float endV = sprite2.getFrameV((1.0F - c1) * 0.5F);

                    float ap = sprite2.getFrameU(0.5F);
                    float ax = sprite2.getFrameV((1.0F - c2) * 0.5F);
                    float ay = sprite2.getFrameV(0.5F);
                    float directionBrightness = direction.getAxis() == Direction.Axis.Z ? northBrightness : westBrightness;
                    float red = upBrightness * directionBrightness * r;
                    float blue = upBrightness * directionBrightness * g;
                    float green = upBrightness * directionBrightness * b;

                    this.vertex(vertexConsumer, x2, chunkY + 1 - c2, z2, red, blue, green, ap, ax, light);
                    this.vertex(vertexConsumer, x1, chunkY + 1 - c1, z1, red, blue, green, startU, endV, light);
                    this.vertex(vertexConsumer, x1, chunkY + 1 - yOffset, z1, red, blue, green, startU, ay, light);
                    this.vertex(vertexConsumer, x2, chunkY + 1 - yOffset, z2, red, blue, green, ap, ay, light);
                    if (getFluidSprites(world, blockPos, fluidState).length == 3 && sprite2 != getFluidSprites(world, blockPos, fluidState)[2]) { // Render overlay (inside of the fluid)
                        this.vertex(vertexConsumer, x2, chunkY + 1 - yOffset, z2, red, blue, green, startU, ay, light);
                        this.vertex(vertexConsumer, x1, chunkY + 1 - yOffset, z1, red, blue, green, ap, ay, light);
                        this.vertex(vertexConsumer, x1, chunkY + 1 - c1, z1, red, blue, green, ap, ax, light);
                        this.vertex(vertexConsumer, x2, chunkY + 1 - c2, z2, red, blue, green, startU, endV, light);
                    }
                }
            }
        }
    }

    @Override
    public FluidBakedModel getFluidModel() {
        return this.model;
    }

    public static int getLight(BlockRenderView world, BlockPos pos) {
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

    public static boolean isSideCovered(BlockView blockView, BlockPos blockPos, Direction direction, float maxDeviation, BlockState blockState) {
        return isSideCovered(blockView, direction, maxDeviation, blockPos.offset(direction), blockState);
    }

    private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state);
    }

}
