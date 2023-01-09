package me.alphamode.star.mixin.common;

import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.EntityExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements EntityExtension {
    public ItemEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    private void applyUpsideDownBuoyancy() {
        Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(vec3d.x * 0.99F, vec3d.y - (double)(vec3d.y < 0.06F ? 5.0E-4F : 0.0F), vec3d.z * 0.99F);
    }

//    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight()F"))
//    public void star$upsideDownFluidItems(CallbackInfo ci) {
//        float f = this.getEyeHeight() - 0.11111111F;
//        if (this.isTouchingUpsideDownFluid() && this.getFluidHeight(StarTags.Fluids.UPSIDE_DOWN_FLUID) > (double)f) {
//            applyUpsideDownBuoyancy();
//        }
//    }
}
