package com.yipeekiyaay.kitchen_sink.network;

import com.yipeekiyaay.kitchen_sink.network.packets.*;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

public class KitchenSinkNetworking {
    public static void init() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                MoveSlotlessItemC2SPacket.TYPE,
                MoveSlotlessItemC2SPacket.CODEC,
                MoveSlotlessItemC2SPacket::handle
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                PickSlotlessItemC2SPacket.TYPE,
                PickSlotlessItemC2SPacket.CODEC,
                PickSlotlessItemC2SPacket::handle
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                PutSlotlessItemC2SPacket.TYPE,
                PutSlotlessItemC2SPacket.CODEC,
                PutSlotlessItemC2SPacket::handle
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                DropSlotlessItemC2SPacket.TYPE,
                DropSlotlessItemC2SPacket.CODEC,
                DropSlotlessItemC2SPacket::handle
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                SwapSlotlessItemC2SPacket.TYPE,
                SwapSlotlessItemC2SPacket.CODEC,
                SwapSlotlessItemC2SPacket::handle
        );

        if (Platform.getEnv() == EnvType.SERVER) {
            NetworkManager.registerS2CPayloadType(
                    SyncSlotlessInventoryS2CPacket.TYPE,
                    SyncSlotlessInventoryS2CPacket.CODEC
            );
        }
    }

    public static void initClient() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                SyncSlotlessInventoryS2CPacket.TYPE,
                SyncSlotlessInventoryS2CPacket.CODEC,
                SyncSlotlessInventoryS2CPacket::handle
        );
    }
}
