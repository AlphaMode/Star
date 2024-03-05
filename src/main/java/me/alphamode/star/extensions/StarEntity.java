package me.alphamode.star.extensions;

import net.minecraft.fluid.FluidState;
import org.jetbrains.annotations.Nullable;

public interface StarEntity {
    default boolean isTouchingStarFluid() {
        return false;
    }

    default boolean isSubmergedInStarFluid() {
        return false;
    }

    default void checkStarFluidState() {}

    @Nullable
    default FluidState getStarTouchingFluid() {
        throw new RuntimeException();
    }

    default boolean star$getSubmergedStarState() {
        return false;
    }
}
