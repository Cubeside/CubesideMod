package de.fanta.cubeside;

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
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerList;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

public class Events {

    private boolean flyingLastTick = false;
    private Perspective lastmode = Perspective.FIRST_PERSON;
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
                    if (client.isIntegratedServerRunning()) {
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
                        CubesideClientFabric.setChatDatabase(new ChatDatabase(finalWorldName, handler.getRegistryManager()));

                        List<Text> messages = CubesideClientFabric.getChatDatabase().loadMessages(Configs.Chat.ChatMessageLimit.getIntegerValue());
                        List<String> commands = CubesideClientFabric.getChatDatabase().loadCommands();

                        client.inGameHud.getChatHud().clear(true);
                        if (messages != null) {
                            messages.forEach(((ChatHudMethods) client.inGameHud.getChatHud())::cubesideMod$addStoredChatMessage);
                        }
                        if (commands != null) {
                            commands.forEach(((ChatHudMethods) client.inGameHud.getChatHud())::cubesideMod$addStoredCommand);
                        }
                    } finally {
                        CubesideClientFabric.setLoadingMessages(false);

                        CubesideClientFabric.messageQueue.forEach(text -> client.inGameHud.getChatHud().addMessage(text));
                        CubesideClientFabric.messageQueue.clear();

                    }

                }

                if (Configs.HitBox.KeepEntityHitBox.getBooleanValue()) {
                    // FIXME 1.21.9 client.getEntityRenderDispatcher().setRenderHitboxes(Configs.HitBox.ShowHitBox.getBooleanValue());
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
                    if (mc.player.isGliding()) {
                        if (!flyingLastTick) {
                            flyingLastTick = true;
                            lastmode = mc.options.getPerspective();
                            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                        }
                    } else {
                        if (flyingLastTick) {
                            flyingLastTick = false;
                            mc.options.setPerspective(lastmode);
                        }
                    }
                }

                if (Configs.Generic.ElytraAlarm.getBooleanValue()) {
                    if (sound == null) {
                        Identifier location = Identifier.of(CubesideClientFabric.MODID, "alarm");
                        sound = SoundEvent.of(location);
                    }
                    if (mc.player.isGliding() && mc.player.getY() <= mc.world.getBottomY()) {
                        if (soundThread == null) {
                            soundThread = SoundThread.of(1944, sound, mc.player);
                            soundThread.start();
                        }
                    } else if (soundThread != null && soundThread.isRunning()) {
                        soundThread.stopThread();
                        soundThread = null;
                    }
                }

                while (KeyBinds.AUTO_CHAT.wasPressed()) {
                    if (PermissionHandler.hasPermission("cubeside.autochat")) {
                        if (Configs.PermissionSettings.AutoChat.getBooleanValue()) {
                            Configs.PermissionSettings.AutoChat.setBooleanValue(false);
                            mc.player.sendMessage(Text.of("§cAuto Chat deaktiviert"), true);
                        } else {
                            Configs.PermissionSettings.AutoChat.setBooleanValue(true);
                            mc.player.sendMessage(Text.of("§aAuto Chat aktiviert"), true);
                        }
                        Configs.saveToFile();
                    } else {
                        ChatUtils.sendErrorMessage("AutoChat kannst du erst ab Staff benutzen!");
                    }
                }

                while (KeyBinds.TOGGLE_SHOW_ENTITIES_IN_SPECTATOR_MODE.wasPressed()) {
                    if (Configs.Generic.ShowInvisibleEntitiesInSpectator.getBooleanValue()) {
                        Configs.Generic.ShowInvisibleEntitiesInSpectator.setBooleanValue(false);
                        mc.player.sendMessage(Text.of("§aUnsichtbare Entities werden jetzt im Spectator nicht mehr angezeigt!"), true);
                    } else {
                        Configs.Generic.ShowInvisibleEntitiesInSpectator.setBooleanValue(true);
                        mc.player.sendMessage(Text.of("§aUnsichtbare Entities werden jetzt im Spectator wieder angezeigt!"), true);
                    }
                    Configs.saveToFile();
                }

                while (KeyBinds.SET_MINING_ASSISTANT_START_POINT.wasPressed()) {
                    MiningAssistent.setStartPos(MinecraftClient.getInstance().player.getBlockPos());
                }

                while (KeyBinds.TOGGLE_MINING_ASSISTANT.wasPressed()) {
                    Configs.MiningAssistent.MiningAssistentEnabled.setBooleanValue(!Configs.MiningAssistent.MiningAssistentEnabled.getBooleanValue());
                    Configs.saveToFile();
                    mc.player.sendMessage(Text.of("MiningAssistent set to: " + (Configs.MiningAssistent.MiningAssistentEnabled.getBooleanValue() ? "§atrue" : "§cfalse")), true);
                }

                while (KeyBinds.WOOD_STRIPING.wasPressed()) {
                    Configs.Generic.WoodStriping.setBooleanValue(!Configs.Generic.WoodStriping.getBooleanValue());
                    Configs.saveToFile();
                    mc.player.sendMessage(Text.of("WoodStriping set to: " + (Configs.Generic.WoodStriping.getBooleanValue() ? "§atrue" : "§cfalse")), true);
                }

                while (KeyBinds.CREATE_GRASS_PATH.wasPressed()) {
                    Configs.Generic.CreateGrassPath.setBooleanValue(!Configs.Generic.CreateGrassPath.getBooleanValue());
                    Configs.saveToFile();
                    mc.player.sendMessage(Text.of("CreateGrassPath set to: " + (Configs.Generic.CreateGrassPath.getBooleanValue() ? "§atrue" : "§cfalse")), true);
                }

                while (KeyBinds.EDIT_SIGN.wasPressed()) {
                    Configs.Generic.SignEdit.setBooleanValue(!Configs.Generic.SignEdit.getBooleanValue());
                    Configs.saveToFile();
                    mc.player.sendMessage(Text.of("SignEdit set to: " + (Configs.Generic.SignEdit.getBooleanValue() ? "§atrue" : "§cfalse")), true);
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

        // FIXME 1.21.9 WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> MiningAssistent.render(context.matrixStack()));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack itemInHand = player.getStackInHand(hand);
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (!Configs.Generic.WoodStriping.getBooleanValue() && itemInHand.getItem() instanceof AxeItem && AxeItem.STRIPPED_BLOCKS.containsKey(blockState.getBlock())) {
                return ActionResult.FAIL;
            }

            if (!Configs.Generic.CreateGrassPath.getBooleanValue() && itemInHand.getItem() instanceof ShovelItem && ShovelItem.PATH_STATES.containsKey(blockState.getBlock())) {
                return ActionResult.FAIL;
            }

            if (!Configs.Generic.SignEdit.getBooleanValue() && blockState.getBlock() instanceof AbstractSignBlock) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

    public String getMapName(MinecraftClient client) {
        Optional<IntegratedServer> integratedServer = Optional.ofNullable(client.getServer());

        if (integratedServer.isEmpty()) {
            String error = "Tried fetching map name on a non-integrated server!";
            CubesideClientFabric.LOGGER.fatal(error);
            throw new IllegalStateException(error);
        }

        return integratedServer.get().getSavePath(WorldSavePath.ROOT).normalize().toFile().getName();
    }

    public String getServerName(ClientPlayNetworkHandler networkHandler) {
        String serverName = null;

        try {
            ServerInfo serverInfo = networkHandler.getServerInfo();
            serverInfo = serverInfo != null ? serverInfo : MinecraftClient.getInstance().getCurrentServerEntry();
            boolean isRealm = serverInfo != null && serverInfo.isRealm();
            if (serverInfo != null) {
                boolean isOnLAN = serverInfo.isLocal();
                if (isOnLAN) {
                    CubesideClientFabric.LOGGER.warn("LAN server detected!");
                    serverName = serverInfo.name;
                } else if (isRealm) {
                    CubesideClientFabric.LOGGER.info("Server is a Realm.");
                    RealmsClient realmsClient = RealmsClient.createRealmsClient(MinecraftClient.getInstance());
                    RealmsServerList realmsServerList = realmsClient.listWorlds();
                    for (RealmsServer realmsServer : realmsServerList.servers) {
                        if (realmsServer.name.equals(serverInfo.name)) {
                            serverName = "Realm_" + realmsServer.id + "." + realmsServer.ownerUUID;
                            break;
                        }
                    }
                } else {
                    serverName = serverInfo.address;
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
