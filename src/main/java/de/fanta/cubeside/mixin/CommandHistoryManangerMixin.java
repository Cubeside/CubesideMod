package de.fanta.cubeside.mixin;

import de.fanta.cubeside.config.Configs;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.client.CommandHistory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandHistory.class)
public class CommandHistoryManangerMixin {

    @Inject(method = "history", at = @At(value = "FIELD", target = "Lnet/minecraft/client/CommandHistory;lastCommands:Lnet/minecraft/util/ArrayListDeque;"), cancellable = true)
    private void getHistory(CallbackInfoReturnable<Collection<String>> cir) {
        if (Configs.Chat.SaveMessagesToDatabase.getBooleanValue()) {
            cir.setReturnValue(new ArrayList<>());
        }
    }
}
