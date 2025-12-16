package de.fanta.cubeside;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerList;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.data.ChatDatabase;
import de.fanta.cubeside.util.ChatHudMethods;
import de.fanta.cubeside.util.ChatUtils;
import de.fanta.cubeside.util.SoundThread;
import de.iani.cubesideutils.fabric.permission.PermissionHandler;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.Level;

public class Events {

    private boolean flyingLastTick = false;
    private CameraType lastmode = CameraType.FIRST_PERSON;
    private SoundEvent sound;

    private SoundThread soundThread;

    private boolean connect;

    private LocalDate lastTickDate;

    public Events() {
    }

    public void init() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            if (Configs.Chat.SaveMessagesToDatabase.getBooleanValue() && !CubesideClientFabric.databaseinuse) {
                if (!connect) {
                    String worldName;
                    if (client.hasSingleplayerServer()) {
                        worldName = getMapName(client);
                    } else {
                        worldName = getServerName(handler);
                    }

                    if (worldName == null) {
                        CubesideClientFabric.LOGGER.log(Level.WARN, "WorldName is Null :(");
                        return;
                    }
                    if (worldName.endsWith(":25565")) {
                        int portSepLoc = worldName.lastIndexOf(':');
                        if (portSepLoc != -1) {
                            worldName = worldName.substring(0, portSepLoc);
                        }
                    }

                    String finalWorldName = scrubNameFile(worldName);
                    connect = true;
                    CubesideClientFabric.setLoadingMessages(true);

                    try {
                        CubesideClientFabric.setChatDatabase(new ChatDatabase(finalWorldName, handler.registryAccess()));

                        List<Component> messages = CubesideClientFabric.getChatDatabase().loadMessages(Configs.Chat.ChatMessageLimit.getIntegerValue());
                        List<String> commands = CubesideClientFabric.getChatDatabase().loadCommands();

                        client.gui.getChat().clearMessages(true);
                        if (messages != null) {
                            messages.forEach(((ChatHudMethods) client.gui.getChat())::cubesideMod$addStoredChatMessage);
                        }
                        if (commands != null) {
                            commands.forEach(((ChatHudMethods) client.gui.getChat())::cubesideMod$addStoredCommand);
                        }
                    } finally {
                        CubesideClientFabric.setLoadingMessages(false);

                        CubesideClientFabric.messageQueue.forEach(text -> client.gui.getChat().addMessage(text));
                        CubesideClientFabric.messageQueue.clear();

                    }

                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ChatDatabase chatDatabase = CubesideClientFabric.getChatDatabase();
            if (chatDatabase != null) {
                chatDatabase.close();
                CubesideClientFabric.setChatDatabase(null);
            }

            if (Configs.Chat.SaveMessagesToDatabase.getBooleanValue()) {
                connect = false;
            }
            CubesideClientFabric.setChatInfo(null);
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player != null) {
                if (Configs.Generic.ThirdPersonElytra.getBooleanValue()) {
                    if (mc.player.isFallFlying()) {
                        if (!flyingLastTick) {
                            flyingLastTick = true;
                            lastmode = mc.options.getCameraType();
                            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                        }
                    } else {
                        if (flyingLastTick) {
                            flyingLastTick = false;
                            mc.options.setCameraType(lastmode);
                        }
                    }
                }

                if (Configs.Generic.ElytraAlarm.getBooleanValue()) {
                    if (sound == null) {
                        Identifier location = Identifier.fromNamespaceAndPath(CubesideClientFabric.MODID, "alarm");
                        sound = SoundEvent.createVariableRangeEvent(location);
                    }
                    if (mc.player.isFallFlying() && mc.player.getY() <= mc.level.getMinY()) {
                        if (soundThread == null) {
                            soundThread = SoundThread.of(1944, sound, mc.player);
                            soundThread.start();
                        }
                    } else if (soundThread != null && soundThread.isRunning()) {
                        soundThread.stopThread();
                        soundThread = null;
                    }
                }

                while (KeyBinds.AUTO_CHAT.consumeClick()) {
                    if (PermissionHandler.hasPermission("cubeside.autochat")) {
                        if (Configs.PermissionSettings.AutoChat.getBooleanValue()) {
                            Configs.PermissionSettings.AutoChat.setBooleanValue(false);
                            mc.player.displayClientMessage(Component.nullToEmpty("§cAuto Chat deaktiviert"), true);
                        } else {
                            Configs.PermissionSettings.AutoChat.setBooleanValue(true);
                            mc.player.displayClientMessage(Component.nullToEmpty("§aAuto Chat aktiviert"), true);
                        }
                        Configs.saveToFile();
                    } else {
                        ChatUtils.sendErrorMessage("AutoChat kannst du erst ab Staff benutzen!");
                    }
                }

                while (KeyBinds.TOGGLE_SHOW_ENTITIES_IN_SPECTATOR_MODE.consumeClick()) {
                    if (Configs.Generic.ShowInvisibleEntitiesInSpectator.getBooleanValue()) {
                        Configs.Generic.ShowInvisibleEntitiesInSpectator.setBooleanValue(false);
                        mc.player.displayClientMessage(Component.nullToEmpty("§aUnsichtbare Entities werden jetzt im Spectator nicht mehr angezeigt!"), true);
                    } else {
                        Configs.Generic.ShowInvisibleEntitiesInSpectator.setBooleanValue(true);
                        mc.player.displayClientMessage(Component.nullToEmpty("§aUnsichtbare Entities werden jetzt im Spectator wieder angezeigt!"), true);
                    }
                    Configs.saveToFile();
                }

                while (KeyBinds.SET_MINING_ASSISTANT_START_POINT.consumeClick()) {
                    MiningAssistent.setStartPos(Minecraft.getInstance().player.blockPosition());
                    mc.player.displayClientMessage(Component.nullToEmpty("MiningAssistent start position set"), true);
                }

                while (KeyBinds.TOGGLE_MINING_ASSISTANT.consumeClick()) {
                    Configs.MiningAssistent.MiningAssistentEnabled.setBooleanValue(!Configs.MiningAssistent.MiningAssistentEnabled.getBooleanValue());
                    Configs.saveToFile();
                    mc.player.displayClientMessage(Component.nullToEmpty("MiningAssistent set to: " + (Configs.MiningAssistent.MiningAssistentEnabled.getBooleanValue() ? "§atrue" : "§cfalse")), true);
                }

                while (KeyBinds.WOOD_STRIPING.consumeClick()) {
                    Configs.Generic.WoodStriping.setBooleanValue(!Configs.Generic.WoodStriping.getBooleanValue());
                    Configs.saveToFile();
                    mc.player.displayClientMessage(Component.nullToEmpty("WoodStriping set to: " + (Configs.Generic.WoodStriping.getBooleanValue() ? "§atrue" : "§cfalse")), true);
                }

                while (KeyBinds.CREATE_GRASS_PATH.consumeClick()) {
                    Configs.Generic.CreateGrassPath.setBooleanValue(!Configs.Generic.CreateGrassPath.getBooleanValue());
                    Configs.saveToFile();
                    mc.player.displayClientMessage(Component.nullToEmpty("CreateGrassPath set to: " + (Configs.Generic.CreateGrassPath.getBooleanValue() ? "§atrue" : "§cfalse")), true);
                }

                while (KeyBinds.EDIT_SIGN.consumeClick()) {
                    Configs.Generic.SignEdit.setBooleanValue(!Configs.Generic.SignEdit.getBooleanValue());
                    Configs.saveToFile();
                    mc.player.displayClientMessage(Component.nullToEmpty("SignEdit set to: " + (Configs.Generic.SignEdit.getBooleanValue() ? "§atrue" : "§cfalse")), true);
                }
            }

            if (Configs.Chat.SaveMessagesToDatabase.getBooleanValue()) {
                LocalDate date = LocalDate.now();
                if (lastTickDate != null) {
                    if (!lastTickDate.isEqual(date)) {
                        ChatUtils.sendNormalMessage("Neuer Tag: " + date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    }
                }
                lastTickDate = date;
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            CubesideClientFabric.commands.register(dispatcher);
        });

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> MiningAssistent.render(context.matrices()));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack itemInHand = player.getItemInHand(hand);
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (!Configs.Generic.WoodStriping.getBooleanValue() && itemInHand.getItem() instanceof AxeItem && AxeItem.STRIPPABLES.containsKey(blockState.getBlock())) {
                return InteractionResult.FAIL;
            }

