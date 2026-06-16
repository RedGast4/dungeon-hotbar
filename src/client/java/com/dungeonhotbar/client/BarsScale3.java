package com.dungeonhotbar.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.util.Identifier;

import static com.dungeonhotbar.client.CustomHotbarRenderer.hexToArgb;

public class BarsScale3 {

    private static final Identifier ICONS = Identifier.of("dungeonhotbar", "textures/gui/icons.png");

    private static final int TEX_WATER_U = 12, TEX_WATER_V = 165;
    private static final int TEX_LVL_U = 12, TEX_LVL_V = 163;
    private static final int TEX_JMP_U = 12, TEX_JMP_V = 167;
    private static final int TEX_BAR_WIDTH = 90;        // 45 × 2
    private static final int TEX_BAR_HEIGHT = 2;        // 1 × 2
    private static final int TEX_BAR_WIDTH_REG = 100;
    private static final int TEX_BAR_HEIGHT_REG = 2;

    private static final float SMOOTHING = 0.1f;
    private static float displayWater = 1f;
    private static float displayLevel = 0f;
    private static float displayJump = 0f;

    private static final float TEXT_SCALE = 0.64f;      // 0.32 × 2

    public static void render(DrawContext context, boolean showWaterBar, boolean LvLBar) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        ClientPlayerEntity player = client.player;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 110;               // 55 × 2

        boolean playerUnderwater = player.getAir() < player.getMaxAir();
        if (showWaterBar && playerUnderwater && !player.isCreative()) {
            float airRatio = player.getAir() / (float) player.getMaxAir();
            displayWater += (airRatio - displayWater) * SMOOTHING;
            renderBarX(context, client, TEX_WATER_U, TEX_WATER_V,
                    centerX - 100 - 40,                 // (50+20)×2
                    bottomY + 100,                      // 50 × 2
                    displayWater, "", "#3CAFFF");
            int iconX = centerX - 46;                       // 23 × 2
            int iconY = bottomY + 95;                       // 48 × 2
            drawIcon(context, 0, iconX, iconY, 15, 15, 16, 16);
        }

        boolean showJumpBar = false;

        if (player.hasVehicle() && player.getVehicle() instanceof LivingEntity mount) {
            float jumpRatio = 0f;

            if (mount.getAttributes().hasAttribute(EntityAttributes.JUMP_STRENGTH)) {
                jumpRatio = player.getMountJumpStrength();
                if (jumpRatio > 0.99f) jumpRatio = 1.0f;
                showJumpBar = jumpRatio > 0f;
            }
            else if (mount.getType().toString().contains("camel")) {
                try {
                    var camel = (CamelEntity) mount;
                    int cooldown = camel.getJumpCooldown();
                    int last = CamelEntity.DASHING.id();
                    float dashRatio = Math.max(0f, 1f - ((float) last / (float) cooldown));
                    if (dashRatio > 0.99f) dashRatio = 1.0f;
                    jumpRatio = dashRatio;
                    showJumpBar = dashRatio < 1f;
                } catch (Exception ignored) {}
            }

            if (showJumpBar) {
                displayJump += (jumpRatio - displayJump) * (SMOOTHING * 2f);
                if (jumpRatio >= 0.99f) displayJump = 1.0f;
                renderBarX(context, client, TEX_JMP_U, TEX_JMP_V,
                        centerX - 100 + 160,            // (-50+80)×2
                        bottomY + 100,
                        displayJump, "JMP", "#FFFFFF");
            }
        }

        if (LvLBar && !showJumpBar) {
            float lvlRatio = Math.min(player.experienceProgress, 1f);
            displayLevel += (lvlRatio - displayLevel) * SMOOTHING;
            int displayLvl = Math.min(player.experienceLevel, 999);
            renderBarX(context, client, TEX_LVL_U, TEX_LVL_V,
                    centerX - 100 + 160,
                    bottomY + 100,
                    displayLevel, "LVL " + displayLvl, "#FFFFFF");
        }
    }

    private static void renderBarX(DrawContext context, MinecraftClient client,
                                   int u, int v, int x, int y,
                                   float fillRatio, String label, String colorHex) {
        int colorText = hexToArgb(colorHex);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x - 26, y - 2);   // 13×2, 1×2
        context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
        context.drawText(client.textRenderer, label, 0, 0, colorText, false);
        context.getMatrices().popMatrix();

        int grayColor = hexToArgb("#202020");
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICONS,
                x, y, u, v, TEX_BAR_WIDTH, TEX_BAR_HEIGHT,
                TEX_BAR_WIDTH_REG, TEX_BAR_HEIGHT_REG,
                512, 512, grayColor);

        int fillWidth = Math.round(TEX_BAR_WIDTH * fillRatio);
        if (fillWidth <= 0) return;

        context.enableScissor(x, y, x + fillWidth, y + TEX_BAR_HEIGHT);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICONS,
                x, y, u, v, TEX_BAR_WIDTH, TEX_BAR_HEIGHT,
                TEX_BAR_WIDTH_REG, TEX_BAR_HEIGHT_REG,
                512, 512);
        context.disableScissor();
    }

    public static void drawIcon(DrawContext context, int iconIndex, int x, int y,
                                int texWidth, int texHeight, int width, int height) {
        int u = 0, v = 0;
        switch (iconIndex) {
            case 0 -> { u = 37; v = 131; }
        }
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                ICONS,
                x, y,
                u, v,
                texWidth, texHeight,
                width, height,
                512, 512,
                0xFFFFFFFF
        );
    }
}