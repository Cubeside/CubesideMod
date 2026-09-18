package de.fanta.cubeside.mixin;

import de.fanta.cubeside.KeyBinds;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardHandler.class)
public class MixinKeyboard {
    @Redirect(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/KeyEvent;shortcutKey()I", ordinal = 1))
    private int narratorShortcutKey(KeyEvent event) {
        return KeyBinds.NARRATOR_KEYBINDING.matches(event) ? 'b' : -1;
    }
}
