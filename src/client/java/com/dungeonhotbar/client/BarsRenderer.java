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

public class BarsRenderer {

    private static final Identifier ICONS = Identifier.of("dungeonhotbar", "textures/gui/icons.png");

    // Текстуры
    private static final int TEX_WATER_U = 12, TEX_WATER_V = 165;
    private static final int TEX_LVL_U = 12, TEX_LVL_V = 163;
    private static final int TEX_JMP_U = 12, TEX_JMP_V = 167;
    private static final int TEX_BAR_WIDTH = 45, TEX_BAR_HEIGHT = 1;
    private static final int TEX_BAR_WIDTH_REG = 100, TEX_BAR_HEIGHT_REG = 2;

    // Плавность
    private static final float SMOOTHING = 0.1f;
    private static float displayWater = 1f;
    private static float displayLevel = 0f;
    private static float displayJump = 0f;

    private static final float TEXT_SCALE = 0.32f;

    public static void render(DrawContext context, boolean showWaterBar, boolean LvLBar) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        ClientPlayerEntity player = client.player;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 55;

        int iconX = centerX - 23;
        int iconY = bottomY + 48;
        drawIcon(context, 0, iconX, iconY, 3, 3, 8, 8);
        // --- WATER BAR ---
        boolean playerUnderwater = player.getAir() < player.getMaxAir();
        if (showWaterBar && playerUnderwater && !player.isCreative()) {
            float airRatio = player.getAir() / (float) player.getMaxAir();

            displayWater += (airRatio - displayWater) * SMOOTHING;
            renderBarX(context, client, TEX_WATER_U, TEX_WATER_V,
                    centerX - 50 - 20, bottomY + 50,
                    displayWater, "", "#3CAFFF");
        }


        boolean showJumpBar = false;


        // --- JUMP BAR ---
        if (player.hasVehicle() && player.getVehicle() instanceof LivingEntity mount) {

            float jumpRatio = 0f;

            // 1️⃣ Лошади, мулы, ослы и т.п.
            if (mount.getAttributes().hasAttribute(EntityAttributes.JUMP_STRENGTH)) {
                jumpRatio = player.getMountJumpStrength();

                // иногда max < 1.0 → нормализуем
                if (jumpRatio > 0.99f) jumpRatio = 1.0f;

                showJumpBar = jumpRatio > 0f;
            }

            // 2️⃣ Верблюды (dash)
            else if (mount.getType().toString().contains("camel")) {
                try {
                    var camel = (CamelEntity) mount;
                    int cooldown = camel.getJumpCooldown();
                    int last = CamelEntity.DASHING.id(); // безопаснее, чем DASHING.id()

                    float dashRatio = Math.max(0f, 1f - ((float) last / (float) cooldown));
                    if (dashRatio > 0.99f) dashRatio = 1.0f;

                    jumpRatio = dashRatio;
                    showJumpBar = dashRatio < 1f;
                } catch (Exception ignored) {
                }
            }

            // 3️⃣ Остальные маунты (свинья, гаст и т.д.) — не показываем
            if (showJumpBar) {
                // Плавное обновление, но быстрее реагирует при отпускании пробела
                displayJump += (jumpRatio - displayJump) * (SMOOTHING * 2f);

                // Гарантия полного заполнения при 100 %
                if (jumpRatio >= 0.99f) displayJump = 1.0f;

                renderBarX(context, client, TEX_JMP_U, TEX_JMP_V,
                        centerX - 50 + 80, bottomY + 50,
                        displayJump, "JMP", "#FFFFFF");
            }
        }
        // --- LEVEL BAR ---
        if (LvLBar && !showJumpBar) {
            float lvlRatio = Math.min(player.experienceProgress, 1f);
            displayLevel += (lvlRatio - displayLevel) * SMOOTHING;
            int displayLvl = Math.min(player.experienceLevel, 999);
            renderBarX(context, client, TEX_LVL_U, TEX_LVL_V,
                    centerX - 50 + 80, bottomY + 50,
                    displayLevel, "LVL " + displayLvl, "#FFFFFF");
        }
    }

    /**
     * Рендер горизонтального бара с фоновой частью и заполнением
     */
    private static void renderBarX(DrawContext context, MinecraftClient client,
                                   int u, int v, int x, int y,
                                   float fillRatio, String label, String colorHex) {
        int colorText = hexToArgb(colorHex);

        // Подпись
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x - 13, y - 1);
        context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
        context.drawText(client.textRenderer, label, 0, 0, colorText, false);
        context.getMatrices().popMatrix();

        // --- Рендер фона бара (пустой, серый) ---

        int grayColor = hexToArgb("#202020");
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICONS,
                x, y, u, v, TEX_BAR_WIDTH, TEX_BAR_HEIGHT, TEX_BAR_WIDTH_REG, TEX_BAR_HEIGHT_REG,  512, 512, grayColor);

        // --- Рендер заполнения ---
        int fillWidth = Math.round(TEX_BAR_WIDTH * fillRatio);
        if (fillWidth <= 0) return;

        int scissorX1 = x;
        int scissorY1 = y;
        int scissorX2 = x + fillWidth;
        int scissorY2 = y + TEX_BAR_HEIGHT;

        context.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICONS,
                x, y, u, v, TEX_BAR_WIDTH, TEX_BAR_HEIGHT, TEX_BAR_WIDTH_REG, TEX_BAR_HEIGHT_REG, 512, 512);
        context.disableScissor();
    }
    public static void drawIcon(DrawContext context, int iconIndex, int x, int y, int tex_width, int tex_height, int width, int height) {
        int u = 0, v = 0;

        switch (iconIndex) {

            case 0 -> { u = 37; v = 131; } // water
        }

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                ICONS,
                x, y,        // позиция на экране
                u, v,
                tex_width, tex_height, // uv координаты в текстуре
                width, height,    // размер на экране
                512, 512,    // размер текстуры
                0xFFFFFFFF           // цвет (белый)
        );
    }
}
