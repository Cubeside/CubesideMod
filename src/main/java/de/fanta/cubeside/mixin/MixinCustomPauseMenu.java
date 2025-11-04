package de.fanta.cubeside.mixin;

import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.config.ConfigGui;
import de.fanta.cubeside.data.SearchScreen;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class MixinCustomPauseMenu extends Screen {

    protected MixinCustomPauseMenu(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "createPauseMenu")
    private void addCustomButton(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.translatable("custombutton.cubeside.options"), button -> GuiBase.openGui(new ConfigGui())).bounds(this.width / 2 - 100 + 205, this.height / 4 + 72 - 15, 100, 20).build());
        if (CubesideClientFabric.getChatDatabase() != null) {
            if (minecraft == null) {
                return;
            }

            ClientLevel world = this.minecraft.level;
            if (world != null) {
                this.addRenderableWidget(Button.builder(Component.literal("ChatLog (Beta)"), button -> minecraft.setScreen(new SearchScreen(this, world.registryAccess()))).bounds(this.width / 2 - 100 + 205, this.height / 4 + 72 - 16 + 25, 100, 20).build());
            }
        }
    }

}
