package com.aleosiss.diageticui.network.handlers

import com.aleosiss.diageticui.client.DiageticUIClient
import com.aleosiss.diageticui.client.render.HudOverlayRenderer
import com.aleosiss.diageticui.client.render.HudOverlayRenderer.CachedRequestData
import com.aleosiss.diageticui.data.ContainerType
import com.aleosiss.diageticui.network.NetworkConstants
import com.aleosiss.diageticui.network.packets.PacketGetInventory.Companion.fromPacketBuffer
import com.jcraft.jorbis.Block
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.LogManager

// Receive the results of an inventory request
class MakeInventoryRequestHandler(private val renderer: HudOverlayRenderer) : ClientPlayNetworking.PlayPayloadHandler<NetworkConstants.InventoryResponsePayload> {

    companion object {
        private val LOGGER = LogManager.getLogger()
        val CLIENT_SEND_INVENTORY_REQUEST: ClientPlayNetworking.PlayPayloadHandler<NetworkConstants.InventoryResponsePayload> =
            MakeInventoryRequestHandler(DiageticUIClient.HUD_OVERLAY_RENDERER)
    }

    override fun receive(payload: NetworkConstants.InventoryResponsePayload, context: ClientPlayNetworking.Context) {
        val world = context.client().world
        if (world == null) {
            LOGGER.error("ERROR: MakeInventoryRequestHandler received null world!")
            return
        }

        payload.inventoryTagData?.let { inv -> CachedRequestData(
            inv,
            world.getBlockEntity(BlockPos.fromLong(payload.blockPosLong)),
            System.currentTimeMillis(),
            ContainerType.entries[payload.containerTypeInt],
            payload.invMaxSize
        ) }.also {
            renderer.updateCachedRequestData(it)
        }
    }
}
