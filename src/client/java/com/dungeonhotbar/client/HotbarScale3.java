package com.dungeonhotbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;

import static com.dungeonhotbar.client.hud.HotbarRenderer.hexToArgb;
import static net.minecraft.client.MinecraftClient.getInstance;

public class HotbarScale3 {

    private static final Identifier WIDGETS = new Identifier("dungeonhotbar", "textures/gui/dungeon_hotbar.png");
    private static final Identifier SELECTED_SLOT = new Identifier("dungeonhotbar", "textures/gui/dungeon_hotbar_select.png");
    private static final Identifier FRAME = new Identifier("dungeonhotbar", "textures/gui/dungeon_frame_health.png");
    private static final Identifier HEARTH = new Identifier("dungeonhotbar", "textures/gui/dungeon_health.png");

    private static final int WIDGETS_WIDTH = 1636;
    private static final int WIDGETS_HEIGHT = 210;
    private static final int SELECT_SLOT_WIDTH = 103;
    private static final int SELECT_SLOT_HEIGHT = 103;
    private static final int FRAME_WIDTH = 310;
    private static final int FRAME_HEIGHT = 270;
    private static final int HEARTH_WIDTH = 257;
    private static final int HEARTH_HEIGHT = 202;

    // Scale3: ×2.0
    private static final float WIDGETS_SCALE_X = 364f / WIDGETS_WIDTH;
    private static final float WIDGETS_SCALE_Y = 44f / WIDGETS_HEIGHT;
    private static final float SLOT_SCALE_X = 28f / SELECT_SLOT_WIDTH;
    private static final float SLOT_SCALE_Y = 28f / SELECT_SLOT_HEIGHT;
    private static final float FRAME_SCALE_X = 56f / FRAME_WIDTH;
    private static final float FRAME_SCALE_Y = 56f / FRAME_HEIGHT;
    private static final float HEARTH_SCALE_X = 48f / HEARTH_WIDTH;
    private static final float HEARTH_SCALE_Y = 48f / HEARTH_HEIGHT;

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

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();

        // WIDGETS
        context.getMatrices().push();
        context.getMatrices().translate(centerX - 182, bottomY, 0);
        context.getMatrices().scale(WIDGETS_SCALE_X, WIDGETS_SCALE_Y, 1.0f);
        context.drawTexture(WIDGETS, 0, 0, 0, 0, WIDGETS_WIDTH, WIDGETS_HEIGHT, WIDGETS_WIDTH, WIDGETS_HEIGHT);
        context.getMatrices().pop();

        boolean isCreative = client.player.isCreative();
        boolean isOnMount = client.player.hasVehicle() && client.player.getVehicle() instanceof LivingEntity;

        boolean showPlayerHp = false;
        boolean showPlayerHp2 = false;
        boolean showMountHp = false;
        boolean showArmor = false;
        boolean showFood = false;
        boolean showWaterBar = false;
        boolean LvLBar = true;

        if (!isCreative) {
            if (isOnMount) {
                showPlayerHp = true;
                showPlayerHp2 = true;
                showMountHp = true;
                showArmor = true;
            } else {
                showPlayerHp = true;
                showPlayerHp2 = true;
                showArmor = true;
                showFood = true;
                showWaterBar = true;
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

        StatusTextScale3.render(context, showPlayerHp, showMountHp, showArmor, showFood);

        int[] offsetX = {4, 4, 4, 4, 4, 4, 4, 4, 4};
        int[] offsetY = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        renderHotbarItems(context, centerX, bottomY, offsetX, offsetY, inv);

        renderSelectedSlot(context, centerX, bottomY, inv);

        renderVanillaOffHand(context, centerX, bottomY, client);
        BarsScale3.render(context, showWaterBar, LvLBar);

        RenderSystem.disableBlend();
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

        // FRAME
        context.getMatrices().push();
        context.getMatrices().translate(frameX, frameY, 0);
        context.getMatrices().scale(FRAME_SCALE_X, FRAME_SCALE_Y, 1.0f);
        context.drawTexture(FRAME, 0, 0, 0, 0, FRAME_WIDTH, FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
        context.getMatrices().pop();

        if (visibleHeight > 0) {
            float r = ((tint >> 16) & 0xFF) / 255.0f;
            float g = ((tint >> 8) & 0xFF) / 255.0f;
            float b = (tint & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(r, g, b, 1.0f);

            context.enableScissor(
                    heartX,
                    heartY + (heartHeight - visibleHeight),
                    heartX + heartWidth,
                    heartY + heartHeight
            );
            // HEARTH
            context.getMatrices().push();
            context.getMatrices().translate(heartX, heartY, 0);
            context.getMatrices().scale(HEARTH_SCALE_X, HEARTH_SCALE_Y, 1.0f);
            context.drawTexture(HEARTH, 0, 0, 0, 0, HEARTH_WIDTH, HEARTH_HEIGHT, HEARTH_WIDTH, HEARTH_HEIGHT);
            context.getMatrices().pop();
            context.disableScissor();

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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

            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0);
            context.getMatrices().scale(scale, scale, 1.0f);

            context.drawItem(stack, 0, 0);
            context.drawItemInSlot(client.textRenderer, stack, 0, 0);

            if (stack.getCount() > 1) {
                String countText = String.valueOf(stack.getCount());
                context.drawText(client.textRenderer, countText,
                        x + 24 - client.textRenderer.getWidth(countText), y + 18, 0xFFFFFF, true);
            }

            if (stack.isDamaged()) {
                float damage = (float) stack.getDamage() / (float) stack.getMaxDamage();
                int barWidth = Math.round(26.0F - damage * 26.0F);
                int barColor = stack.getItemBarColor();
                context.fill(x + 4, y + 26, x + 4 + barWidth, y + 28, barColor | 0xFF000000);
            }

            context.getMatrices().pop();
        }
    }

    private static void renderVanillaOffHand(DrawContext context, int centerX, int bottomY, MinecraftClient client) {
        if (client.player == null) return;
        var offHand = client.player.getOffHandStack();
        if (offHand.isEmpty()) return;

        int baseX = centerX - 84 - 62 - 26;
        int baseY = bottomY + 6;

        float slotSize = 20f;
        float defaultItemSize = 16f;
        float scale = slotSize / defaultItemSize;

        context.getMatrices().push();
        context.getMatrices().translate(baseX, baseY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        context.drawItem(offHand, 0, 0);
        context.drawItemInSlot(client.textRenderer, offHand, 0, 0);

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

        context.getMatrices().pop();
    }

    private static void renderSelectedSlot(DrawContext context, int centerX, int bottomY, PlayerInventory inv) {
        int selected = inv.selectedSlot;
        int slotX = getSlotPositionsX(centerX)[selected];
        int slotY = bottomY + 4;

        context.getMatrices().push();
        context.getMatrices().translate(slotX, slotY, 0);
        context.getMatrices().scale(SLOT_SCALE_X, SLOT_SCALE_Y, 1.0f);
        context.drawTexture(SELECTED_SLOT, 0, 0, 0, 0, SELECT_SLOT_WIDTH, SELECT_SLOT_HEIGHT, SELECT_SLOT_WIDTH, SELECT_SLOT_HEIGHT);
        context.getMatrices().pop();
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