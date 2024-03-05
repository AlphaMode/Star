package me.alphamode.star.mixin.common;

import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.StarEntity;
import me.alphamode.star.world.fluids.StarFluid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemStarEntityMixin extends Entity implements StarEntity {
    public ItemStarEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getStandingEyeHeight()F"))
    public void star$upsideDownFluidItems(CallbackInfo ci) {
        float f = this.getStandingEyeHeight() - 0.11111111F;
        if (this.isTouchingStarFluid() && this.getFluidHeight(StarTags.Fluids.STAR_FLUID) > (double)f) {
            ((StarFluid)getStarTouchingFluid().getFluid()).applyItemBuoyancy(this);
        }
    }
}
