package com.yipeekiyaay.kitchen_sink;

import com.yipeekiyaay.kitchen_sink.client.gui.SlotlessScreen;
import com.yipeekiyaay.kitchen_sink.network.KitchenSinkNetworking;
import com.yipeekiyaay.kitchen_sink.registry.ModRegistries;
import dev.architectury.registry.menu.MenuRegistry;

public class KitchenSinkModClient {
    public static void initClient() {
        KitchenSinkNetworking.initClient();

        MenuRegistry.registerScreenFactory(
                ModRegistries.SLOTLESS_BARREL_SCREEN_HANDLER.get(),
                SlotlessScreen::new
        );
    }
}
