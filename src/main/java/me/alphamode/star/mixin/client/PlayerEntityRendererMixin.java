package me.alphamode.star.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isTouchingWater()Z"))
    private boolean starFluidStepSound(boolean original, AbstractClientPlayerEntity abstractClientPlayerEntity) {
        return original || abstractClientPlayerEntity.isTouchingStarFluid();
    }
}
