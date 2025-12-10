package de.fanta.cubeside.mixin;

import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameModeSwitcherScreen.class)
public abstract class MixinGameModeSelectionScreen {
    // FIXME 1.21.11
    // @Redirect(method = "switchToHoveredGameMode(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z"))
    // private static boolean returnFakePermissionCheck(LocalPlayer clientPlayerEntity, int permissionLevel) {
    // return true;
    // }
}
