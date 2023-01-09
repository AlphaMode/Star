package me.alphamode.star.client.renderers;

import me.alphamode.star.world.fluids.DirectionalFluid;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import static net.minecraft.client.renderer.block.LiquidBlockRenderer.isNeighborSameFluid;
import static net.minecraft.client.renderer.block.LiquidBlockRenderer.shouldRenderFace;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * A Modified version of vanilla's fluid renderer to support
 * upside-down fluid rendering
 */
public class UpsideDownFluidRenderer {

    public void renderFluid(BlockPos pos, BlockAndTintGetter world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        DirectionalFluid fluid = (DirectionalFluid) fluidState.getType();
        boolean isInLava = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] sprites = ForgeHooksClient.getFluidSprites(world, pos, fluidState);;
        int fluidColor = IClientFluidTypeExtensions.of(fluidState).getTintColor(fluidState, world, pos);
        float r = (fluidColor >> 16 & 0xFF) / 255.0F;
        float g = (fluidColor >> 8 & 0xFF) / 255.0F;
        float b = (fluidColor & 0xFF) / 255.0F;
        BlockState downState = world.getBlockState(pos.relative(Direction.DOWN));
        FluidState downFluidState = downState.getFluidState();
        BlockState upState = world.getBlockState(pos.relative(Direction.UP));
        FluidState upFluidState = upState.getFluidState();
        BlockState northState = world.getBlockState(pos.relative(Direction.NORTH));
        FluidState northFluidState = northState.getFluidState();
        BlockState southState = world.getBlockState(pos.relative(Direction.SOUTH));
        FluidState southFluidState = southState.getFluidState();
        BlockState westState = world.getBlockState(pos.relative(Direction.WEST));
        FluidState westFluidState = westState.getFluidState();
        BlockState eastState = world.getBlockState(pos.relative(Direction.EAST));
        FluidState eastFluidState = eastState.getFluidState();
        boolean isFluidTheSame = !isNeighborSameFluid(fluidState, downFluidState);
        boolean renderBottomSide = shouldRenderFace(world, pos, fluidState, blockState, fluid.getFlowDirection(), upFluidState)
                && !isSideCovered(world, pos, fluid.getFlowDirection(), 0.8888889F, upState);
        boolean renderNorthSide = shouldRenderFace(world, pos, fluidState, blockState, Direction.NORTH, northFluidState);
        boolean renderSouthSide = shouldRenderFace(world, pos, fluidState, blockState, Direction.SOUTH, southFluidState);
        boolean renderWestSide = shouldRenderFace(world, pos, fluidState, blockState, Direction.WEST, westFluidState);
        boolean renderEastSide = shouldRenderFace(world, pos, fluidState, blockState, Direction.EAST, eastFluidState);
        if (isFluidTheSame || renderBottomSide || renderEastSide || renderWestSide || renderNorthSide || renderSouthSide) {
            float downBrightness = world.getShade(Direction.DOWN, true);
            float upBrightness = world.getShade(Direction.UP, true);
            float northBrightness = world.getShade(Direction.NORTH, true);
            float westBrightness = world.getShade(Direction.WEST, true);
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
                northEastHeight = this.getHeightToRenderFluid(world, fluid, fluidHeight, s, u, pos.relative(Direction.NORTH).relative(Direction.EAST));
                northWestHeight = this.getHeightToRenderFluid(world, fluid, fluidHeight, s, v, pos.relative(Direction.NORTH).relative(Direction.WEST));
                southEastHeight = this.getHeightToRenderFluid(world, fluid, fluidHeight, t, u, pos.relative(Direction.SOUTH).relative(Direction.EAST));
                southWestHeight = this.getHeightToRenderFluid(world, fluid, fluidHeight, t, v, pos.relative(Direction.SOUTH).relative(Direction.WEST));
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
                Vec3 vec3d = fluidState.getFlow(world, pos);
                float z, ab, ad, af, aa, ac, ae, ag;
                if (vec3d.x == 0.0 && vec3d.z == 0.0) {
                    TextureAtlasSprite sprite = sprites[0];
                    z = sprite.getU(0.0);
                    aa = sprite.getV(0.0);
                    ab = z;
                    ac = sprite.getV(16.0);
                    ad = sprite.getU(16.0);
                    ae = ac;
                    af = ad;
                    ag = aa;
                } else {
                    TextureAtlasSprite sprite = sprites[1];
                    float ah = (float) Mth.atan2(vec3d.z, vec3d.x) - (float) (Math.PI / 2);
                    float ai = Mth.sin(ah) * 0.25F;
                    float aj = Mth.cos(ah) * 0.25F;
                    float ak = 8.0F;
                    z = sprite.getU(8.0F + (-aj - ai) * 16.0F);
                    aa = sprite.getV(8.0F + (-aj + ai) * 16.0F);
                    ab = sprite.getU(8.0F + (-aj + ai) * 16.0F);
                    ac = sprite.getV(8.0F + (aj + ai) * 16.0F);
                    ad = sprite.getU(8.0F + (aj + ai) * 16.0F);
                    ae = sprite.getV(8.0F + (aj - ai) * 16.0F);
                    af = sprite.getU(8.0F + (aj - ai) * 16.0F);
                    ag = sprite.getV(8.0F + (-aj - ai) * 16.0F);
                }

                float al = (z + ab + ad + af) / 4.0F;
                float ah = (aa + ac + ae + ag) / 4.0F;
                float ai = sprites[0].getWidth() / (sprites[0].getU1() - sprites[0].getU0());
                float aj = sprites[0].getHeight() / (sprites[0].getV1() - sprites[0].getV0());
                float ak = 4.0F / Math.max(aj, ai);
                z = Mth.lerp(ak, z, al);
                ab = Mth.lerp(ak, ab, al);
                ad = Mth.lerp(ak, ad, al);
                af = Mth.lerp(ak, af, al);
                aa = Mth.lerp(ak, aa, ah);
                ac = Mth.lerp(ak, ac, ah);
                ae = Mth.lerp(ak, ae, ah);
                ag = Mth.lerp(ak, ag, ah);
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
                float z = sprites[0].getU0();
                float ab = sprites[0].getU1();
                float ad = sprites[0].getV0();
                float af = sprites[0].getV1();
                int aq = this.getLight(world, pos.relative(fluid.getFlowDirection()));
                float ac = downBrightness * r;
                float ae = downBrightness * g;
                float ag = downBrightness * b;
                this.vertex(vertexConsumer, chunkX + 1.0, chunkY + yOffset + 1, chunkZ, ac, ae, ag, z, af, aq);
                this.vertex(vertexConsumer, chunkX, chunkY + yOffset + 1, chunkZ, ac, ae, ag, ab, af, aq);
                this.vertex(vertexConsumer, chunkX, chunkY + yOffset + 1, chunkZ + 1.0, ac, ae, ag, ab, ad, aq);
                this.vertex(vertexConsumer, chunkX + 1.0, chunkY + yOffset + 1, chunkZ + 1.0, ac, ae, ag, z, ad, aq);
            }

