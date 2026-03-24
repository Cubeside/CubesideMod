package de.fanta.cubeside.mixin;

import de.fanta.cubeside.KeyBinds;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(KeyboardHandler.class)
public class MixinKeyboard {
    @ModifyConstant(method = "keyPress", constant = @Constant(intValue = 66))
    private int narratorkey(int old) {
        return KeyMappingHelper.getBoundKeyOf(KeyBinds.NARRATOR_KEYBINDING).getValue();
    }
}
