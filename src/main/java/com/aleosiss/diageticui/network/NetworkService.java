package com.aleosiss.diageticui.network;

import com.aleosiss.diageticui.network.handlers.MakeInventoryRequestHandler;
import com.aleosiss.diageticui.network.handlers.ReceiveInventoryRequestHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;

public class NetworkService {
    private static NetworkService SINGLETON;
    private NetworkService() {}

    public static NetworkService getInstance() {
        if(SINGLETON == null) {
            SINGLETON = new NetworkService();
        }

        return SINGLETON;
    }

    public void initializeServer() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkConstants.DIAGETIC_INVENTORY_REQUEST_PACKET, ReceiveInventoryRequestHandler.SERVER_RECEIVE_INVENTORY_REQUEST);
    }

    @Environment(EnvType.CLIENT)
    public void initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkConstants.DIAGETIC_INVENTORY_RESPONSE_PACKET, MakeInventoryRequestHandler.CLIENT_SEND_INVENTORY_REQUEST);
    }

    public PacketByteBuf buildInventoryRequestPacket(BlockEntity blockEntity) {
        PacketByteBuf packetByteBuf = PacketByteBufs.create();
        packetByteBuf.writeBlockPos(blockEntity.getPos());

        return packetByteBuf;
    }



    //TODO: Move deserialization here
}
