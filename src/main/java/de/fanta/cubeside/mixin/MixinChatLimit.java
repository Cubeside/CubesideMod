package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatComponent.class)
public class MixinChatLimit {

    @ModifyConstant(method = "addMessageToQueue(Lnet/minecraft/client/GuiMessage;)V", constant = { @Constant(intValue = 100) })
    private int replaceMessageLimit(int original) {
        return Configs.Chat.ChatMessageLimit.getIntegerValue();
    }

    @ModifyConstant(method = "addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V", constant = { @Constant(intValue = 100) })
    private int replaceVisibleMessageLimit(int original) {
        return Configs.Chat.ChatMessageLimit.getIntegerValue();
    }

    @ModifyConstant(method = "addRecentChat(Ljava/lang/String;)V", constant = { @Constant(intValue = 100) })
    private int replaceMessageHistoryLimit(int original) {
        return Configs.Chat.ChatMessageLimit.getIntegerValue();
    }
}
