package me.alphamode.star.mixin.common;

import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.EntityExtension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    private void applyUpsideDownBuoyancy() {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x * 0.99F, vec3d.y - (double)(vec3d.y < 0.06F ? 5.0E-4F : 0.0F), vec3d.z * 0.99F);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getStandingEyeHeight()F"))
    public void star$upsideDownFluidItems(CallbackInfo ci) {
        float f = this.getStandingEyeHeight() - 0.11111111F;
        if (this.isTouchingUpsideDownFluid() && this.getFluidHeight(StarTags.Fluids.UPSIDE_DOWN_FLUID) > (double)f) {
            applyUpsideDownBuoyancy();
        }
    }
}
