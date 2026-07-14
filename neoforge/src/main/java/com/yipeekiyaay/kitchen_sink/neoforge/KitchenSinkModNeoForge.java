package com.yipeekiyaay.kitchen_sink.neoforge;

import com.yipeekiyaay.kitchen_sink.KitchenSinkModClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(KitchenSinkMod.MOD_ID)
public final class KitchenSinkModNeoForge {
    public KitchenSinkModNeoForge(IEventBus modEventBus) {
        KitchenSinkMod.init();

        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(KitchenSinkModClient::initClient);
    }
}
