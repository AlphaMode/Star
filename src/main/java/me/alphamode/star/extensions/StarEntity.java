package me.alphamode.star.extensions;

import net.minecraft.fluid.FluidState;

public interface StarEntity {
    default boolean isTouchingUpsideDownFluid() {
        return false;
    }

    default void checkUpsideDownState() {}

    default FluidState getTouchingFluid() {
        throw new RuntimeException();
    }
}
