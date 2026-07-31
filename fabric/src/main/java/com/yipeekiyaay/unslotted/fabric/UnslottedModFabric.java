package com.yipeekiyaay.unslotted.fabric;

import net.fabricmc.api.ModInitializer;

import com.yipeekiyaay.unslotted.UnslottedMod;

public final class UnslottedModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        UnslottedMod.init();
    }
}
