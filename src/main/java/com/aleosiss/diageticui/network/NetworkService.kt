package com.aleosiss.diageticui.network

import com.aleosiss.diageticui.network.handlers.MakeInventoryRequestHandler
import com.aleosiss.diageticui.network.handlers.ReceiveInventoryRequestHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.BlockEntity
import net.minecraft.network.PacketByteBuf


class NetworkService private constructor() {
    fun initializeServer() {
        ServerPlayNetworking.registerGlobalReceiver(
            NetworkConstants.DIAGETIC_INVENTORY_REQUEST_PACKET,
            ReceiveInventoryRequestHandler.SERVER_RECEIVE_INVENTORY_REQUEST
        )
    }

    @Environment(EnvType.CLIENT)
    fun initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
            NetworkConstants.DIAGETIC_INVENTORY_RESPONSE_PACKET,
            MakeInventoryRequestHandler.CLIENT_SEND_INVENTORY_REQUEST
        )
    }

    fun buildInventoryRequestPacket(blockEntity: BlockEntity): PacketByteBuf {
        val packetByteBuf = PacketByteBufs.create()
        packetByteBuf.writeBlockPos(blockEntity.pos)
        return packetByteBuf
    } //TODO: Move deserialization here

    companion object {
        private var SINGLETON: NetworkService? = null
        val instance: NetworkService
            get() {
                if (SINGLETON == null) {
                    SINGLETON = NetworkService()
                }
                return SINGLETON as NetworkService
            }
    }
}
