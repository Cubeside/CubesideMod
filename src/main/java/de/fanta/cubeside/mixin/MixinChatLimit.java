package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatHud.class)
public class MixinChatLimit {

    @ModifyConstant(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", constant = {@Constant(intValue = 100)})
    private int replaceMessageLimit(int original) {
        return Configs.Chat.ChatMessageLimit.getIntegerValue();
    }

    @ModifyConstant(method = "addVisibleMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", constant = {@Constant(intValue = 100)})
    private int replaceVisibleMessageLimit(int original) {
        return Configs.Chat.ChatMessageLimit.getIntegerValue();
    }

    @ModifyConstant(method = "addToMessageHistory(Ljava/lang/String;)V", constant = {@Constant(intValue = 100)})
    private int replaceMessageHistoryLimit(int original) {
        return Configs.Chat.ChatMessageLimit.getIntegerValue();
    }
}
