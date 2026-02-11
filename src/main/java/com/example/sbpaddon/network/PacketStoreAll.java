package com.example.sbpaddon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketStoreAll {
    public PacketStoreAll() {}

    public static void encode(PacketStoreAll msg, FriendlyByteBuf buf) {}

    public static PacketStoreAll decode(FriendlyByteBuf buf) {
        return new PacketStoreAll();
    }

    public static void handle(PacketStoreAll msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                StoreLogic.transferItemsToBackpack(player, player.containerMenu);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
