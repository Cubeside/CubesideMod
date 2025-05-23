package de.fanta.cubeside.mixin;

import net.minecraft.client.gui.screen.GameModeSwitcherScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameModeSwitcherScreen.class)
public abstract class MixinGameModeSelectionScreen {

    @Redirect(method = "apply(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/GameModeSwitcherScreen$GameModeSelection;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasPermissionLevel(I)Z"))
    private static boolean returnFakePermissionCheck(ClientPlayerEntity clientPlayerEntity, int permissionLevel) {
        return true;
    }
}
