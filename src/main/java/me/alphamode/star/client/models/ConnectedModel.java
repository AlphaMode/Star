package me.alphamode.star.client.models;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

public class ConnectedModel extends ForwardingBakedModel {
    public ConnectedModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        ConnectionManager manager = new ConnectionManager(blockView, state, pos);
        context.pushTransform(quad -> {
                SpriteAtlasTexture atlas = MinecraftClient.getInstance().getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
                Sprite connectedSprite = CTModelRegistry.getCTSprite(state.getBlock());
                Sprite original = SpriteFinder.get(atlas).find(quad, 0);
                for (int vertex = 0; vertex < 4; vertex++) {
                    int index = manager.getTextureIndex(quad.lightFace());
                    quad.sprite(vertex, 0, getTargetU(original, connectedSprite, quad.spriteU(vertex, 0), index), getTargetV(original, connectedSprite, quad.spriteV(vertex, 0), index));
                }
            return true;
        });
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();

    }

    public float getTargetU(Sprite original, Sprite connected, float localU, int index) {
        float uOffset = (index % 8);
        return connected.getFrameU(
                (getUnInterpolatedU(original, localU) + (uOffset * 16)) / ((float) 8));
    }

    public float getTargetV(Sprite original, Sprite connected, float localV, int index) {
        float vOffset = (index / 8);
        return connected.getFrameV(
                (getUnInterpolatedV(original, localV) + (vOffset * 16)) / ((float) 8));
    }

    public static float getUnInterpolatedU(Sprite sprite, float u) {
        float f = sprite.getMaxU() - sprite.getMinU();
        return (u - sprite.getMinU()) / f * 16.0F;
    }

    public static float getUnInterpolatedV(Sprite sprite, float v) {
        float f = sprite.getMaxV() - sprite.getMinV();
        return (v - sprite.getMinV()) / f * 16.0F;
    }
}
