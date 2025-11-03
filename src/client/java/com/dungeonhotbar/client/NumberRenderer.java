package com.dungeonhotbar.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

public class NumberRenderer {

    private static final Identifier ICONS = Identifier.of("dungeonhotbar", "textures/gui/icons.png");

    // Иконки 9x9
    public static final int ICON_SIZE = 9;

    /**
     * Отрисовка иконки по индексу 0..3
     * 1 - armor, 2 - food, 3 - water, 4 - hearth mount
     */
    public static void drawIcon(DrawContext context, int iconIndex, int x, int y, int width, int height) {
        int u = 0, v = 0;

        switch (iconIndex) {
            case 0 -> {u = 117; v = 140;}
            case 1 -> { u = 69; v = 122; } // armor
            case 2 -> { u = 103; v = 122; } // food
            case 3 -> { u = 37; v = 131; } // water
            case 4 -> { u = 104; v = 140; } // mount
        }

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                ICONS,
                x, y,        // позиция на экране
                u, v,        // uv координаты в текстуре
                width, height,   // размер на экране
                512, 512,    // размер текстуры
                0xFFFFFFFF   // цвет (белый)
        );
    }
}
