package com.yipeekiyaay.kitchen_sink.neoforge;

import com.yipeekiyaay.kitchen_sink.neoforge.client.KitchenSinkModNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(KitchenSinkMod.MOD_ID)
public final class KitchenSinkModNeoForge {
    public KitchenSinkModNeoForge(IEventBus modEventBus) {
        KitchenSinkMod.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            KitchenSinkModNeoForgeClient.init(modEventBus);
        }
    }
}
