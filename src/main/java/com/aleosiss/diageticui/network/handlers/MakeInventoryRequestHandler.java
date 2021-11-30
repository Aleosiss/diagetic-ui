package com.aleosiss.diageticui.network.handlers;

import com.aleosiss.diageticui.client.DiageticUIClient;
import com.aleosiss.diageticui.client.render.HudOverlayRenderer;
import com.aleosiss.diageticui.network.packets.PacketGetInventory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


// Receive the results of a inventory request
public class MakeInventoryRequestHandler implements ClientPlayNetworking.PlayChannelHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final ClientPlayNetworking.PlayChannelHandler CLIENT_SEND_INVENTORY_REQUEST = new MakeInventoryRequestHandler(DiageticUIClient.HUD_OVERLAY_RENDERER);

    private final HudOverlayRenderer renderer;

    public MakeInventoryRequestHandler(HudOverlayRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if(client.world == null) {
            LOGGER.error("ERROR: MakeInventoryRequestHandler received null world!");
            return;
        }

        PacketGetInventory packet = PacketGetInventory.fromPacketBuffer(buf);
        renderer.updateCachedRequestData(new HudOverlayRenderer.CachedRequestData(
                packet.getInventory(),
                client.world.getBlockEntity(packet.getPos()),
                System.currentTimeMillis(),
                packet.getContainerType(),
                packet.getInvMaxSize())
        );
    }
}
