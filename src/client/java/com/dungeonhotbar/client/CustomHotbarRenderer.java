package com.dungeonhotbar.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;

import static net.minecraft.client.MinecraftClient.getInstance;

public class CustomHotbarRenderer {

    private static final Identifier WIDGETS = Identifier.of("dungeonhotbar", "textures/gui/widgets.png");
    private static final Identifier FRAME = Identifier.of("dungeonhotbar", "textures/gui/icons.png");
    private static final Identifier HEARTH = Identifier.of("dungeonhotbar", "textures/gui/hearth_bar.png");

    private static float displayHealth = 20f; // плавная анимация HP

    public static void render(DrawContext context) {
        MinecraftClient client = getInstance();
        if (client.player == null) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 24;

        PlayerInventory inv = client.player.getInventory();

        // Плавная интерполяция HP
        float currentHealth = client.player.getHealth();
        displayHealth += (currentHealth - displayHealth) * 0.1f;

        // 1️⃣ Фон хотбара
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS,
                centerX - 91, bottomY,
                200, 46,
                182, 22,
                1636, 210,
                1836, 256
        );

        boolean isCreative = client.player.isCreative();
        boolean isOnMount = client.player.hasVehicle() && client.player.getVehicle() instanceof LivingEntity;

        // --- Определяем, что показывать ---
        boolean showPlayerHp2 = false;
        boolean showPlayerHp = false;
        boolean showMountHp = false;
        boolean showArmor = false;
        boolean snowFood = false;
        boolean showWaterBar = false;
        boolean LvLBar = true;


        if (!isCreative) {
            if (isOnMount) { // на маунтe
                showPlayerHp = true;
                showPlayerHp2 = true;
                showMountHp = true;
                showArmor = true;
            } else { // на земле
                showWaterBar = true;
                showPlayerHp = true;
                showPlayerHp2 = true;
                showArmor = true;
                snowFood = true;
            }
        } else { // креатив
            if (isOnMount) { // на маунте
                showPlayerHp = true;
                showPlayerHp2 = true;
                showMountHp = true;
            } // на земле ничего не показываем
        }

        if (showPlayerHp2) {
            renderHeart(context, centerX, bottomY, client);
        }

        // --- Рендер текста HP и брони ---
        StatusTextRenderer.render(context, showPlayerHp, showMountHp, showArmor, snowFood);

        // --- Рендер рамки и сердца игрока ---


        // --- Рендер предметов ---
        int[] offsetX = {2, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] offsetY = {1, 1, 1, 1, 1, 1, 1, 1, 1};
        renderHotbarItems(context, centerX, bottomY, offsetX, offsetY, inv);

        // --- Подсветка выбранного слота ---
        renderSelectedSlot(context, centerX, bottomY, inv);

