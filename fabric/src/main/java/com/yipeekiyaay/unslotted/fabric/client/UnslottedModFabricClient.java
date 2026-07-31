package com.yipeekiyaay.unslotted.fabric.client;

import com.yipeekiyaay.unslotted.UnslottedModClient;
import net.fabricmc.api.ClientModInitializer;

public final class UnslottedModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UnslottedModClient.initClient();
    }
}
