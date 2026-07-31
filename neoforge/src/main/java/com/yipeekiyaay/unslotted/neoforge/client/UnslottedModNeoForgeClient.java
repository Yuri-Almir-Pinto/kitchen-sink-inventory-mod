package com.yipeekiyaay.unslotted.neoforge.client;

import com.yipeekiyaay.unslotted.client.gui.SlotlessScreen;
import com.yipeekiyaay.unslotted.network.UnslottedNetworking;
import com.yipeekiyaay.unslotted.registry.ModRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public class UnslottedModNeoForgeClient {
    public static void init(IEventBus modEventBus) {
        // NeoForge being an absolute ass (Registering using the KitchenSinkModClient method does not work because
        // it misses timing in NeoForge for some reason. Either I'm dumb or it's bugged)
        UnslottedNetworking.initClient();

        modEventBus.addListener((RegisterMenuScreensEvent event) -> event.register(
                ModRegistries.SLOTLESS_SCREEN_HANDLER.get(),
                SlotlessScreen::new
        ));
    }
}