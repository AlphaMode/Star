package me.alphamode.star.mixin.client;

import com.mojang.authlib.GameProfile;
import me.alphamode.star.extensions.EntityExtension;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements EntityExtension {

    @Shadow public Input input;

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isFallFlying()Z"))
    public void star$knockUpwards(CallbackInfo ci) {
        if (this.isTouchingUpsideDownFluid() && this.input.sneaking && this.shouldSwimInFluids())
            this.setVelocity(this.getVelocity().add(0.0, 0.04f, 0.0));
    }
}
