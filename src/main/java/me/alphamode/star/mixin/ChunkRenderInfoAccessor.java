package me.alphamode.star.mixin;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.ChunkRenderInfo;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkRenderInfo.class)
public interface ChunkRenderInfoAccessor {
    @Accessor
    BlockRenderView getBlockView();
}
