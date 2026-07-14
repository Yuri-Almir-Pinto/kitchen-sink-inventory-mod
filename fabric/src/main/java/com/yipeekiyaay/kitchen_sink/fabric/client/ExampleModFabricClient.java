package com.yipeekiyaay.kitchen_sink.fabric.client;

import com.yipeekiyaay.kitchen_sink.KitchenSinkModClient;
import net.fabricmc.api.ClientModInitializer;

public final class ExampleModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KitchenSinkModClient.initClient();
    }
}
