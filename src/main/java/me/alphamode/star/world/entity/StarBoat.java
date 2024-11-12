package me.alphamode.star.world.entity;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;

public interface StarBoat {
    default boolean isSubmergedInFluid(FluidState state) {
        return ((BoatEntity) this).isSubmergedInWater();
    }
}
