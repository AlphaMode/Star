package me.alphamode.star.extensions;

public interface EntityExtension {
    default boolean isTouchingUpsideDownFluid() {
        return false;
    }

    default void checkUpsideDownState() {}
}
