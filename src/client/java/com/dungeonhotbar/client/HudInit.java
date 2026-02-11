package com.dungeonhotbar.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;

public class HudInit {
    private static final HudElement EMPTY_ELEMENT =
            (context, tickCounter) -> {
                // Keep element registered but render nothing.
            };

    private static void hide(Identifier elementId) {
        HudElementRegistry.replaceElement(elementId, previous -> EMPTY_ELEMENT);
    }

    public static void register() {
        // Для 1.21.11 нельзя удалять эти элементы: Fabric ожидает, что они останутся зарегистрированы.
        hide(VanillaHudElements.HEALTH_BAR);
        hide(VanillaHudElements.ARMOR_BAR);
        hide(VanillaHudElements.INFO_BAR);
        hide(VanillaHudElements.FOOD_BAR);
        hide(VanillaHudElements.AIR_BAR);
        hide(VanillaHudElements.MOUNT_HEALTH);
        hide(VanillaHudElements.EXPERIENCE_LEVEL);

        // Подменяем ванильный хотбар на кастомный рендерер.
        HudElementRegistry.replaceElement(
                VanillaHudElements.HOTBAR,
                previous -> (context, tickCounter) -> CustomHotbarRenderer.render(context)
        );
    }
}
