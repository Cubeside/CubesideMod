package de.fanta.cubeside.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.fanta.cubeside.ChatInfoHud;
import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.chat.DuplicateMessageFormatter;
import de.fanta.cubeside.chat.DuplicateMessageTracker;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.data.ChatDatabase;
import de.fanta.cubeside.util.ChatHudMethods;
import de.fanta.cubeside.util.ChatUtils;
import de.iani.cubesideutils.fabric.permission.PermissionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public abstract class MixinChatHud implements ChatHudMethods {
    @Unique
    private static final Date DATE = new Date();
    @Unique
    private final DuplicateMessageTracker duplicateMessageTracker = new DuplicateMessageTracker();
    @Unique
    private Component pendingOriginalMessage;
    @Unique
    private GuiMessage pendingVisibleMessage;
    @Unique
    private DuplicateMessageTracker.Decision pendingDuplicateDecision;
    @Unique
    private boolean pendingAggregationEnabled;
    @Unique
    private static boolean invalidDuplicateFormatWarningLogged;
    @Unique
    private static ChatInfoHud chatInfoHud;
    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private static String getChatTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss]");
        DATE.setTime(System.currentTimeMillis());
        return sdf.format(DATE);
    }

    @Redirect(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;logChatMessage(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V"))
    private void prepareVisibleMessage(ChatComponent instance, GuiMessage message) {
        Component originalMessage = pendingOriginalMessage != null ? pendingOriginalMessage : message.content();
        DuplicateMessageTracker.Key key = new DuplicateMessageTracker.Key(originalMessage.copy(), message.source(), message.tag());
        DuplicateMessageTracker.Decision decision;
        pendingAggregationEnabled = Configs.Chat.CountDuplicateMessages.getBooleanValue();
        if (pendingAggregationEnabled) {
            decision = duplicateMessageTracker.prepareVisible(key, allMessages, trimmedMessages);
        } else {
            duplicateMessageTracker.reset();
            decision = new DuplicateMessageTracker.Decision(key, 1, false, false, 0);
        }

        GuiMessage visibleMessage = decision.duplicate() ? withDuplicateCount(message, decision.count()) : message;
        pendingDuplicateDecision = decision;
        pendingVisibleMessage = visibleMessage;
        logChatMessage(visibleMessage);
    }

    @Shadow
    public abstract void addRecentChat(String message);

    @Shadow
    public abstract void logChatMessage(GuiMessage message);

    @Shadow
    @Final
    public List<GuiMessage.Line> trimmedMessages;

    @Shadow
    @Final
    public List<GuiMessage> allMessages;

    @Invoker("addMessageToDisplayQueue")
    protected abstract void cubesideMod$invokeAddMessageToDisplayQueue(GuiMessage message);

    @Invoker("addMessageToQueue")
    protected abstract void cubesideMod$invokeAddMessageToQueue(GuiMessage message);

    @Shadow
    public abstract void scrollChat(int amount);

    @Inject(method = "extractRenderState", at = @At(value = "RETURN"))
    private void renderChatHudInfo(GuiGraphicsExtractor context, Font font, int currentTick, int mouseX, int mouseY, ChatComponent.DisplayMode displayMode, boolean changeCursorOnInsertions, CallbackInfo ci) {
        if (displayMode.foreground) {
            chatInfoHud = chatInfoHud != null ? chatInfoHud : new ChatInfoHud();
            chatInfoHud.onRenderChatInfoHud(context);
        }
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At("HEAD"), argsOnly = true)
    private Component modifyMessages(Component componentIn) {
        if (CubesideClientFabric.isLoadingMessages()) {
            return componentIn;
        }

        Component originalMessage = componentIn;
        if (!Configs.Chat.CountDuplicateMessages.getBooleanValue()) {
            duplicateMessageTracker.reset();
        }

        if (Configs.PermissionSettings.AutoChat.getBooleanValue()) {
            String s = componentIn.toString();
            String[] arr = s.split(" ");

            if (arr.length >= 16) {
                if (arr[7].equals("literal{From") && arr[8].equals("}[style={color=light_purple}],") && (arr[16].contains("style={color=white}") || arr[16].contains("style={color=green}"))) {
                    if (minecraft.player != null) {
                        if (PermissionHandler.hasPermission("cubeside.autochat")) {
                            minecraft.player.connection.sendCommand("r " + Configs.PermissionSettings.AutoChatAntwort.getStringValue());
                        } else {
                            ChatUtils.sendErrorMessage("AutoChat kannst du erst ab Staff benutzen!");
                        }
                    }

                }
            }
        }

        if (Configs.Generic.AFKPling.getBooleanValue()) {
            String AFKMessage = componentIn.getString();
            if (AFKMessage.equals("* Du bist nun abwesend.")) {
                playAFKSound();
            }
        }

        if (Configs.Generic.ClickableTpaMessage.getBooleanValue()) {
            String tpamessage = componentIn.getString();
            String[] args2 = tpamessage.split(" ", 2);
            String[] args5 = tpamessage.split(" ", 5);
            String[] args6 = tpamessage.split(" ", 6);
            MutableComponent component = Component.literal("");
            if (args2.length == 2) {
                MutableComponent name = Component.literal(args2[0]);
                name.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff592").result().get()));
                MutableComponent accept = Component.literal("[Annehmen]");
                accept.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#119e1d").result().get()).withClickEvent(new ClickEvent.RunCommand("/tpaccept")));
                MutableComponent deny = Component.literal(" [Ablehnen]");
                deny.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#9e1139").result().get()).withClickEvent(new ClickEvent.RunCommand("/tpdeny")));

                if (args2[1].startsWith("fragt, ob er sich zu dir teleportieren darf.")) {
                    component.append(name);
                    MutableComponent message = Component.literal(" möchte sich zu dir teleportieren.\n");
                    message.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message);
                    component.append(accept);
                    component.append(deny);

                    if (Configs.Generic.TpaSound.getBooleanValue()) {
                        if (minecraft.player != null) {
                            Vec3 pos = minecraft.player.position();
                            minecraft.level.playLocalSound(pos.x, pos.y, pos.z, SoundEvent.createVariableRangeEvent(Identifier.parse("block.note_block.flute")), SoundSource.PLAYERS, 20.0f, 0.5f, false);
                        }
                    }

                    componentIn = component;
                }

                if (args2[1].startsWith("fragt, ob du dich zu ihm teleportieren möchtest.")) {
                    component.append(name);
                    MutableComponent message = Component.literal(" möchte, dass du dich zu ihm teleportierst.\n");
                    message.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message);
                    component.append(accept);
                    component.append(deny);

                    if (Configs.Generic.TpaSound.getBooleanValue()) {
                        if (minecraft.player != null) {
                            Vec3 pos = minecraft.player.position();
                            minecraft.level.playLocalSound(pos.x, pos.y, pos.z, SoundEvent.createVariableRangeEvent(Identifier.parse("block.note_block.flute")), SoundSource.PLAYERS, 20.0f, 0.5f, false);
                        }
                    }

                    componentIn = component;
                }

                if (args2[1].startsWith("hat deine Teleportierungsanfrage angenommen.")) {
                    component.append(name);
                    MutableComponent message = Component.literal(" hat deine Teleportierungsanfrage");
                    message.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message);
                    MutableComponent message2 = Component.literal(" angenommen.");
                    message2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#119e1d").result().get()));
                    component.append(message2);
                    componentIn = component;
                }

                if (args2[1].startsWith("hat deine Teleportierungsanfrage abgelehnt.")) {
                    component.append(name);
                    MutableComponent message = Component.literal(" hat deine Teleportierungsanfrage");
                    message.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message);
                    MutableComponent message2 = Component.literal(" abgelehnt.");
                    message2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#9e1139").result().get()));
                    component.append(message2);
                    componentIn = component;
                }
            }
            if (args5.length == 5) {
                MutableComponent name = Component.literal(args6[4].replace(".", ""));
                name.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff592").result().get()));

                if (tpamessage.startsWith("Du teleportierst dich zu")) {
                    MutableComponent message1 = Component.literal("Du wirst zu ");
                    message1.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message1);
                    component.append(name);
                    MutableComponent message2 = Component.literal(" teleportiert.");
                    message2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message2);
                    componentIn = component;
                }
            }

            if (args6.length == 6) {
                MutableComponent name = Component.literal(args6[4].replace(".", ""));
                name.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff592").result().get()));
                if (tpamessage.startsWith("Eine Anfrage wurde an")) {
                    MutableComponent message1 = Component.literal("Du hast eine Anfrage an ");
                    message1.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message1);
                    component.append(name);
                    MutableComponent message2 = Component.literal(" gesendet.");
                    message2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message2);
                    componentIn = component;
                }

                if (tpamessage.startsWith("Diese Anfrage wird nach")) {
                    MutableComponent message1 = Component.literal("Diese Anfrage wird in ");
                    message1.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message1);
                    component.append(name);
                    MutableComponent seconds = Component.literal(" Sekunden ");
                    seconds.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff592").result().get()));
                    component.append(seconds);
                    MutableComponent message2 = Component.literal("ablaufen.");
                    message2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                    component.append(message2);
                    componentIn = component;
                }

            }
            if (tpamessage.equals("Teleportation läuft...")) {
                MutableComponent message = Component.literal("Teleportation läuft...");
                message.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                component.append(message);
                componentIn = component;
            }

            if (tpamessage.equals("Du hast die Teleportierungsanfrage abgelehnt.")) {
                MutableComponent message1 = Component.literal("Du hast die Teleportierungsanfrage");
                message1.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                component.append(message1);
                MutableComponent message2 = Component.literal(" abgelehnt.");
                message2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#9e1139").result().get()));
                component.append(message2);
                componentIn = component;
            }

            if (tpamessage.equals("Du hast die Teleportierungsanfrage angenommen.")) {
                MutableComponent message1 = Component.literal("Du hast die Teleportierungsanfrage");
                message1.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                component.append(message1);
                MutableComponent message2 = Component.literal(" angenommen.");
                message2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#119e1d").result().get()));
                component.append(message2);
                componentIn = component;
            }

            if (tpamessage.equals("Fehler: Du hast keine Teleportierungsanfragen.")) {
                MutableComponent message = Component.literal("Fehler: ");
                message.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#9e1139").result().get()));
                component.append(message);
                MutableComponent message2 = Component.literal("Du hast keine Teleportierungsanfrage.");
                message2.setStyle(Style.EMPTY.withColor(TextColor.parseColor("#2ff5db").result().get()));
                component.append(message2);
                componentIn = component;
            }
        }

        if (Configs.Chat.ChatTimeStamps.getBooleanValue()) {
            MutableComponent component = Component.literal("");
            MutableComponent timestamp = Component.literal(getChatTimestamp() + " ");
            timestamp.setStyle(Style.EMPTY.withColor(Configs.Chat.TimeStampColor.getColor().intValue));
            component.append(timestamp);
            component.append(componentIn);
            componentIn = component;
        }

        pendingOriginalMessage = originalMessage;
        pendingVisibleMessage = null;
        pendingDuplicateDecision = null;
        return componentIn;
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    private void queueMessageWhileLoading(Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci) {
        if (CubesideClientFabric.isLoadingMessages()) {
            CubesideClientFabric.messageQueue.add(message);
            clearPendingMessage();
            ci.cancel();
        }
    }

    @Redirect(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToDisplayQueue(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V"))
    private void addPreparedMessageToDisplay(ChatComponent instance, GuiMessage message) {
        GuiMessage visibleMessage = pendingVisibleMessage != null ? pendingVisibleMessage : message;
        cubesideMod$invokeAddMessageToDisplayQueue(visibleMessage);
        if (pendingDuplicateDecision != null && pendingDuplicateDecision.removedLineCount() > 0) {
            scrollChat(-pendingDuplicateDecision.removedLineCount());
        }
    }

    @Redirect(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessageToQueue(Lnet/minecraft/client/multiplayer/chat/GuiMessage;)V"))
    private void addPreparedMessageToQueue(ChatComponent instance, GuiMessage message) {
        GuiMessage visibleMessage = pendingVisibleMessage != null ? pendingVisibleMessage : message;
        cubesideMod$invokeAddMessageToQueue(visibleMessage);

        DuplicateMessageTracker.Decision decision = pendingDuplicateDecision;
        boolean persistenceSucceeded = persistVisibleMessage(visibleMessage, decision != null && decision.duplicate() && decision.previousPersisted());
        if (pendingAggregationEnabled && decision != null) {
            duplicateMessageTracker.commit(decision, visibleMessage, DuplicateMessageTracker.sequencePersisted(decision, persistenceSucceeded));
        }
        clearPendingMessage();
    }

    @Inject(method = "clearMessages(Z)V", at = @At("HEAD"))
    private void resetDuplicateMessageState(boolean clearHistory, CallbackInfo ci) {
        duplicateMessageTracker.reset();
        clearPendingMessage();
    }

    @Inject(method = "addServerSystemMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void addMessage(Component message, CallbackInfo ci) {
        if (Configs.Generic.ClickableTpaMessage.getBooleanValue()) {
            if (message.getString().equals("Du kannst diese Anfrage mit /tpdeny ablehnen.") || message.getString().equals("Du kannst die Teleportationsanfrage mit /tpaccept annehmen.") || message.getString().equals("Du kannst die Anfrage mit /tpacancel ablehnen.")) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "addRecentChat", at = @At("HEAD"))
    private void addMessageHistory(String message, CallbackInfo ci) {
        if (CubesideClientFabric.isLoadingMessages()) {
            return;
        }
        if (Configs.Chat.SaveMessagesToDatabase.getBooleanValue()) {
            ChatDatabase chatDatabase = CubesideClientFabric.getChatDatabase();
            if (chatDatabase != null) {
                try {
                    chatDatabase.addCommandEntry(message);
                } catch (Throwable e) {
                    CubesideClientFabric.LOGGER.log(Level.WARN, "Command can not save to Database " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void cubesideMod$addStoredChatMessage(Component message) {
        GuiMessage storedMessage = new GuiMessage(0, message, null, GuiMessageSource.SYSTEM_SERVER, new GuiMessageTag(10631423, null, Component.literal("*"), null));
        cubesideMod$invokeAddMessageToDisplayQueue(storedMessage);
        cubesideMod$invokeAddMessageToQueue(storedMessage);
    }

    @Override
    public void cubesideMod$addStoredCommand(String message) {
        this.addRecentChat(message);
    }

    @Override
    public void cubesideMod$resetDuplicateMessageState() {
        duplicateMessageTracker.reset();
        clearPendingMessage();
    }

    @Unique
    public void playAFKSound() {
        if (minecraft.player != null) {
            SoundEvent sound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CubesideClientFabric.MODID, "afk"));
            Vec3 pos = minecraft.player.position();
            minecraft.level.playLocalSound(pos.x, pos.y, pos.z, sound, SoundSource.PLAYERS, 0.2f, 1.0f, false);
        }
    }

    @Unique
    private GuiMessage withDuplicateCount(GuiMessage message, int duplicateCount) {
        MutableComponent text = message.content().copy();
        MutableComponent countText = Component.literal(formatDuplicateCount(duplicateCount));
        countText.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Configs.Chat.CountDuplicateMessagesColor.getColor().toVanillaRgb())));
        text.append(countText);
        return new GuiMessage(message.addedTime(), text, message.signature(), message.source(), message.tag());
    }

    @Unique
    private static String formatDuplicateCount(int duplicateCount) {
        DuplicateMessageFormatter.Result result = DuplicateMessageFormatter.format(Configs.Chat.CountDuplicateMessagesFormat.getStringValue(), duplicateCount);
        if (result.usedFallback()) {
            if (!invalidDuplicateFormatWarningLogged) {
                invalidDuplicateFormatWarningLogged = true;
                CubesideClientFabric.LOGGER.log(Level.WARN, "Invalid duplicate message count format; using default", result.error());
            }
        }
        return result.text();
    }

    @Unique
    private boolean persistVisibleMessage(GuiMessage message, boolean replaceNewest) {
        if (!Configs.Chat.SaveMessagesToDatabase.getBooleanValue()) {
            return false;
        }

        ChatDatabase chatDatabase = CubesideClientFabric.getChatDatabase();
        ClientLevel world = minecraft.level;
        if (chatDatabase == null || world == null) {
            return false;
        }

        try {
            RegistryOps<JsonElement> ops = world.registryAccess().createSerializationContext(JsonOps.INSTANCE);
            String serializedMessage = ComponentSerialization.CODEC.encode(message.content(), ops, ops.empty()).getOrThrow().toString();
            if (replaceNewest) {
                chatDatabase.replaceNewestMessage(serializedMessage);
            } else {
                chatDatabase.addMessageEntry(serializedMessage);
            }
            return true;
        } catch (Throwable e) {
            CubesideClientFabric.LOGGER.log(Level.WARN, "Message can not save to Database " + e.getMessage());
            return false;
        }
    }

    @Unique
    private void clearPendingMessage() {
        pendingOriginalMessage = null;
        pendingVisibleMessage = null;
        pendingDuplicateDecision = null;
        pendingAggregationEnabled = false;
    }
}
