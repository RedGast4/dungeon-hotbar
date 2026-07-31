package com.dungeonhotbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;

public class NumberRenderer {

    private static final Identifier ICON_ARMOR = Identifier.of("dungeonhotbar", "textures/gui/icon_armor.png");
    private static final Identifier ICON_HEALTH_MOUNT = Identifier.of("dungeonhotbar", "textures/gui/icon_health_mount.png");
    private static final Identifier ICON_HUNGRY = Identifier.of("dungeonhotbar", "textures/gui/icon_hungry.png");
    private static final Identifier ICON_WATER = Identifier.of("dungeonhotbar", "textures/gui/icon_water.png");
    private static final Identifier ICON_AIR = Identifier.of("dungeonhotbar", "textures/gui/icon_air.png");

    public static final int ICON_SIZE = 9;

    public static void drawIcon(DrawContext context, int iconIndex, int x, int y, int width, int height) {
        Identifier icon = switch (iconIndex) {
            case 0 -> ICON_AIR;
            case 1 -> ICON_ARMOR;
            case 2 -> ICON_HUNGRY;
            case 3 -> ICON_WATER;
            case 4 -> ICON_HEALTH_MOUNT;
            default -> ICON_AIR;
        };

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();

        float scaleX = (float) width / ICON_SIZE;
        float scaleY = (float) height / ICON_SIZE;

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scaleX, scaleY, 1.0f);
        context.drawTexture(icon, 0, 0, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        context.getMatrices().pop();

        RenderSystem.disableBlend();
    }
}