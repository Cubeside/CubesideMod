package de.fanta.cubeside.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyboardHandler.class)
public class MixinKeyboard {
    // FIXME 1.21.11
    // @ModifyConstant(method = "keyPress", constant = @Constant(intValue = 66))
    // private int narratorkey(int old) {
    // return KeyBindingHelper.getBoundKeyOf(KeyBinds.NARRATOR_KEYBINDING).getValue();
    // }
    //
    // @ModifyConstant(method = "keyPress", constant = @Constant(intValue = 292))
    // private int f3Key(int old) {
    // return KeyBindingHelper.getBoundKeyOf(KeyBinds.F3_KEYBINDING).getValue();
    // }
}
