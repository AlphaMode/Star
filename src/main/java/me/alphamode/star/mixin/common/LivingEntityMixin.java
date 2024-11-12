package me.alphamode.star.mixin.common;

import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.StarEntity;
import me.alphamode.star.world.fluids.StarFluid;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements StarEntity {
    @Shadow protected abstract boolean shouldSwimInFluids();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "fall", at = @At("HEAD"))
    public void star$fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition, CallbackInfo ci) {
        if (!this.isTouchingUpsideDownFluid()) {
            this.checkUpsideDownState();
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isTouchingWater()Z"))
    public void star$swim(CallbackInfo ci) {
        FluidState fluid = getTouchingFluid();
        if (fluid == null)
            return;
        double fluidHeight = this.getFluidHeight(StarTags.Fluids.STAR_FLUID);

        boolean touchingFluid = fluid.getFluid() instanceof StarFluid && fluidHeight > 0.0;
        double l = this.getSwimHeight();
        if (touchingFluid || (fluidHeight > l)) {
            ((StarFluid)fluid.getFluid()).swim((LivingEntity) (Object) this);
        }
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void star$fluidMovement(Vec3d movementInput, CallbackInfo ci, double gravity, boolean falling) {
        FluidState fluidState = getTouchingFluid();
        if (fluidState == null)
            return;
        if (fluidState.getFluid() instanceof StarFluid fluid && this.shouldSwimInFluids() && !fluid.canWalkOn((LivingEntity) (Object) this, movementInput, fluidState)) {
            fluid.travelInFluid((LivingEntity) (Object) this, movementInput, gravity, falling);
        }
    }

    @Inject(method = "canBreatheInWater", at = @At("RETURN"), cancellable = true)
    private void checkIfCustomFluidIsBreathable(CallbackInfoReturnable<Boolean> cir) {
        var state = getTouchingFluid();
        if (state != null && state.getFluid() instanceof StarFluid fluid && fluid.canBreathe((LivingEntity) (Object) this))
            cir.setReturnValue(true);
    }
}