            if (!Configs.Generic.CreateGrassPath.getBooleanValue() && itemInHand.getItem() instanceof ShovelItem && ShovelItem.FLATTENABLES.containsKey(blockState.getBlock())) {
                return InteractionResult.FAIL;
            }

            if (!Configs.Generic.SignEdit.getBooleanValue() && blockState.getBlock() instanceof SignBlock) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }

    public String getMapName(Minecraft client) {
        Optional<IntegratedServer> integratedServer = Optional.ofNullable(client.getSingleplayerServer());

        if (integratedServer.isEmpty()) {
            String error = "Tried fetching map name on a non-integrated server!";
            CubesideClientFabric.LOGGER.fatal(error);
            throw new IllegalStateException(error);
        }

        return integratedServer.get().getWorldPath(LevelResource.ROOT).normalize().toFile().getName();
    }

    public String getServerName(ClientPacketListener networkHandler) {
        String serverName = null;

        try {
            ServerData serverInfo = networkHandler.getServerData();
            serverInfo = serverInfo != null ? serverInfo : Minecraft.getInstance().getCurrentServer();
            boolean isRealm = serverInfo != null && serverInfo.isRealm();
            if (serverInfo != null) {
                boolean isOnLAN = serverInfo.isLan();
                if (isOnLAN) {
                    CubesideClientFabric.LOGGER.warn("LAN server detected!");
                    serverName = serverInfo.name;
                } else if (isRealm) {
                    CubesideClientFabric.LOGGER.info("Server is a Realm.");
                    RealmsClient realmsClient = RealmsClient.getOrCreate(Minecraft.getInstance());
                    RealmsServerList realmsServerList = realmsClient.listRealms();
                    for (RealmsServer realmsServer : realmsServerList.servers()) {
                        if (realmsServer.name.equals(serverInfo.name)) {
                            serverName = "Realm_" + realmsServer.id + "." + realmsServer.ownerUUID;
                            break;
                        }
                    }
                } else {
                    serverName = serverInfo.ip;
                }
            }
        } catch (Exception var6) {
            CubesideClientFabric.LOGGER.error("error getting ServerData", var6);
        }

        return serverName;
    }

    public static String scrubNameFile(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("<", "~less~")
                .replace(">", "~greater~")
                .replace(":", "~colon~")
                .replace("\"", "~quote~")
                .replace("/", "~slash~")
                .replace("\\", "~backslash~")
                .replace("|", "~pipe~")
                .replace("?", "~question~")
                .replace("*", "~star~");
    }
}
