package me.alphamode.star.mixin.indium;

import link.infra.indium.renderer.render.ChunkRenderInfo;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(ChunkRenderInfo.class)
public interface ChunkRenderInfoAccessor {
    @Accessor
    BlockRenderView getBlockView();

    @Accessor
    boolean isDidOutput();

    @Accessor
    void setDidOutput(boolean didOutput);
}
