package com.dungeonhotbar.mixin;

import com.dungeonhotbar.DungeonHotbar;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    private static boolean logged = false;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        if (!logged) {
            DungeonHotbar.LOGGER.info("[DungeonHotbar] Vanilla hotbar rendering cancelled");
            logged = true;
        }
        ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaStatusBars(DrawContext context, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void cancelExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void cancelStatusEffects(DrawContext context, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void cancelMountHealth(DrawContext context, CallbackInfo ci) {
        ci.cancel();
    }

    // Отключаем ванильное название предмета над хотбаром
    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    private void cancelHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        ci.cancel();
    }
}