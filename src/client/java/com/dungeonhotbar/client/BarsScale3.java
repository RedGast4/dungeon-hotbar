package com.dungeonhotbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.util.Identifier;

import static com.dungeonhotbar.client.hud.HotbarRenderer.hexToArgb;

public class BarsScale3 {

    private static final Identifier BAR_WATER = Identifier.of("dungeonhotbar", "textures/gui/progress_bar_water_air.png");
    private static final Identifier BAR_LEVEL = Identifier.of("dungeonhotbar", "textures/gui/progress_bar_enchanment.png");
    private static final Identifier BAR_JUMP = Identifier.of("dungeonhotbar", "textures/gui/progress_bar_jump_mount.png");
    private static final Identifier BAR_EMPTY = Identifier.of("dungeonhotbar", "textures/gui/progress_bar_zero_down.png");

    private static final int BAR_TEX_WIDTH = 100;
    private static final int BAR_TEX_HEIGHT = 2;

    private static final int BAR_DISPLAY_WIDTH = 90;
    private static final int BAR_DISPLAY_HEIGHT = 1;

    private static final float BAR_SCALE_X = (float) BAR_DISPLAY_WIDTH / BAR_TEX_WIDTH;
    private static final float BAR_SCALE_Y = (float) BAR_DISPLAY_HEIGHT / BAR_TEX_HEIGHT;

    private static final float SMOOTHING = 0.1f;
    private static float displayWater = 1f;
    private static float displayLevel = 0f;
    private static float displayJump = 0f;

    private static final float TEXT_SCALE = 0.64f;
    private static final int BAR_ICON_SIZE = 14;

    public static void render(DrawContext context, boolean showWaterBar, boolean LvLBar) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        ClientPlayerEntity player = client.player;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 110;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.enableBlend();

        // --- WATER BAR ---
        boolean playerUnderwater = player.getAir() < player.getMaxAir();
        if (showWaterBar && playerUnderwater && !player.isCreative()) {
            float airRatio = player.getAir() / (float) player.getMaxAir();
            displayWater += (airRatio - displayWater) * SMOOTHING;
            renderBar(context, client, BAR_WATER,
                    centerX - 100 - 40, bottomY + 100,
                    displayWater, "", "#3CAFFF", false);
            int iconX = centerX - 46;
            int iconY = bottomY + 95;
            NumberRenderer.drawIcon(context, 3, iconX, iconY, BAR_ICON_SIZE, BAR_ICON_SIZE);
        }

        boolean showJumpBar = false;

        // --- JUMP BAR ---
        if (player.hasVehicle() && player.getVehicle() instanceof LivingEntity mount) {
            float jumpRatio = 0f;

            if (mount.getAttributes().hasAttribute(EntityAttributes.GENERIC_JUMP_STRENGTH)) {
                jumpRatio = player.getMountJumpStrength();
                if (jumpRatio > 0.99f) jumpRatio = 1.0f;
                showJumpBar = jumpRatio > 0f;
            }
            else if (mount instanceof CamelEntity camel) {
                try {
                    float cooldown = camel.getJumpCooldown();
                    float dashRatio = Math.max(0f, 1f - cooldown);
                    if (dashRatio > 0.99f) dashRatio = 1.0f;
                    jumpRatio = dashRatio;
                    showJumpBar = dashRatio < 1f;
                } catch (Exception ignored) {}
            }

            if (showJumpBar) {
                displayJump += (jumpRatio - displayJump) * (SMOOTHING * 2f);
                if (jumpRatio >= 0.99f) displayJump = 1.0f;
                renderBar(context, client, BAR_JUMP,
                        centerX - 100 + 160, bottomY + 100,
                        displayJump, "JMP", "#FFFFFF", true);
            }
        }

        // --- LEVEL BAR (текст на одной линии) ---
        if (LvLBar && !showJumpBar) {
            float lvlRatio = Math.min(player.experienceProgress, 1f);
            displayLevel += (lvlRatio - displayLevel) * SMOOTHING;
            int displayLvl = Math.min(player.experienceLevel, 999);
            renderBar(context, client, BAR_LEVEL,
                    centerX - 100 + 160, bottomY + 100,
                    displayLevel, "LVL " + displayLvl, "#FFFFFF", true);
        }

        RenderSystem.disableBlend();
    }

    private static void renderBar(DrawContext context, MinecraftClient client,
                                  Identifier barTexture,
                                  int x, int y,
                                  float fillRatio, String label, String colorHex,
                                  boolean labelOnSameLine) {
        int colorText = hexToArgb(colorHex);

        if (!label.isEmpty()) {
            context.getMatrices().push();
            if (labelOnSameLine) {
                // Текст на одной линии с баром (слева)
                context.getMatrices().translate(x - 6, y - 1, 0);
                context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
                int textWidth = client.textRenderer.getWidth(label);
                context.drawText(client.textRenderer, label, -textWidth, 0, colorText, false);
            } else {
                // Текст над баром
                context.getMatrices().translate(x, y - 10, 0);
                context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
                context.drawText(client.textRenderer, label, 0, 0, colorText, false);
            }
            context.getMatrices().pop();
        }

        RenderSystem.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(BAR_SCALE_X, BAR_SCALE_Y, 1.0f);
        context.drawTexture(BAR_EMPTY, 0, 0, 0, 0, BAR_TEX_WIDTH, BAR_TEX_HEIGHT, BAR_TEX_WIDTH, BAR_TEX_HEIGHT);
        context.getMatrices().pop();

        int fillWidth = Math.round(BAR_DISPLAY_WIDTH * fillRatio);
        if (fillWidth <= 0) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            return;
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int scissorHeight = Math.max(BAR_DISPLAY_HEIGHT, 1);
        context.enableScissor(x, y, x + fillWidth, y + scissorHeight);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(BAR_SCALE_X, BAR_SCALE_Y, 1.0f);
        context.drawTexture(barTexture, 0, 0, 0, 0, BAR_TEX_WIDTH, BAR_TEX_HEIGHT, BAR_TEX_WIDTH, BAR_TEX_HEIGHT);
        context.getMatrices().pop();
        context.disableScissor();
    }
}