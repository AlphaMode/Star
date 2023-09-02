package me.alphamode.star.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker
    SoundEvent callGetSplashSound();

    @Invoker
    SoundEvent callGetHighSpeedSplashSound();
}
