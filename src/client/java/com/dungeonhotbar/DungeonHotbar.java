package com.dungeonhotbar;

import com.dungeonhotbar.client.HudInit;
import com.dungeonhotbar.client.HudInitG;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DungeonHotbar implements ClientModInitializer {
    public static final String MOD_ID = "dungeonhotbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[DungeonHotbar] Initializing custom HUD...");
        HudInit.register();
    }
}
