package com.yipeekiyaay.kitchen_sink.fabric;

import net.fabricmc.api.ModInitializer;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;

public final class KitchenSinkModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        KitchenSinkMod.init();
    }
}
