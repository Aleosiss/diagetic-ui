package com.aleosiss.diageticui.client

import com.aleosiss.diageticui.DiageticUI
import com.aleosiss.diageticui.client.render.HudOverlayRenderer
import com.aleosiss.diageticui.network.NetworkService
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

class DiageticUIClient : ClientModInitializer {
    override fun onInitializeClient() {
        LOGGER.info("Client init")
        NetworkService.instance.initializeClient()
        HudRenderCallback.EVENT.register(HudRenderCallback { draw: DrawContext?, _ ->
            draw?.let { HUD_OVERLAY_RENDERER.show(it) }
        })
    }

    companion object {
        private val LOGGER = LogManager.getLogger()
        private val HUD_OVERLAY_RENDERER_ID = Identifier.of(DiageticUI.MOD_ID, "hud_overlay_renderer")
        val HUD_OVERLAY_RENDERER = HudOverlayRenderer(HUD_OVERLAY_RENDERER_ID)
    }
}
