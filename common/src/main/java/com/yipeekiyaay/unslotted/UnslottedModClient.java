package com.yipeekiyaay.unslotted;

import com.yipeekiyaay.unslotted.client.gui.SlotlessScreen;
import com.yipeekiyaay.unslotted.network.UnslottedNetworking;
import com.yipeekiyaay.unslotted.registry.ModRegistries;
import dev.architectury.registry.menu.MenuRegistry;

public class UnslottedModClient {
    public static void initClient() {
        UnslottedNetworking.initClient();

        MenuRegistry.registerScreenFactory(
                ModRegistries.SLOTLESS_SCREEN_HANDLER.get(),
                SlotlessScreen::new
        );
    }
}
