package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class MixinGameModeSwitch {

    @Inject(at = @At("HEAD"), method = "handleDebugKeys(Lnet/minecraft/client/input/KeyEvent;)Z", cancellable = true)
    public void behaviour(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (Configs.Generic.GamemodeSwitcher.getBooleanValue()) {
            if (input.input() == 293) {
                Minecraft.getInstance().setScreen(new GameModeSwitcherScreen());
                cir.setReturnValue(true);
            }
        }
    }
}
