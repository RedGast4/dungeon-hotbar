package com.dungeonhotbar.client.hud;

import com.dungeonhotbar.client.ScaleDetector;
import com.dungeonhotbar.client.StatusTextRenderer;
import com.dungeonhotbar.client.BarsRenderer;
import com.dungeonhotbar.client.HotbarScale1;
import com.dungeonhotbar.client.HotbarScale2;
import com.dungeonhotbar.client.HotbarScale3;
import com.dungeonhotbar.client.HotbarScale4;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;

import static net.minecraft.client.MinecraftClient.getInstance;

public class HotbarRenderer {

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

    private static final float SCALE_X = 182f / WIDGETS_WIDTH;
    private static final float SCALE_Y = 22f / WIDGETS_HEIGHT;

    private static float displayHealth = 20f;

    public static void render(DrawContext drawContext, float tickDelta) {
        int scale = ScaleDetector.getGuiScale();

        switch (scale) {
            case 1 -> { HotbarScale1.render(drawContext); return; }
            case 2 -> { HotbarScale2.render(drawContext); return; }
            case 3 -> { HotbarScale3.render(drawContext); return; }
            case 4 -> { HotbarScale4.render(drawContext); return; }
            default -> { /* auto */ }
        }

        MinecraftClient client = getInstance();
        if (client.player == null) return;

        int screenWidth = drawContext.getScaledWindowWidth();
        int screenHeight = drawContext.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 24;

        PlayerInventory inv = client.player.getInventory();

        float currentHealth = client.player.getHealth();
        displayHealth += (currentHealth - displayHealth) * 0.1f;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();

        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(centerX - 91, bottomY, 0);
        drawContext.getMatrices().scale(SCALE_X, SCALE_Y, 1.0f);
        drawContext.drawTexture(WIDGETS, 0, 0, 0, 0, WIDGETS_WIDTH, WIDGETS_HEIGHT, WIDGETS_WIDTH, WIDGETS_HEIGHT);
        drawContext.getMatrices().pop();

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
            renderHeart(drawContext, centerX, bottomY, client);
        }

        renderItemName(drawContext, client, centerX - 14, bottomY - 9, 28);

        StatusTextRenderer.render(drawContext, showPlayerHp, showMountHp, showArmor, showFood);

        int[] offsetX = {2, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] offsetY = {1, 1, 1, 1, 1, 1, 1, 1, 1};
        renderHotbarItems(drawContext, centerX, bottomY, offsetX, offsetY, inv);

        renderSelectedSlot(drawContext, centerX, bottomY, inv);

        renderVanillaOffHand(drawContext, centerX, bottomY, client);
        BarsRenderer.render(drawContext, showWaterBar, LvLBar);

        RenderSystem.disableBlend();
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

        float frameScaleX = 28f / FRAME_WIDTH;
        float frameScaleY = 28f / FRAME_HEIGHT;
        context.getMatrices().push();
        context.getMatrices().translate(centerX - 14, bottomY - 9, 0);
        context.getMatrices().scale(frameScaleX, frameScaleY, 1.0f);
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
            float hearthScaleX = (float)heartWidth / HEARTH_WIDTH;
            float hearthScaleY = (float)heartHeight / HEARTH_HEIGHT;
            context.getMatrices().push();
            context.getMatrices().translate(heartX, heartY, 0);
            context.getMatrices().scale(hearthScaleX, hearthScaleY, 1.0f);
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

    public static int hexToArgb(String hex) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

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

            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.drawItem(stack, 0, 0);
            context.drawItemInSlot(client.textRenderer, stack, 0, 0);
            context.getMatrices().pop();

            if (stack.getCount() > 1) {
                String countText = String.valueOf(stack.getCount());
                context.drawText(client.textRenderer, countText,
                        x + 12 - client.textRenderer.getWidth(countText), y + 9, 0xFFFFFF, true);
            }
            if (stack.isDamaged()) {
                float damage = (float) stack.getDamage() / (float) stack.getMaxDamage();
                int barWidth = Math.round(13.0F - damage * 13.0F);
                int barColor = stack.getItemBarColor();
                context.fill(x + 2, y + 13, x + 2 + barWidth, y + 14, barColor | 0xFF000000);
            }
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

        context.getMatrices().push();
        context.getMatrices().translate(baseX, baseY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawItem(offHand, 0, 0);
        context.drawItemInSlot(client.textRenderer, offHand, 0, 0);
        context.getMatrices().pop();

        if (offHand.getCount() > 1) {
            String countText = String.valueOf(offHand.getCount());
            context.drawText(client.textRenderer, countText,
                    baseX + 12 - client.textRenderer.getWidth(countText), baseY + 9, 0xFFFFFF, true);
        }
        if (offHand.isDamaged()) {
            float damage = (float) offHand.getDamage() / (float) offHand.getMaxDamage();
            int barWidth = Math.round(13.0F - damage * 13.0F);
            int barColor = offHand.getItemBarColor();
            context.fill(baseX + 2, baseY + 13, baseX + 2 + barWidth, baseY + 14, barColor | 0xFF000000);
        }
    }

    private static void renderSelectedSlot(DrawContext context, int centerX, int bottomY, PlayerInventory inv) {
        int selected = inv.selectedSlot;
        int slotX = getSlotPositionsX(centerX)[selected];
        int slotY = bottomY + 2;

        float slotScaleX = 14f / SELECT_SLOT_WIDTH;
        float slotScaleY = 14f / SELECT_SLOT_HEIGHT;
        context.getMatrices().push();
        context.getMatrices().translate(slotX, slotY, 0);
        context.getMatrices().scale(slotScaleX, slotScaleY, 1.0f);
        context.drawTexture(SELECTED_SLOT, 0, 0, 0, 0, SELECT_SLOT_WIDTH, SELECT_SLOT_HEIGHT, SELECT_SLOT_WIDTH, SELECT_SLOT_HEIGHT);
        context.getMatrices().pop();
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