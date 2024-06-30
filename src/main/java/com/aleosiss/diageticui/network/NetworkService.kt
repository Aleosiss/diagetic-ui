package com.aleosiss.diageticui.network

import com.aleosiss.diageticui.client.DiageticUIClient
import com.aleosiss.diageticui.network.NetworkConstants.DIAGETIC_INVENTORY_REQUEST_PAYLOAD
import com.aleosiss.diageticui.network.NetworkConstants.DIAGETIC_INVENTORY_RESPONSE_PAYLOAD
import com.aleosiss.diageticui.network.handlers.MakeInventoryRequestHandler
import com.aleosiss.diageticui.network.handlers.ReceiveInventoryRequestHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.BlockEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.CustomPayload


class NetworkService private constructor() {
    fun initializeServer() {
        val id: CustomPayload.Id<NetworkConstants.InventoryRequestPayload> = DIAGETIC_INVENTORY_REQUEST_PAYLOAD
        PayloadTypeRegistry.playC2S().register(id, NetworkConstants.InventoryRequestPayload.CODEC);
        val handler: ServerPlayNetworking.PlayPayloadHandler<NetworkConstants.InventoryRequestPayload> = ReceiveInventoryRequestHandler()
        ServerPlayNetworking.registerGlobalReceiver(id, handler)
    }

    @Environment(EnvType.CLIENT)
    fun initializeClient() {
        val id: CustomPayload.Id<NetworkConstants.InventoryResponsePayload> = NetworkConstants.DIAGETIC_INVENTORY_RESPONSE_PAYLOAD
        PayloadTypeRegistry.playS2C().register(id, NetworkConstants.InventoryResponsePayload.CODEC);
        val handler: ClientPlayNetworking.PlayPayloadHandler<NetworkConstants.InventoryResponsePayload> = MakeInventoryRequestHandler(DiageticUIClient.HUD_OVERLAY_RENDERER)
        ClientPlayNetworking.registerGlobalReceiver(id, handler)
    }

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
