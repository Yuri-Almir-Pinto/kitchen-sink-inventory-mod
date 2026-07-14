package com.yipeekiyaay.kitchen_sink.network;

import com.yipeekiyaay.kitchen_sink.network.packets.*;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import dev.architectury.networking.NetworkManager;

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
                NetworkManager.c2s(),
                ClickSlotItemC2SPacket.TYPE,
                ClickSlotItemC2SPacket.CODEC,
                ClickSlotItemC2SPacket::handle
        );
    }

    public static void initClient() {
        NetworkManager.registerReceiver(
                NetworkManager.s2c(),
                SyncSlotlessInventoryS2CPacket.TYPE,
                SyncSlotlessInventoryS2CPacket.CODEC,
                SyncSlotlessInventoryS2CPacket::handle
        );
    }
}
