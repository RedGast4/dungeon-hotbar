package com.dungeonhotbar.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import static com.dungeonhotbar.client.hud.HotbarRenderer.hexToArgb;

public class StatusTextScale2 {

    private static final int PLAYER_TEXT_OFFSET_X = 127;    // 85 × 1.5
    private static final int PLAYER_TEXT_OFFSET_Y = 3;      // 2 × 1.5
    private static final int MOUNT_TEXT_OFFSET_X  = 142;    // 95 × 1.5
    private static final int MOUNT_TEXT_OFFSET_Y  = 29;     // 16 × 1.5
    private static final int ARMOR_TEXT_OFFSET_X  = 112;    // 75 × 1.5
    private static final int ARMOR_TEXT_OFFSET_Y  = 29;
    private static final int FOOD_TEXT_OFFSET_X   = 142;
    private static final int FOOD_TEXT_OFFSET_Y   = 29;

    private static final float SMOOTHING = 0.1f;
    private static final float TEXT_SCALE = 0.6f;           // 0.4 × 1.5

    // Scale2: размер иконки ×1.5 (8→12)
    private static final int ICON_SIZE = 10;
    private static final int ICON_OFFSET_Y = 3;

    private static float displayPlayerHp = 20;
    private static float displayMountHp = 0;
    private static float displayArmor = 0;
    private static float displayFood = 20;

    public static void render(DrawContext context, boolean showPlayerHp, boolean showMountHp, boolean showArmor, boolean showFood) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        PlayerEntity player = client.player;
        boolean hasMount = player.hasVehicle() && player.getVehicle() instanceof LivingEntity;
        LivingEntity mount = hasMount ? (LivingEntity) player.getVehicle() : null;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 36;
        int hotbarLeft = centerX - 136;

        if (showPlayerHp) {
            float playerCur = player.getHealth() + player.getAbsorptionAmount();
            float playerMax = player.getMaxHealth() + player.getAbsorptionAmount();
            displayPlayerHp += (playerCur - displayPlayerHp) * SMOOTHING;
            drawStatWithIcon(context, client,
                    String.valueOf(Math.round(displayPlayerHp)),
                    String.valueOf(Math.round(playerMax)),
                    hotbarLeft + PLAYER_TEXT_OFFSET_X,
                    bottomY + PLAYER_TEXT_OFFSET_Y,
                    "#FFFFFF", 0);
        }

        if (showMountHp && hasMount && mount != null) {
            float mountCur = mount.getHealth() + mount.getAbsorptionAmount();
            float mountMax = mount.getMaxHealth() + mount.getAbsorptionAmount();
            displayMountHp += (mountCur - displayMountHp) * SMOOTHING;
            drawStatWithIcon(context, client,
                    String.valueOf(Math.round(displayMountHp)),
                    String.valueOf(Math.round(mountMax)),
                    hotbarLeft + MOUNT_TEXT_OFFSET_X,
                    bottomY + MOUNT_TEXT_OFFSET_Y,
                    "#EF7E4A", 4);
        }

        if (showArmor) {
            float armorCur = player.getArmor();
            float armorMax = 20;
            displayArmor += (armorCur - displayArmor) * SMOOTHING;
            drawStatWithIcon(context, client,
                    String.valueOf(Math.round(displayArmor)),
                    String.valueOf(Math.round(armorMax)),
                    hotbarLeft + ARMOR_TEXT_OFFSET_X,
                    bottomY + ARMOR_TEXT_OFFSET_Y,
                    "#CACACA", 1);
        }

        if (showFood) {
            float foodCur = player.getHungerManager().getFoodLevel();
            float foodMax = 20;
            displayFood += (foodCur - displayFood) * SMOOTHING;
            drawStatWithIcon(context, client,
                    String.valueOf(Math.round(displayFood)),
                    String.valueOf(Math.round(foodMax)),
                    hotbarLeft + FOOD_TEXT_OFFSET_X,
                    bottomY + FOOD_TEXT_OFFSET_Y,
                    "#9D6D43", 2);
        }
    }

    private static void drawStatWithIcon(DrawContext context, MinecraftClient client,
                                         String cur, String max, int x, int y,
                                         String colorHex, int iconIndex) {
        int colorMain = hexToArgb(colorHex);
        int colorSep = hexToArgb("#888888");

        String fullText = cur + "/" + max;
        int textWidth = client.textRenderer.getWidth(fullText);

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);

        // Иконка СВЕРХУ по ЦЕНТРУ
        int iconX = (textWidth - ICON_SIZE) / 2;
        int iconY = -ICON_SIZE - ICON_OFFSET_Y;
        NumberRenderer.drawIcon(context, iconIndex, iconX, iconY, ICON_SIZE, ICON_SIZE);

        int offset = 0;
        context.drawText(client.textRenderer, cur, offset, 0, colorMain, false);
        offset += client.textRenderer.getWidth(cur);
        context.drawText(client.textRenderer, "/", offset, 0, colorSep, false);
        offset += client.textRenderer.getWidth("/");
        context.drawText(client.textRenderer, max, offset, 0, colorMain, false);

        context.getMatrices().pop();
    }
}