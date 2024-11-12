package me.alphamode.star.mixin.common;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.alphamode.star.data.StarTags;
import me.alphamode.star.extensions.StarEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemStarEntityMixin extends Entity implements StarEntity {
    public ItemStarEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    private void applyUpsideDownBuoyancy() {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x * 0.99F, vec3d.y - (double)(vec3d.y < 0.06F ? 5.0E-4F : 0.0F), vec3d.z * 0.99F);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;applyGravity()V"))
    public boolean star$upsideDownFluidItems(ItemEntity instance) {
        if (this.isTouchingUpsideDownFluid() && this.getFluidHeight(StarTags.Fluids.STAR_FLUID) > 0.1F) {
            applyUpsideDownBuoyancy();
            return false;
        }
        return true;
    }
}
