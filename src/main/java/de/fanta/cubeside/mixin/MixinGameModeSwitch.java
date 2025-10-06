package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameModeSwitcherScreen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public class MixinGameModeSwitch {

    @Inject(at = @At("HEAD"), method = "processF3(Lnet/minecraft/client/input/KeyInput;)Z", cancellable = true)
    public void behaviour(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (Configs.Generic.GamemodeSwitcher.getBooleanValue()) {
            if (input.getKeycode() == 293) {
                MinecraftClient.getInstance().setScreen(new GameModeSwitcherScreen());
                cir.setReturnValue(true);
            }
        }
    }
}
