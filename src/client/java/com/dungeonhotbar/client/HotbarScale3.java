package com.dungeonhotbar.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;

import static com.dungeonhotbar.client.CustomHotbarRenderer.hexToArgb;
import static net.minecraft.client.MinecraftClient.getInstance;

public class HotbarScale3 {

    private static final Identifier WIDGETS = Identifier.of("dungeonhotbar", "textures/gui/widgets.png");
    private static final Identifier FRAME = Identifier.of("dungeonhotbar", "textures/gui/icons.png");
    private static final Identifier HEARTH = Identifier.of("dungeonhotbar", "textures/gui/hearth_bar.png");

    private static float displayHealth = 20f;

    public static void render(DrawContext context) {
        MinecraftClient client = getInstance();
        if (client.player == null) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 48;

        PlayerInventory inv = client.player.getInventory();

        float currentHealth = client.player.getHealth();
        displayHealth += (currentHealth - displayHealth) * 0.1f;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS,
                centerX - 182, bottomY,
                200, 46,
                364, 44,
                1636, 210,
                1836, 256
        );

        boolean isCreative = client.player.isCreative();
        boolean isOnMount = client.player.hasVehicle() && client.player.getVehicle() instanceof LivingEntity;

        boolean showPlayerHp2 = false;
        boolean showPlayerHp = false;
        boolean showMountHp = false;
        boolean showArmor = false;
        boolean snowFood = false;
        boolean showWaterBar = false;
        boolean LvLBar = true;

        if (!isCreative) {
            if (isOnMount) {
                showPlayerHp = true;
                showPlayerHp2 = true;
                showMountHp = true;
                showArmor = true;
            } else {
                showWaterBar = true;
                showPlayerHp = true;
                showPlayerHp2 = true;
                showArmor = true;
                snowFood = true;
            }
        } else {
            if (isOnMount) {
                showPlayerHp = true;
                showPlayerHp2 = true;
                showMountHp = true;
            }
        }

        if (showPlayerHp2) {
            renderHeart(context, centerX, bottomY, client);
        }

        renderItemName(context, client, centerX - 28, bottomY - 18, 56);

        StatusTextScale3.render(context, showPlayerHp, showMountHp, showArmor, snowFood);

        int[] offsetX = {4, 4, 4, 4, 4, 4, 4, 4, 4};
        int[] offsetY = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        renderHotbarItems(context, centerX, bottomY, offsetX, offsetY, inv);

        renderSelectedSlot(context, centerX, bottomY, inv);

        renderVanillaOffHand(context, centerX, bottomY, client);
        BarsScale3.render(context, showWaterBar, LvLBar);
    }

    private static void renderHeart(DrawContext context, int centerX, int bottomY, MinecraftClient client) {
        if (client.player == null) return;

        float currentHealth = client.player.getHealth() + client.player.getAbsorptionAmount();
        float maxHealth = client.player.getMaxHealth() + client.player.getAbsorptionAmount();
        int tint = getHeartTintSurvival(client);

        int heartWidth = 48;
        int heartHeight = 48;
        int totalSlots = 24;
        float hpPerSlot = maxHealth / totalSlots;
        int lostSlots = totalSlots - (int) Math.ceil(currentHealth / hpPerSlot);
        float pixelsPerSlot = heartHeight / (float) totalSlots;
        int visibleHeight = heartHeight - Math.round(lostSlots * pixelsPerSlot);

        int frameX = centerX - 28;
        int frameY = bottomY - 18;
        int heartX = centerX - 24;
        int heartY = frameY + 4;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, FRAME,
                frameX, frameY,
                0, 234,
                56, 56,
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

    private static void renderHotbarItems(DrawContext context, int centerX, int bottomY, int[] offsetX, int[] offsetY, PlayerInventory inv) {
        MinecraftClient client = getInstance();
        int[] slotPositionsX = getSlotPositionsX(centerX);
        int slotY = bottomY + 6;

        float slotSize = 20f;
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
                context.drawText(client.textRenderer, countText, x + 24 - client.textRenderer.getWidth(countText), y + 18, 0xFFFFFF, true);
            }

            if (stack.isDamaged()) {
                float damage = (float) stack.getDamage() / (float) stack.getMaxDamage();
                int barWidth = Math.round(26.0F - damage * 26.0F);
                int barColor = stack.getItemBarColor();
                context.fill(x + 4, y + 26, x + 4 + barWidth, y + 28, barColor | 0xFF000000);
            }

            context.getMatrices().popMatrix();
        }
    }

    private static void renderVanillaOffHand(DrawContext context, int centerX, int bottomY, MinecraftClient client) {
        if (client.player == null) return;
        var offHand = client.player.getOffHandStack();
        if (offHand.isEmpty()) return;

        int baseX = centerX - 84 - 62 - 26;
        int baseY = bottomY + 7;

        float slotSize = 20f;
        float defaultItemSize = 16f;
        float scale = slotSize / defaultItemSize;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(baseX, baseY);
        context.getMatrices().scale(scale, scale);

        context.drawItem(offHand, 0, 0);
        context.drawStackOverlay(client.textRenderer, offHand, 0, 0);

        if (offHand.getCount() > 1) {
            String countText = String.valueOf(offHand.getCount());
            int textX = 24 - client.textRenderer.getWidth(countText);
            int textY = 18;
            context.drawText(client.textRenderer, countText, textX, textY, 0xFFFFFF, true);
        }

        if (offHand.isDamaged()) {
            float damage = (float) offHand.getDamage() / (float) offHand.getMaxDamage();
            int barWidth = Math.round(26.0F - damage * 26.0F);
            int barColor = offHand.getItemBarColor();
            context.fill(4, 26, 4 + barWidth, 28, barColor | 0xFF000000);
        }

        context.getMatrices().popMatrix();
    }

    private static void renderSelectedSlot(DrawContext context, int centerX, int bottomY, PlayerInventory inv) {
        int selected = inv.getSelectedSlot();

        int u = 97, v = 153;
        int width = 28, height = 28;
        int selWidth = 103, selHeight = 103;
        int texWidth = 1836, texHeight = 256;

        int slotX = getSlotPositionsX(centerX)[selected];
        int slotY = bottomY + 4;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS, slotX, slotY,
                u, v, width, height,
                selWidth, selHeight, texWidth, texHeight);
    }

    private static int[] getSlotPositionsX(int centerX) {
        int[] slotPositionsX = new int[9];
        slotPositionsX[0] = centerX - 84 - 62;
        slotPositionsX[1] = centerX - 54 - 62;
        slotPositionsX[2] = centerX - 24 - 62;
        slotPositionsX[3] = centerX - 58;
        slotPositionsX[4] = centerX + 62 - 34;
        slotPositionsX[5] = centerX + 62 + 28 - 32;
        slotPositionsX[6] = centerX + 62 + 56 - 30;
        slotPositionsX[7] = centerX + 62 + 84 - 30;
        slotPositionsX[8] = centerX + 62 + 112 - 28;
        return slotPositionsX;
    }

    private static void renderItemName(DrawContext context, MinecraftClient client, int frameX, int frameY, int frameWidth) {
        if (client.player == null) return;
        var stack = client.player.getMainHandStack();
        if (stack.isEmpty()) return;
        String name = stack.getName().getString();
        int nameX = frameX + frameWidth / 2 - client.textRenderer.getWidth(name) / 2;
        int nameY = frameY - 12;
        context.drawText(client.textRenderer, name, nameX, nameY, 0xFFFFFFFF, true);
    }
}