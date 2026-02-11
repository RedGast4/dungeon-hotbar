package com.dungeonhotbar.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class HudInit {
    private static final net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement EMPTY_ELEMENT =
            (context, tickCounter) -> {
                // Intentionally empty: keeps vanilla element registered, but hidden.
            };

    public static void register() {
        // Не удаляем ванильные элементы, а подменяем их на пустой рендер.
        // Это важно для 1.21.11, где статус-бары ожидают зарегистрированные элементы.
        HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, EMPTY_ELEMENT);
        HudElementRegistry.replaceElement(VanillaHudElements.ARMOR_BAR, EMPTY_ELEMENT);
        HudElementRegistry.replaceElement(VanillaHudElements.INFO_BAR, EMPTY_ELEMENT);
        HudElementRegistry.replaceElement(VanillaHudElements.FOOD_BAR, EMPTY_ELEMENT);
        HudElementRegistry.replaceElement(VanillaHudElements.AIR_BAR, EMPTY_ELEMENT);
        HudElementRegistry.replaceElement(VanillaHudElements.MOUNT_HEALTH, EMPTY_ELEMENT);
        HudElementRegistry.replaceElement(VanillaHudElements.EXPERIENCE_LEVEL, EMPTY_ELEMENT);

        // Подменяем ванильный хотбар на кастомный.
        HudElementRegistry.replaceElement(
                VanillaHudElements.HOTBAR,
                (context, tickCounter) -> CustomHotbarRenderer.render(context)
        );
    }
}
