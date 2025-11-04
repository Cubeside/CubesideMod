package de.fanta.cubeside.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.fanta.cubeside.ChatInfoHud;
import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.data.ChatDatabase;
import de.fanta.cubeside.util.ChatHudMethods;
import de.fanta.cubeside.util.ChatUtils;
import de.iani.cubesideutils.fabric.permission.PermissionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
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

@Mixin(ChatComponent.class)
public abstract class MixinChatHud implements ChatHudMethods {
    @Unique
    private static final Date DATE = new Date();
    @Unique
    private Component lastMessage;
    @Unique
    private Component lastEditMessage;
    @Unique
    private int count = 1;
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

    @Redirect(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;logChatMessage(Lnet/minecraft/client/GuiMessage;)V"))
    private void addMessage(ChatComponent instance, GuiMessage message) {
        if (!CubesideClientFabric.isLoadingMessages()) {
            logChatMessage(message);
        }
    }

    @Shadow
    public abstract void addRecentChat(String message);

    @Shadow
    public abstract void logChatMessage(GuiMessage message);

    @Shadow
    @Final
    public List<GuiMessage.Line> trimmedMessages;

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract double getScale();

    @Shadow
    @Final
    public List<GuiMessage> allMessages;

    @Shadow
    public abstract void addMessageToDisplayQueue(GuiMessage message);

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void renderChatHudInfo(GuiGraphics context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        if (focused) {
            chatInfoHud = chatInfoHud != null ? chatInfoHud : new ChatInfoHud();
            chatInfoHud.onRenderChatInfoHud(context);
        }
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), argsOnly = true)
    private Component modifyMessages(Component componentIn) {
        if (CubesideClientFabric.isLoadingMessages()) {
            CubesideClientFabric.messageQueue.add(componentIn);
            return Component.empty();
        }

        if (Configs.Chat.CountDuplicateMessages.getBooleanValue()) {
            if (lastMessage != null && lastMessage.equals(componentIn)) {
                count++;
                lastMessage = componentIn;

                MutableComponent text = lastMessage.copy();
                MutableComponent countText = Component.literal(String.format(Configs.Chat.CountDuplicateMessagesFormat.getStringValue(), count));
                countText.setStyle(Style.EMPTY.withColor(TextColor.parseColor(Configs.Chat.CountDuplicateMessagesColor.getColor().toHexString()).result().get()));
                text.append(countText);
                componentIn = text;

                if (lastEditMessage != null) {
                    int with = Mth.floor(this.getWidth() / this.getScale());
                    List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(lastEditMessage, with, this.minecraft.font);
                    for (int i = 1; i <= list.size(); i++) {
                        if (!this.trimmedMessages.isEmpty()) {
                            this.trimmedMessages.removeFirst();
                        }
                        if (!this.allMessages.isEmpty()) {
                            this.allMessages.removeFirst();
                        }
                    }
                    if (CubesideClientFabric.getChatDatabase() != null) {
                        try {
                            CubesideClientFabric.getChatDatabase().deleteNewestMessage();
                        } catch (Throwable e) {
                            CubesideClientFabric.LOGGER.log(Level.WARN, "Could not delete latest message from Database " + e.getMessage());
                        }
                    }
                }

            } else {
                lastMessage = componentIn;
                count = 1;
            }
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
                            minecraft.player.playNotifySound(SoundEvent.createVariableRangeEvent(ResourceLocation.parse("block.note_block.flute")), SoundSource.PLAYERS, 20.0f, 0.5f);
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
                            minecraft.player.playNotifySound(SoundEvent.createVariableRangeEvent(ResourceLocation.parse("block.note_block.flute")), SoundSource.PLAYERS, 20.0f, 0.5f);
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
            addMessageToDatabase(component);
            componentIn = component;
        } else {
            addMessageToDatabase(componentIn);
        }

        if (Configs.Chat.CountDuplicateMessages.getBooleanValue()) {
            lastEditMessage = componentIn;
        }
        return componentIn;
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
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
        this.addMessageToDisplayQueue(new GuiMessage(0, message, null, new GuiMessageTag(10631423, null, Component.literal("*"), null)));
    }

    @Override
    public void cubesideMod$addStoredCommand(String message) {
        this.addRecentChat(message);
    }

    @Unique
    public void playAFKSound() {
        if (minecraft.player != null) {
            SoundEvent sound = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CubesideClientFabric.MODID, "afk"));
            minecraft.player.playNotifySound(sound, SoundSource.PLAYERS, 0.2f, 1.0f);
        }
    }

    @Unique
    public void addMessageToDatabase(Component component) {
        if (Configs.Chat.SaveMessagesToDatabase.getBooleanValue()) {
            ChatDatabase chatDatabase = CubesideClientFabric.getChatDatabase();
            if (chatDatabase != null) {
                ClientLevel world = minecraft.level;
                if (world != null) {
                    try {
                        RegistryOps<JsonElement> ops = world.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                        chatDatabase.addMessageEntry(ComponentSerialization.CODEC.encode(component, ops, ops.empty()).getOrThrow().toString());
                    } catch (Throwable e) {
                        CubesideClientFabric.LOGGER.log(Level.WARN, "Message can not save to Database " + e.getMessage());
                    }
                }
            }
        }
    }
}
