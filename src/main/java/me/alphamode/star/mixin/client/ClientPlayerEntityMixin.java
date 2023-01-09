package me.alphamode.star.mixin.client;

import com.mojang.authlib.GameProfile;
import me.alphamode.star.extensions.EntityExtension;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends Player implements EntityExtension {

    @Shadow public Input input;

    public ClientPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile profile, @Nullable ProfilePublicKey publicKey) {
        super(world, pos, yaw, profile, publicKey);
    }

//    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
//    public void star$knockUpwards(CallbackInfo ci) {
//        if (this.isTouchingUpsideDownFluid() && this.input.shiftKeyDown && this.isAffectedByFluids())
//            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04f, 0.0));
//    }
}