            int light = this.getLight(world, pos);

            for (Direction direction : Direction.Plane.HORIZONTAL) {
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

                if (shouldRender && !isSideCovered(world, pos, direction, Math.max(sideY, endSideY), world.getBlockState(pos.relative(direction)))) {
                    BlockPos blockPos = pos.relative(direction);
                    TextureAtlasSprite overlay = sprites[1];
                    if (!isInLava) {
                        if (sprites[2] != null && world.getBlockState(blockPos).shouldDisplayFluidOverlay(world, blockPos, fluidState)) {
                            overlay = sprites[2];
                        }
                    }

                    float startU = overlay.getU(0.0);
                    float endV = overlay.getV((1.0F - sideY) * 16.0F * 0.5F);

                    float ap = overlay.getU(8.0);
                    float ax = overlay.getV((1.0F - endSideY) * 16.0F * 0.5F);
                    float ay = overlay.getV(8.0);
                    float directionBrightness = direction.getAxis() == Direction.Axis.Z ? northBrightness : westBrightness;
                    float red = upBrightness * directionBrightness * r;
                    float blue = upBrightness * directionBrightness * g;
                    float green = upBrightness * directionBrightness * b;

                    this.vertex(vertexConsumer, endX, chunkY + 1 - endSideY, endZ, red, blue, green, ap, ax, light);
                    this.vertex(vertexConsumer, startX, chunkY + 1 - sideY, startZ, red, blue, green, startU, endV, light);
                    this.vertex(vertexConsumer, startX, chunkY + 1 - yOffset, startZ, red, blue, green, startU, ay, light);
                    this.vertex(vertexConsumer, endX, chunkY + 1 - yOffset, endZ, red, blue, green, ap, ay, light);
                    if (overlay != ModelBakery.WATER_OVERLAY.sprite()) { // Render overlay (inside of the fluid)
                        this.vertex(vertexConsumer, endX, chunkY + 1 - yOffset, endZ, red, blue, green, startU, ay, light);
                        this.vertex(vertexConsumer, startX, chunkY + 1 - yOffset, startZ, red, blue, green, ap, ay, light);
                        this.vertex(vertexConsumer, startX, chunkY + 1 - sideY, startZ, red, blue, green, ap, ax, light);
                        this.vertex(vertexConsumer, endX, chunkY + 1 - endSideY, endZ, red, blue, green, startU, endV, light);
                    }
                }
            }
        }
    }

    private float getFluidHeight(BlockAndTintGetter blockRenderView, DirectionalFluid fluid, BlockPos blockPos) {
        BlockState blockState = blockRenderView.getBlockState(blockPos);
        return this.getFluidHeight(blockRenderView, fluid, blockPos, blockState, blockState.getFluidState());
    }

    private float getHeightToRenderFluid(BlockAndTintGetter blockRenderView, DirectionalFluid fluid, float f, float g, float h, BlockPos blockPos) {
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

    private float getFluidHeight(BlockAndTintGetter blockRenderView, DirectionalFluid fluid, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (fluid.isSame(fluidState.getType())) {
            BlockState blockState2 = blockRenderView.getBlockState(blockPos.relative(fluid.getFlowDirection().getOpposite()));
            return fluid.isSame(blockState2.getFluidState().getType()) ? 1.0F : fluidState.getOwnHeight();
        } else {
            return !blockState.getMaterial().isSolid() ? 0.0F : -1.0F;
        }
    }

    private int getLight(BlockAndTintGetter world, BlockPos pos) {
        int i = LevelRenderer.getLightColor(world, pos);
        int j = LevelRenderer.getLightColor(world, pos.above());
        int k = i & (LightTexture.FULL_BLOCK | 15);
        int l = j & (LightTexture.FULL_BLOCK | 15);
        int m = i >> 16 & (LightTexture.FULL_BLOCK | 15);
        int n = j >> 16 & (LightTexture.FULL_BLOCK | 15);
        return (k > l ? k : l) | (m > n ? m : n) << 16;
    }

    private void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, 1.0F).uv(u, v).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private static boolean isSideCovered(BlockGetter world, Direction direction, float f, BlockPos pos, BlockState state) {
        if (state.canOcclude()) {
            VoxelShape voxelShape = Shapes.box(0.0, 0.0, 0.0, 1.0, f, 1.0);
            VoxelShape voxelShape2 = state.getOcclusionShape(world, pos);
            return Shapes.blockOccudes(voxelShape, voxelShape2, direction);
        } else {
            return false;
        }
    }

    private static boolean isSideCovered(BlockGetter blockView, BlockPos blockPos, Direction direction, float maxDeviation, BlockState blockState) {
        return isSideCovered(blockView, direction, maxDeviation, blockPos.relative(direction), blockState);
    }

    private static boolean isOppositeSideCovered(BlockGetter world, BlockPos pos, BlockState state, Direction direction) {
        return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state);
    }

}
