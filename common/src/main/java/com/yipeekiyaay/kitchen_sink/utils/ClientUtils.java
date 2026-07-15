package com.yipeekiyaay.kitchen_sink.utils;

import net.minecraft.client.MinecraftClient;

import java.util.Objects;

public class ClientUtils {
    public static MinecraftClient client = MinecraftClient.getInstance();

    public static MinecraftClient getClient() {
        Objects.requireNonNull(client, "getClient was called while client was not initialized yet!");

        return client;
    }

    public static int getScaledMouseX() {
        Objects.requireNonNull(client, "getScaledMouseX was called while client was not initialized yet!");

        double rawX = client.mouse.getX();

        return (int)(rawX * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth());
    }
    
    public static int getScaledMouseY() {
        Objects.requireNonNull(client, "getScaledMouseY was called while client was not initialized yet!");

        double rawY = client.mouse.getY();

        return (int)(rawY * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight());
    }
}
