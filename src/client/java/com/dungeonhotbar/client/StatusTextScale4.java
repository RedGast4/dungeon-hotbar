package com.dungeonhotbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static com.dungeonhotbar.client.CustomHotbarRenderer.hexToArgb;

public class StatusTextScale4 {

    private static final int PLAYER_TEXT_OFFSET_X = 127;
    private static final int PLAYER_TEXT_OFFSET_Y = 3;
    private static final int MOUNT_TEXT_OFFSET_X  = 142;
    private static final int MOUNT_TEXT_OFFSET_Y  = 24;
    private static final int ARMOR_TEXT_OFFSET_X  = 112;
    private static final int ARMOR_TEXT_OFFSET_Y  = 24;
    private static final int FOOD_TEXT_OFFSET_X   = 142;
    private static final int FOOD_TEXT_OFFSET_Y   = 24;

    private static final float SMOOTHING = 0.1f;
    private static final float TEXT_SCALE = 0.6f;

    private static float displayPlayerHp = 20;
    private static float displayMountHp = 0;
    private static float displayArmor = 0;
    private static float displayFood = 20;

    public static void render(GuiGraphicsExtractor graphics, boolean showPlayerHp, boolean showMountHp, boolean showArmor, boolean showFood) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;

        Player player = client.player;
        boolean hasMount = player.isPassenger() && player.getVehicle() instanceof LivingEntity;
        LivingEntity mount = hasMount ? (LivingEntity) player.getVehicle() : null;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 36;
        int hotbarLeft = centerX - 136;

        if (showPlayerHp) {
            float playerCur = player.getHealth() + player.getAbsorptionAmount();
            float playerMax = player.getMaxHealth() + player.getAbsorptionAmount();
            displayPlayerHp += (playerCur - displayPlayerHp) * SMOOTHING;

            drawStatWithIcon(graphics, client,
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

            drawStatWithIcon(graphics, client,
                    String.valueOf(Math.round(displayMountHp)),
                    String.valueOf(Math.round(mountMax)),
                    hotbarLeft + MOUNT_TEXT_OFFSET_X,
                    bottomY + MOUNT_TEXT_OFFSET_Y,
                    "#EF7E4A", 4);
        }

        if (showArmor) {
            float armorCur = player.getArmorValue();
            float armorMax = 20;
            displayArmor += (armorCur - displayArmor) * SMOOTHING;

            drawStatWithIcon(graphics, client,
                    String.valueOf(Math.round(displayArmor)),
                    String.valueOf(Math.round(armorMax)),
                    hotbarLeft + ARMOR_TEXT_OFFSET_X,
                    bottomY + ARMOR_TEXT_OFFSET_Y,
                    "#CACACA", 1);
        }

        if (showFood) {
            float foodCur = player.getFoodData().getFoodLevel();
            float foodMax = 20;
            displayFood += (foodCur - displayFood) * SMOOTHING;

            drawStatWithIcon(graphics, client,
                    String.valueOf(Math.round(displayFood)),
                    String.valueOf(Math.round(foodMax)),
                    hotbarLeft + FOOD_TEXT_OFFSET_X,
                    bottomY + FOOD_TEXT_OFFSET_Y,
                    "#9D6D43", 2);
        }
    }

    private static void drawStatWithIcon(GuiGraphicsExtractor graphics, Minecraft client,
                                         String cur, String max, int x, int y,
                                         String colorHex, int iconIndex) {

        int colorMain = hexToArgb(colorHex);
        int colorSep = hexToArgb("#888888");

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);

        NumberRenderer.drawIcon(graphics, iconIndex, 9, -10, 8, 8);

        // Добавляем пробел для однозначных чисел
        String curDisplay = cur.length() == 1 ? " " + cur : cur;

        int offset = 0;
        graphics.text(client.font, curDisplay, offset, 0, colorMain, false);
        offset += client.font.width(curDisplay);
        graphics.text(client.font, "/", offset, 0, colorSep, false);
        offset += client.font.width("/");
        graphics.text(client.font, max, offset, 0, colorMain, false);

        graphics.pose().popMatrix();
    }
}