package com.aleosiss.diageticui

import com.aleosiss.diageticui.network.NetworkService
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager

class DiageticUI : ModInitializer {
    override fun onInitialize() {
        LOGGER.info("main init")
        NetworkService.instance.initializeServer()
    }

    companion object {
        private val LOGGER = LogManager.getLogger()
        const val MOD_ID = "diageticui"
    }
}
