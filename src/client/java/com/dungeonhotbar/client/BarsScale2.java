package com.dungeonhotbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;

import static com.dungeonhotbar.client.CustomHotbarRenderer.hexToArgb;

public class BarsScale2 {

    private static final Identifier ICONS = Identifier.fromNamespaceAndPath("dungeonhotbar", "textures/gui/icons.png");

    private static final int TEX_WATER_U = 12, TEX_WATER_V = 165;
    private static final int TEX_LVL_U = 12, TEX_LVL_V = 163;
    private static final int TEX_JMP_U = 12, TEX_JMP_V = 167;
    private static final int TEX_BAR_WIDTH = 67;
    private static final int TEX_BAR_HEIGHT = 1;
    private static final int TEX_BAR_WIDTH_REG = 100;
    private static final int TEX_BAR_HEIGHT_REG = 2;

    private static final float SMOOTHING = 0.1f;
    private static float displayWater = 1f;
    private static float displayLevel = 0f;
    private static float displayJump = 0f;

    private static final float TEXT_SCALE = 0.48f;

    public static void render(GuiGraphicsExtractor graphics, boolean showWaterBar, boolean LvLBar) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;
        LocalPlayer player = client.player;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 82;

        boolean playerUnderwater = player.getAirSupply() < player.getMaxAirSupply();

        // --- WATER BAR ---
        if (showWaterBar && playerUnderwater && !player.getAbilities().instabuild) {
            float airRatio = player.getAirSupply() / (float) player.getMaxAirSupply();
            displayWater += (airRatio - displayWater) * SMOOTHING;

            int iconX = centerX - 34;
            int iconY = bottomY + 72;
            drawIcon(graphics, 0, iconX, iconY, 8, 8, 8, 8);
            renderBarX(graphics, client, TEX_WATER_U, TEX_WATER_V,
                    centerX - 75 - 30,
                    bottomY + 75,
                    displayWater, "", "#3CAFFF");
        } else {
            displayWater = 1f;
        }

        boolean showJumpBar = false;

        // --- JUMP BAR ---
        if (player.isPassenger() && player.getVehicle() instanceof LivingEntity mount) {
            float jumpRatio = 0f;

            if (mount.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)) {
                jumpRatio = player.getJumpRidingScale();
                if (jumpRatio > 0.99f) jumpRatio = 1.0f;
                showJumpBar = jumpRatio > 0f;
            }
            else if (mount.getType().toString().contains("camel")) {
                try {
                    var camel = (Camel) mount;
                    int cooldown = camel.getJumpCooldown();
                    int last = Camel.DASH.id();
                    float dashRatio = Math.max(0f, 1f - ((float) last / (float) cooldown));
                    if (dashRatio > 0.99f) dashRatio = 1.0f;
                    jumpRatio = dashRatio;
                    showJumpBar = dashRatio < 1f;
                } catch (Exception ignored) {}
            }

            if (showJumpBar) {
                displayJump += (jumpRatio - displayJump) * (SMOOTHING * 2f);
                if (jumpRatio >= 0.99f) displayJump = 1.0f;
                renderBarX(graphics, client, TEX_JMP_U, TEX_JMP_V,
                        centerX - 75 + 120,
                        bottomY + 75,
                        displayJump, "JMP", "#FFFFFF");
            }
        }

        // --- LEVEL BAR ---
        if (LvLBar && !showJumpBar) {
            float lvlRatio = Math.min(player.experienceProgress, 1f);
            displayLevel += (lvlRatio - displayLevel) * SMOOTHING;
            int displayLvl = Math.min(player.experienceLevel, 999);
            renderBarX(graphics, client, TEX_LVL_U, TEX_LVL_V,
                    centerX - 75 + 120,
                    bottomY + 75,
                    displayLevel, "LVL " + displayLvl, "#FFFFFF");
        }
    }

    private static void renderBarX(GuiGraphicsExtractor graphics, Minecraft client,
                                   int u, int v, int x, int y,
                                   float fillRatio, String label, String colorHex) {
        int fillColor = hexToArgb(colorHex);
        int grayColor = hexToArgb("#202020");

        // Подпись
        if (!label.isEmpty()) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x - 19, y - 1);
            graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
            graphics.text(client.font, label, 0, 0, fillColor, false);
            graphics.pose().popMatrix();
        }

        // --- Серая текстура фона ---
        if (fillRatio <= 0f) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                    x, y, 0, 0,
                    TEX_BAR_WIDTH, TEX_BAR_HEIGHT,
                    TEX_BAR_WIDTH_REG, TEX_BAR_HEIGHT_REG,
                    512, 512, grayColor);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                    x, y, u, v,
                    TEX_BAR_WIDTH, TEX_BAR_HEIGHT,
                    TEX_BAR_WIDTH_REG, TEX_BAR_HEIGHT_REG,
                    512, 512, grayColor);

            // --- Цветное заполнение ---
            int fillWidth = Math.round(TEX_BAR_WIDTH * fillRatio);
            int fillRegionWidth = Math.round(TEX_BAR_WIDTH_REG * fillRatio);
            if (fillWidth > 0 && fillRegionWidth > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS,
                        x, y, u, v,
                        fillWidth, TEX_BAR_HEIGHT,
                        fillRegionWidth, TEX_BAR_HEIGHT_REG,
                        512, 512, fillColor);
            }
        }
    }

    public static void drawIcon(GuiGraphicsExtractor graphics, int iconIndex, int x, int y,
                                int texWidth, int texHeight, int width, int height) {
        int u = 0, v = 0;
        switch (iconIndex) {
            case 0 -> { u = 37; v = 131; }
        }
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ICONS,
                x, y,
                u, v,
                texWidth, texHeight,
                width, height,
                512, 512
        );
    }
}