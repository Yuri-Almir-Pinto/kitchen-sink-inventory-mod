package com.yipeekiyaay.unslotted.neoforge;

import com.yipeekiyaay.unslotted.neoforge.client.UnslottedModNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.yipeekiyaay.unslotted.UnslottedMod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(UnslottedMod.MOD_ID)
public final class UnslottedModNeoForge {
    public UnslottedModNeoForge(IEventBus modEventBus) {
        UnslottedMod.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            UnslottedModNeoForgeClient.init(modEventBus);
        }
    }
}
