package com.aleosiss.diageticui.client;

import com.aleosiss.diageticui.DiageticUI;
import com.aleosiss.diageticui.client.render.HudOverlayRenderer;
import com.aleosiss.diageticui.network.NetworkService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiageticUIClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Identifier HUD_OVERLAY_RENDERER_ID = new Identifier(DiageticUI.MOD_ID, "hud_overlay_renderer");
    public static final HudOverlayRenderer HUD_OVERLAY_RENDERER = new HudOverlayRenderer(HUD_OVERLAY_RENDERER_ID);

    @Override
    public void onInitializeClient() {
            LOGGER.info("Client init");

            NetworkService.getInstance().initializeClient();
            HudRenderCallback.EVENT.register(HUD_OVERLAY_RENDERER::show);
    }
}
