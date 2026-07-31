package com.dungeonhotbar.client;

import com.dungeonhotbar.DungeonHotbar;
import com.dungeonhotbar.client.hud.HotbarRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class HudInit {

    public static void register() {
        DungeonHotbar.LOGGER.info("[DungeonHotbar] Registering custom HUD renderer...");

        // В 1.20.1 используем HudRenderCallback вместо HudElementRegistry
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player != null && client.world != null) {
                HotbarRenderer.render(drawContext, tickDelta);
            }
        });

        DungeonHotbar.LOGGER.info("[DungeonHotbar] HUD renderer registered successfully!");
    }
}