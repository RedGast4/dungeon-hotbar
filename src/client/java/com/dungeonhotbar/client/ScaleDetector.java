package com.dungeonhotbar.client;

import net.minecraft.client.Minecraft;

public class ScaleDetector {

    private static int lastScale = -1;

    public static int getGuiScale() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.options != null) {
            int scale = client.options.guiScale().get();
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