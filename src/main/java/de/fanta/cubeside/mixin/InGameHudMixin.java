package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.util.FlashColorScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Redirect(method = "renderOverlayMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"))
    public void render(GuiGraphics instance, Font textRenderer, Component text, int x, int y, int width, int color) {
        if (!Configs.Generic.ActionBarShadow.getBooleanValue()) {
            instance.drawString(textRenderer, text, x, y, color, false);
        } else {
            instance.drawStringWithBackdrop(textRenderer, text, x, y, width, color);
        }
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "RETURN", opcode = Opcodes.GETFIELD, args = { "log=false" }))
    private void beforeRenderDebugScreen(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        FlashColorScreen.onClientTick(context);
    }
}
