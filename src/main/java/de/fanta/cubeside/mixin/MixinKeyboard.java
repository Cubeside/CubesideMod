package de.fanta.cubeside.mixin;

import de.fanta.cubeside.KeyBinds;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(KeyboardHandler.class)
public class MixinKeyboard {

    @ModifyConstant(method = "keyPress", constant = @Constant(intValue = 66))
    private int narratorkey(int old) {
        return KeyBindingHelper.getBoundKeyOf(KeyBinds.NARRATOR_KEYBINDING).getValue();
    }

    @ModifyConstant(method = "keyPress", constant = @Constant(intValue = 292))
    private int f3Key(int old) {
        return KeyBindingHelper.getBoundKeyOf(KeyBinds.F3_KEYBINDING).getValue();
    }
}
