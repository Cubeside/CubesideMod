package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public abstract class MixinCustomTitleScreen extends Screen {

    protected MixinCustomTitleScreen(Component title) {
        super(title);
    }

    @Unique
    private ServerData selectedEntry;

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        selectedEntry = new ServerData(Configs.Generic.FastJoinButtonIP.getStringValue(), Configs.Generic.FastJoinButtonIP.getStringValue(), ServerData.Type.OTHER);
    }

    @Inject(at = @At("HEAD"), method = "createNormalMenuOptions")
    private void addCustomButton(int y, int spacingY, CallbackInfoReturnable<Integer> cir) {
        if (!Configs.Generic.FastJoinButtonText.getStringValue().isBlank() && !Configs.Generic.FastJoinButtonIP.getStringValue().isBlank()) {
            this.addRenderableWidget(Button.builder(Component.literal(Configs.Generic.FastJoinButtonText.getStringValue()), button -> {
                if (selectedEntry != null) {
                    ConnectScreen.startConnecting(this, Minecraft.getInstance(), new ServerAddress(Configs.Generic.FastJoinButtonIP.getStringValue(), Configs.Generic.FastJoinButtonPort.getIntegerValue()), selectedEntry, false, null);
                }
            }).bounds(this.width / 2 - 100 + 205, y + spacingY, 80, 20).build());
        }
    }

}
