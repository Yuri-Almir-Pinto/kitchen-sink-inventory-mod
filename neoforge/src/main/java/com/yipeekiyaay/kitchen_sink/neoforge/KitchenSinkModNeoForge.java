package com.yipeekiyaay.kitchen_sink.neoforge;

import net.neoforged.fml.common.Mod;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;

@Mod(KitchenSinkMod.MOD_ID)
public final class KitchenSinkModNeoForge {
    public KitchenSinkModNeoForge() {
        // Run our common setup.
        KitchenSinkMod.init();
    }
}
