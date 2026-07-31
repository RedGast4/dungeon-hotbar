package com.dungeonhotbar.client;

import net.minecraft.client.MinecraftClient;

public class ScaleDetector {

    private static int lastScale = -1;

    public static int getGuiScale() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.options != null) {
            int scale = client.options.getGuiScale().getValue();
            return scale == 0 ? 2 : scale;
        }
        return 2;
    }

    public static boolean hasChanged() {
        int current = getGuiScale();
        if (current != lastScale) {
            lastScale = current;
            return true;
        }
        return false;
    }
}