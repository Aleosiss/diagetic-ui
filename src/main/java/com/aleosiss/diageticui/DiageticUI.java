package com.aleosiss.diageticui;

import com.aleosiss.diageticui.network.NetworkService;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiageticUI implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "diageticui";

    @Override
    public void onInitialize() {
        LOGGER.info("main init");
        NetworkService.getInstance().initializeServer();
    }
}
