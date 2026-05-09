package com.dungeonhotbar.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class NumberRenderer {

    private static final Identifier ICONS = Identifier.fromNamespaceAndPath("dungeonhotbar", "textures/gui/icons.png");

    public static final int ICON_SIZE = 9;

    public static void drawIcon(GuiGraphicsExtractor graphics, int iconIndex, int x, int y, int width, int height) {
        int u = 0, v = 0;

        switch (iconIndex) {
            case 0 -> { u = 117; v = 140; }
            case 1 -> { u = 69; v = 122; } // armor
            case 2 -> { u = 103; v = 122; } // food
            case 3 -> { u = 37; v = 131; } // water
            case 4 -> { u = 104; v = 140; } // mount
        }

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ICONS,
                x, y,
                u, v,
                width, height,
                width, height,
                512, 512
        );
    }
}