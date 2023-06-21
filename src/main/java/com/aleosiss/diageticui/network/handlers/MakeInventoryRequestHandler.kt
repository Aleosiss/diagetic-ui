package com.aleosiss.diageticui.network.handlers

import com.aleosiss.diageticui.client.DiageticUIClient
import com.aleosiss.diageticui.client.render.HudOverlayRenderer
import com.aleosiss.diageticui.client.render.HudOverlayRenderer.CachedRequestData
import com.aleosiss.diageticui.network.packets.PacketGetInventory.Companion.fromPacketBuffer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import org.apache.logging.log4j.LogManager

// Receive the results of an inventory request
class MakeInventoryRequestHandler(private val renderer: HudOverlayRenderer) : ClientPlayNetworking.PlayChannelHandler {
    override fun receive(
        client: MinecraftClient,
        handler: ClientPlayNetworkHandler,
        buf: PacketByteBuf,
        responseSender: PacketSender
    ) {
        if (client.world == null) {
            LOGGER.error("ERROR: MakeInventoryRequestHandler received null world!")
            return
        }
        val packet = fromPacketBuffer(buf)
        renderer.updateCachedRequestData(
            packet.inventory?.let { inv ->
                CachedRequestData(
                    inv,
                    client.world!!.getBlockEntity(packet.pos),
                    System.currentTimeMillis(),
                    packet.containerType,
                    packet.invMaxSize
                )
            }
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger()
        val CLIENT_SEND_INVENTORY_REQUEST: ClientPlayNetworking.PlayChannelHandler =
            MakeInventoryRequestHandler(DiageticUIClient.HUD_OVERLAY_RENDERER)
    }
}