        // --- Off-hand ---
        renderVanillaOffHand(context, centerX, bottomY, client);
        BarsRenderer.render(context, showWaterBar, LvLBar);
    }

    private static void renderHeart(DrawContext context, int centerX, int bottomY, MinecraftClient client) {
        if (client.player == null) return;

        float currentHealth = client.player.getHealth() + client.player.getAbsorptionAmount();
        float maxHealth = client.player.getMaxHealth() + client.player.getAbsorptionAmount();
        int tint = getHeartTintSurvival(client);

        int baseY = bottomY - 7;
        int totalSlots = 24;
        float hpPerSlot = maxHealth / totalSlots;
        int lostSlots = totalSlots - (int) Math.ceil(currentHealth / hpPerSlot);

        int heartX = centerX - 12;
        int heartY = baseY;
        int heartWidth = 24;
        int heartHeight = 24;
        int visibleHeight = heartHeight - lostSlots;

        // Рендер рамки и сердца
        context.drawTexture(RenderPipelines.GUI_TEXTURED, FRAME,
                centerX - 14, bottomY - 9,
                0, 234,
                28, 28,
                310, 271,
                512, 512
        );

        if (visibleHeight > 0) {
            context.enableScissor(
                    heartX,
                    heartY + (heartHeight - visibleHeight),
                    heartX + heartWidth,
                    heartY + heartHeight
            );
            context.drawTexture(RenderPipelines.GUI_TEXTURED, HEARTH,
                    heartX, heartY,
                    0, 202,
                    heartWidth, heartHeight,
                    257, 202,
                    257, 606,
                    tint
            );
            context.disableScissor();
        }
    }

    private static int getHeartTintSurvival(MinecraftClient client) {
        if (client == null || client.player == null) return hexToArgb("#ff1313");
        var player = client.player;

        if (player.hasStatusEffect(StatusEffects.POISON)) return hexToArgb("#8b8712");
        if (player.hasStatusEffect(StatusEffects.WITHER)) return hexToArgb("#2b2b2b");
        if (player.hasStatusEffect(StatusEffects.ABSORPTION)) return hexToArgb("#ffec00");
        if (player.isFrozen()) return hexToArgb("#80e5ef");

        return hexToArgb("#ff1313");
    }

    static int hexToArgb(String hex) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    // Остальной код рендера хотбара и предметов без изменений
    private static void renderHotbarItems(DrawContext context, int centerX, int bottomY, int[] offsetX, int[] offsetY, PlayerInventory inv) {
        MinecraftClient client = getInstance();
        int[] slotPositionsX = getSlotPositionsX(centerX);
        int slotY = bottomY + 3;

        float slotSize = 10f;
        float defaultItemSize = 16f;
        float scale = slotSize / defaultItemSize;

        for (int i = 0; i < 9; i++) {
            var stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            int x = slotPositionsX[i] + offsetX[i];
            int y = slotY + offsetY[i];

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(x, y);
            context.getMatrices().scale(scale, scale);

            context.drawItem(stack, 0, 0);
            context.drawStackOverlay(client.textRenderer, stack, 0, 0);

            if (stack.getCount() > 1) {
                String countText = String.valueOf(stack.getCount());
                context.drawText(client.textRenderer, countText, x + 12 - client.textRenderer.getWidth(countText), y + 9, 0xFFFFFF, true);
            }

            if (stack.isDamaged()) {
                float damage = (float) stack.getDamage() / (float) stack.getMaxDamage();
                int barWidth = Math.round(13.0F - damage * 13.0F);
                int barColor = stack.getItemBarColor();
                context.fill(x + 2, y + 13, x + 2 + barWidth, y + 14, barColor | 0xFF000000);
            }

            context.getMatrices().popMatrix();
        }
    }

    private static void renderVanillaOffHand(DrawContext context, int centerX, int bottomY, MinecraftClient client) {
        if (client.player == null) return;
        var offHand = client.player.getOffHandStack();
        if (offHand.isEmpty()) return;

        int baseX = centerX - 42 - 31 - 13;
        int baseY = bottomY + 3;

        float slotSize = 10f;
        float defaultItemSize = 16f;
        float scale = slotSize / defaultItemSize;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(baseX, baseY);
        context.getMatrices().scale(scale, scale);

        context.drawItem(offHand, 0, 0);
        context.drawStackOverlay(client.textRenderer, offHand, 0, 0);

        if (offHand.getCount() > 1) {
            String countText = String.valueOf(offHand.getCount());
            int textX = 12 - client.textRenderer.getWidth(countText);
            int textY = 9;
            context.drawText(client.textRenderer, countText, textX, textY, 0xFFFFFF, true);
        }

        if (offHand.isDamaged()) {
            float damage = (float) offHand.getDamage() / (float) offHand.getMaxDamage();
            int barWidth = Math.round(13.0F - damage * 13.0F);
            int barColor = offHand.getItemBarColor();
            context.fill(2, 13, 2 + barWidth, 14, barColor | 0xFF000000);
        }

        context.getMatrices().popMatrix();
    }

    private static void renderSelectedSlot(DrawContext context, int centerX, int bottomY, PlayerInventory inv) {
        int selected = inv.getSelectedSlot();

        int u = 97, v = 153;
        int width = 14, height = 14;
        int selWidth = 103, selHeight = 103;
        int texWidth = 1836, texHeight = 256;

        int slotX = getSlotPositionsX(centerX)[selected];
        int slotY = bottomY + 2;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS, slotX, slotY,
                u, v, width, height,
                selWidth, selHeight, texWidth, texHeight);
    }

    private static int[] getSlotPositionsX(int centerX) {
        int[] slotPositionsX = new int[9];
        slotPositionsX[0] = centerX - 42 - 31;
        slotPositionsX[1] = centerX - 27 - 31;
        slotPositionsX[2] = centerX - 12 - 31;
        slotPositionsX[3] = centerX - 29;
        slotPositionsX[4] = centerX + 31 - 17;
        slotPositionsX[5] = centerX + 31 + 14 - 16;
        slotPositionsX[6] = centerX + 31 + 28 - 15;
        slotPositionsX[7] = centerX + 31 + 42 - 15;
        slotPositionsX[8] = centerX + 31 + 56 - 14;
        return slotPositionsX;
    }
}
