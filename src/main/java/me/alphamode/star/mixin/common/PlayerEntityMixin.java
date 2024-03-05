package me.alphamode.star.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.alphamode.star.world.fluids.StarFluid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {
    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "increaseTravelMotionStats", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWater()Z"))
    private boolean starFluidStepSound(boolean original) {
        if (isTouchingStarFluid())
            return false;
        return original;
    }

    @WrapOperation(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;playSwimSound()V"))
    private void starPlayFluidSwimSound(PlayerEntity instance, Operation<Void> vanillaCallback) {
        var touching = getStarTouchingFluid();
        if (touching != null && touching.getFluid() instanceof StarFluid starFluid) {
            starFluid.playSwimSound(instance, vanillaCallback);
        } else
            vanillaCallback.call(instance);
    }
}
