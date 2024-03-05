package me.alphamode.star.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import me.alphamode.star.extensions.StarEntity;
import me.alphamode.star.world.fluids.StarFluid;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements StarEntity {

    @Shadow public Input input;

    @Shadow private int underwaterVisibilityTicks;

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isFallFlying()Z"))
    public void star$knockUpwards(CallbackInfo ci) {
        if (isTouchingStarFluid() && this.input.sneaking && shouldSwimInFluids())
            ((StarFluid) getStarTouchingFluid().getFluid()).knockInFlowDirection(this);

        double breathingDistance = getEyeY() - Entity.field_29991;


        BlockPos blockPos = BlockPos.ofFloored(getX(), breathingDistance, getZ());
        FluidState fluidState = this.getWorld().getFluidState(blockPos);

        if (fluidState.getFluid() instanceof StarFluid fluid)
            this.underwaterVisibilityTicks = fluid.calculateSubmergedVisibility(this, this.underwaterVisibilityTicks);
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSubmergedInWater()Z"))
    private boolean starSubmergedCheck(boolean original) {
        return original || isSubmergedInStarFluid();
    }
    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isTouchingWater()Z"))
    private boolean starWaterCheck(boolean original) {
        return original || isTouchingStarFluid();
    }


    @Inject(method = "getUnderwaterVisibility", at = @At("HEAD"), cancellable = true)
    private void getStarFluidVis(CallbackInfoReturnable<Float> cir) {
        var touching = getStarTouchingFluid();
        if (touching != null && touching.getFluid() instanceof StarFluid starFluid) {
            cir.setReturnValue(starFluid.getSubmergedVisibility((ClientPlayerEntity) (Object) this));
        }
    }

    @ModifyExpressionValue(method = "isWalking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSubmergedInWater()Z"))
    private boolean isInStarLiquid(boolean original) {
        return original || isSubmergedInStarFluid();
    }

    @Override
    public boolean isSubmergedInStarFluid() {
        return star$getSubmergedStarState();
    }
}
