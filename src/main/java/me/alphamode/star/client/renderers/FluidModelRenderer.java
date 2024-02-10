package me.alphamode.star.client.renderers;

import me.alphamode.star.client.models.FluidBakedModel;
import me.alphamode.star.client.models.UpsideDownFluidModel;
import me.alphamode.star.extensions.fabric.FluidRenderHandlerExtension;
import me.alphamode.star.extensions.fabric.TerrainRenderContextExtension;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRendererRegion;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.*;
import static net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.WATER_OVERLAY;

public class FluidModelRenderer implements FluidRenderHandler, FluidRenderHandlerExtension {
    private final FluidBakedModel model;
    protected final Supplier<Identifier> stillGetter, flowingGetter, overlayGetter;
    protected final Sprite[] sprites;

    protected final int tint;

    public FluidModelRenderer(FluidBakedModel model, Supplier<Identifier> stillTexture, Supplier<Identifier> flowingTexture, Supplier<Identifier> overlayTexture, int tint) {
        this.stillGetter = stillTexture;
        this.flowingGetter = flowingTexture;
        this.overlayGetter = overlayTexture;
        this.sprites = new Sprite[overlayTexture == null ? 2 : 3];
        this.tint = tint;
        this.model = model;
    }

    public FluidModelRenderer(FluidBakedModel model, Identifier stillTexture, Identifier flowingTexture, int tint) {
        this(model, () -> stillTexture, () -> flowingTexture, null, tint);
    }

    public FluidModelRenderer(FluidBakedModel model, Identifier stillTexture, Identifier flowingTexture, Identifier overlayTexture) {
        this(model, () -> stillTexture, () -> flowingTexture, () -> overlayTexture, -1);
    }

    public FluidModelRenderer(FluidBakedModel model, Identifier stillTexture, Identifier flowingTexture) {
        this(model, () -> stillTexture, () -> flowingTexture, null, -1);
    }

    public FluidModelRenderer(FluidBakedModel model, int tint) {
        this(model, () -> WATER_STILL, () -> WATER_FLOWING, () -> WATER_OVERLAY, tint);
    }

    public FluidModelRenderer(FluidBakedModel model) {
        this(model, WATER_STILL, WATER_FLOWING, WATER_OVERLAY);
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
        MatrixStack stack = new MatrixStack();
        stack.push();
        stack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

        ((TerrainRenderContextExtension) ((AccessChunkRendererRegion) world).fabric_getRenderer()).tessellateFluid(blockState, fluidState, pos, model, stack);
        stack.pop();
    }

    @Override
    public FluidBakedModel getFluidModel() {
        return this.model;
    }
}
