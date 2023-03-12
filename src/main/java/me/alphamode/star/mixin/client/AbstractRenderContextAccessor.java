package me.alphamode.star.mixin.client;

import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractRenderContext")
public interface AbstractRenderContextAccessor {
    @Accessor
    void setMatrix(Matrix4f matrix);

    @Accessor
    void setNormalMatrix(Matrix3f normalMatrix);
}
