package com.dungeonhotbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static com.dungeonhotbar.client.CustomHotbarRenderer.hexToArgb;

public class HotbarScale4 {

    private static final Identifier WIDGETS = Identifier.fromNamespaceAndPath("dungeonhotbar", "textures/gui/widgets.png");
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath("dungeonhotbar", "textures/gui/icons.png");
    private static final Identifier HEARTH = Identifier.fromNamespaceAndPath("dungeonhotbar", "textures/gui/hearth_bar.png");

    private static float displayHealth = 20f;

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        LocalPlayer player = client.player;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int centerX = screenWidth / 2;
        int bottomY = screenHeight - 36;

        Inventory inv = player.getInventory();

        float currentHealth = player.getHealth();
        displayHealth += (currentHealth - displayHealth) * 0.1f;

        graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS,
                centerX - 136, bottomY,
                200, 46,
                273, 33,
                1636, 210,
                1836, 256
        );

        boolean isCreative = player.getAbilities().instabuild;
        boolean isOnMount = player.isPassenger() && player.getVehicle() instanceof LivingEntity;

        boolean showPlayerHp2 = false;
        boolean showPlayerHp = false;
        boolean showMountHp = false;
        boolean showArmor = false;
        boolean snowFood = false;
        boolean showWaterBar = false;
        boolean LvLBar = true;

        if (!isCreative) {
            if (isOnMount) {
                showPlayerHp = false;
                showPlayerHp2 = false;
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
                showPlayerHp = false;
                showPlayerHp2 = false;
                showMountHp = true;
            }
        }

        if (showPlayerHp2) {
            renderHeart(graphics, centerX, bottomY, client);
        }

        renderItemName(graphics, client, centerX - 21, bottomY - 13, 42);

        StatusTextScale4.render(graphics, showPlayerHp, showMountHp, showArmor, snowFood);

        int[] offsetX = {3, 3, 3, 3, 3, 3, 3, 3, 3};
        int[] offsetY = {1, 1, 1, 1, 1, 1, 1, 1, 1};
        renderHotbarItems(graphics, centerX, bottomY, offsetX, offsetY, inv);

        renderSelectedSlot(graphics, centerX, bottomY, inv);

        renderVanillaOffHand(graphics, centerX, bottomY, client);
        BarsScale4.render(graphics, showWaterBar, LvLBar);
    }

    private static void renderHeart(GuiGraphicsExtractor graphics, int centerX, int bottomY, Minecraft client) {
        if (client.player == null) return;

        float currentHealth = client.player.getHealth() + client.player.getAbsorptionAmount();
        float maxHealth = client.player.getMaxHealth() + client.player.getAbsorptionAmount();
        int tint = getHeartTintSurvival(client);

        int heartWidth = 36;
        int heartHeight = 36;
        int totalSlots = 24;
        float hpPerSlot = maxHealth / totalSlots;
        int lostSlots = totalSlots - (int) Math.ceil(currentHealth / hpPerSlot);
        float pixelsPerSlot = heartHeight / (float) totalSlots;
        int visibleHeight = heartHeight - Math.round(lostSlots * pixelsPerSlot);

        int frameX = centerX - 21;
        int frameY = bottomY - 13;
        int heartX = centerX - 18;
        int heartY = frameY + 3;

        graphics.blit(RenderPipelines.GUI_TEXTURED, FRAME,
                frameX, frameY,
                0, 234,
                42, 42,
                310, 271,
                512, 512
        );

        if (visibleHeight > 0) {
            graphics.enableScissor(
                    heartX,
                    heartY + (heartHeight - visibleHeight),
                    heartX + heartWidth,
                    heartY + heartHeight
            );
            graphics.blit(RenderPipelines.GUI_TEXTURED, HEARTH,
                    heartX, heartY,
                    0, 202,
                    heartWidth, heartHeight,
                    257, 202,
                    257, 606, tint
            );
            graphics.disableScissor();
        }
    }

    private static int getHeartTintSurvival(Minecraft client) {
        if (client == null || client.player == null) return hexToArgb("#ff1313");
        var player = client.player;
        if (player.hasEffect(MobEffects.POISON)) return hexToArgb("#8b8712");
        if (player.hasEffect(MobEffects.WITHER)) return hexToArgb("#2b2b2b");
        if (player.hasEffect(MobEffects.ABSORPTION)) return hexToArgb("#ffec00");
        if (player.isFullyFrozen()) return hexToArgb("#80e5ef");
        return hexToArgb("#ff1313");
    }

    private static void renderHotbarItems(GuiGraphicsExtractor graphics, int centerX, int bottomY,
                                          int[] offsetX, int[] offsetY, Inventory inv) {
        Minecraft client = Minecraft.getInstance();
        int[] slotPositionsX = getSlotPositionsX(centerX);
        int slotY = bottomY + 4;

        float slotSize = 15f;
        float defaultItemSize = 16f;
        float itemScale = slotSize / defaultItemSize;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            int x = slotPositionsX[i] + offsetX[i];
            int y = slotY + offsetY[i];

            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(itemScale, itemScale);

            graphics.fakeItem(stack, 0, 0);
            graphics.itemDecorations(client.font, stack, 0, 0);

            if (stack.getCount() > 1) {
                String countText = String.valueOf(stack.getCount());
                graphics.text(client.font, countText,
                        x + 18 - client.font.width(countText), y + 13,
                        0xFFFFFFFF, true);
            }

            if (stack.isDamaged()) {
                float damage = (float) stack.getDamageValue() / (float) stack.getMaxDamage();
                int barWidth = Math.round(19.0F - damage * 19.0F);
                int barColor = stack.getBarColor();
                graphics.fill(x + 3, y + 19, x + 3 + barWidth, y + 21, barColor | 0xFF000000);
            }

            graphics.pose().popMatrix();
        }
    }

    private static void renderVanillaOffHand(GuiGraphicsExtractor graphics, int centerX, int bottomY, Minecraft client) {
        if (client.player == null) return;
        ItemStack offHand = client.player.getOffhandItem();
        if (offHand.isEmpty()) return;

        int baseX = centerX - 63 - 46 - 20;
        int baseY = bottomY + 4;

        float slotSize = 15f;
        float defaultItemSize = 16f;
        float itemScale = slotSize / defaultItemSize;

        graphics.pose().pushMatrix();
        graphics.pose().translate(baseX, baseY);
        graphics.pose().scale(itemScale, itemScale);

        graphics.fakeItem(offHand, 0, 0);
        graphics.itemDecorations(client.font, offHand, 0, 0);

        if (offHand.getCount() > 1) {
            String countText = String.valueOf(offHand.getCount());
            graphics.text(client.font, countText,
                    18 - client.font.width(countText), 13,
                    0xFFFFFFFF, true);
        }

        if (offHand.isDamaged()) {
            float damage = (float) offHand.getDamageValue() / (float) offHand.getMaxDamage();
            int barWidth = Math.round(19.0F - damage * 19.0F);
            int barColor = offHand.getBarColor();
            graphics.fill(3, 19, 3 + barWidth, 21, barColor | 0xFF000000);
        }

        graphics.pose().popMatrix();
    }

    private static void renderSelectedSlot(GuiGraphicsExtractor graphics, int centerX, int bottomY, Inventory inv) {
        int selected = inv.getSelectedSlot();

        int u = 97, v = 153;
        int width = 21, height = 21;
        int selWidth = 103, selHeight = 103;
        int texWidth = 1836, texHeight = 256;

        int slotX = getSlotPositionsX(centerX)[selected];
        int slotY = bottomY + 3;

        graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, slotX, slotY,
                u, v, width, height,
                selWidth, selHeight, texWidth, texHeight);
    }

    private static int[] getSlotPositionsX(int centerX) {
        int[] slotPositionsX = new int[9];
        slotPositionsX[0] = centerX - 63 - 46;
        slotPositionsX[1] = centerX - 40 - 46;
        slotPositionsX[2] = centerX - 18 - 46;
        slotPositionsX[3] = centerX - 43;
        slotPositionsX[4] = centerX + 46 - 25;
        slotPositionsX[5] = centerX + 46 + 21 - 24;
        slotPositionsX[6] = centerX + 46 + 42 - 22;
        slotPositionsX[7] = centerX + 46 + 63 - 22;
        slotPositionsX[8] = centerX + 46 + 84 - 21;
        return slotPositionsX;
    }

    private static void renderItemName(GuiGraphicsExtractor graphics, Minecraft client, int frameX, int frameY, int frameWidth) {
        if (client.player == null) return;
        ItemStack stack = client.player.getMainHandItem();
        if (stack.isEmpty()) return;
        String name = stack.getHoverName().getString();
        int nameX = frameX + frameWidth / 2 - client.font.width(name) / 2;
        int nameY = frameY - 12;
        graphics.text(client.font, name, nameX, nameY, 0xFFFFFFFF, true);
    }
}