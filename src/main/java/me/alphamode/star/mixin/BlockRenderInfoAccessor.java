package me.alphamode.star.mixin;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockRenderInfo.class)
public interface BlockRenderInfoAccessor {
    @Accessor
    void setRecomputeSeed(boolean recomputeSeed);
}
