package de.fanta.cubeside.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.fanta.cubeside.CubesideClientFabric;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigColorList;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import net.minecraft.network.chat.Component;

public class Configs implements IConfigHandler {

    private static final String CONFIG_FILE_NAME = "cubeside.json";
    private static final int CONFIG_VERSION = 1;

    public static class Generic {
        public static final ConfigBoolean ThirdPersonElytra = new ConfigBoolean("ThirdPersonElytra", false, Component.translatable("options.cubeside.thirdpersonelytra").getString());
        public static final ConfigBoolean ElytraAlarm = new ConfigBoolean("ElytraAlarm", false, Component.translatable("options.cubeside.elytraalarm").getString());
        public static final ConfigBoolean ShowInvisibleArmorstands = new ConfigBoolean("ShowInvisibleArmorstands", true, Component.translatable("options.cubeside.showinvisiblearmorstands").getString());
        public static final ConfigBoolean ShowInvisibleEntitiesInSpectator = new ConfigBoolean("ShowInvisibleEntitiesInSpectator", true, Component.translatable("options.cubeside.showinvisibleentitiesinspectator").getString());
        public static final ConfigBoolean AFKPling = new ConfigBoolean("AFKPling", false, Component.translatable("options.cubeside.afkpling").getString());
        public static final ConfigBoolean ClickableTpaMessage = new ConfigBoolean("ClickableTpaMessage", true, Component.translatable("options.cubeside.clickabletpamessage").getString());
        public static final ConfigBoolean TpaSound = new ConfigBoolean("TpaSound", true, Component.translatable("options.cubeside.tpasound").getString());
        public static final ConfigBoolean GamemodeSwitcher = new ConfigBoolean("GamemodeSwitcher", true, Component.translatable("options.cubeside.gamemodeswitcher").getString());
        public static final ConfigBoolean ActionBarShadow = new ConfigBoolean("ActionBarShadow", true, Component.translatable("options.cubeside.actionbarshadow").getString());
        public static final ConfigBoolean WoodStriping = new ConfigBoolean("WoodStriping", true, Component.translatable("options.cubeside.woodstriping").getString());
        public static final ConfigBoolean CreateGrassPath = new ConfigBoolean("CreateGrassPath", true, Component.translatable("options.cubeside.creategrasspath").getString());
        public static final ConfigBoolean SignEdit = new ConfigBoolean("SingEdit", true, Component.translatable("options.cubeside.singedit").getString());
        public static final ConfigBoolean ShowAdditionalRepairCosts = new ConfigBoolean("ShowAdditionalRepairCosts", false, Component.translatable("options.cubeside.showadditionalrepaircosts").getString());
        public static final ConfigString FastJoinButtonText = new ConfigString("FastJoinButtonText", "Join Cubeside", Component.translatable("options.cubeside.fastjoinbuttontext").getString());
        public static final ConfigString FastJoinButtonIP = new ConfigString("FastJoinButtonIP", "cubeside.de", Component.translatable("options.cubeside.fastjoinbuttonip").getString());
        public static final ConfigInteger FastJoinButtonPort = new ConfigInteger("FastJoinButtonPort", 25565, Component.translatable("options.cubeside.fastjoinbuttonport").getString());

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                ThirdPersonElytra,
                ElytraAlarm,
                ShowInvisibleArmorstands,
                ShowInvisibleEntitiesInSpectator,
                AFKPling,
                ClickableTpaMessage,
                TpaSound,
                GamemodeSwitcher,
                ActionBarShadow,
                WoodStriping,
                CreateGrassPath,
                SignEdit,
                ShowAdditionalRepairCosts,
                FastJoinButtonText,
                FastJoinButtonIP,
                FastJoinButtonPort
        );
    }

    public static class Chat {
        public static final ConfigBoolean ChatTimeStamps = new ConfigBoolean("ChatTimeStamps", false, Component.translatable("options.cubeside.chattimestamps").getString());
        public static final ConfigColor TimeStampColor = new ConfigColor("TimeStampColor", "#ffffff", Component.translatable("options.cubeside.timestampcolor").getString());
        public static final ConfigBoolean SaveMessagesToDatabase = new ConfigBoolean("SaveMessagesToDatabase", false, Component.translatable("options.cubeside.savemessagestodatabase").getString());
        public static final ConfigInteger DaysTheMessagesAreStored = new ConfigInteger("DaysTheMessagesAreStored", 10, 1, 30, true, Component.translatable("options.cubeside.daysthemessagesarestored").getString());
        public static final ConfigInteger ChatMessageLimit = new ConfigInteger("ChatMessageLimit", 100, 1, 100000, true, Component.translatable("options.cubeside.chatlimit").getString());
        public static final ConfigBoolean DisplayChatInfo = new ConfigBoolean("DisplayChatInfo", true, Component.translatable("options.cubeside.displaychatinfo").getString());
        public static final ConfigBoolean CountDuplicateMessages = new ConfigBoolean("CountDuplicateMessages", false, Component.translatable("options.cubeside.countduplicatemessages").getString());
        public static final ConfigString CountDuplicateMessagesFormat = new ConfigString("CountDuplicateMessagesFormat", " (%sx)", Component.translatable("options.cubeside.countduplicatemessagesformat").getString());
        public static final ConfigColor CountDuplicateMessagesColor = new ConfigColor("CountDuplicateMessagesColor", "#ffffff", Component.translatable("options.cubeside.countduplicatemessagescolor").getString());

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                ChatTimeStamps,
                TimeStampColor,
                SaveMessagesToDatabase,
                DaysTheMessagesAreStored,
                ChatMessageLimit,
                DisplayChatInfo,
                CountDuplicateMessages,
                CountDuplicateMessagesFormat,
                CountDuplicateMessagesColor
        );
    }

    public static class ChunkLoading {
        public static final ConfigBoolean FullVerticalView = new ConfigBoolean("FullVerticalView", true, Component.translatable("options.cubeside.fullverticalview").getString());
        public static final ConfigBoolean UnloadChunks = new ConfigBoolean("UnloadChunks", true, Component.translatable("options.cubeside.unloadchunks").getString());
        public static final ConfigInteger FakeViewDistance = new ConfigInteger("FakeViewDistance", 32, 1, 64, true, Component.translatable("options.cubeside.fakeviewdistance").getString());

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                FullVerticalView,
                UnloadChunks,
                FakeViewDistance
        );
    }

    public static class Fun {
        public static final ConfigBoolean DisableChristmasChest = new ConfigBoolean("DisableChristmasChest", false, Component.translatable("options.cubeside.removechristmaschest").getString());
        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                DisableChristmasChest
        );
    }

    public static class HitBox {
        public static final ConfigBoolean RainbowEntityHitBox = new ConfigBoolean("RainbowEntityHitBox", false, Component.translatable("options.cubeside.rainbowentityhitbox").getString());
        public static final ConfigColorList RainbowEntityHitBoxColorList = new ConfigColorList("RainbowEntityHitBoxColorList", ImmutableList.of(Color4f.fromColor(16711684), Color4f.fromColor(16754176), Color4f.fromColor(16769280), Color4f.fromColor(65305), Color4f.fromColor(35071), Color4f.fromColor(13959423)), Component.translatable("options.cubeside.rainbowentityhitboxcolorlist").getString());
        public static final ConfigDouble RainbowEntityHitBoxSpeed = new ConfigDouble("RainbowEntityHitBoxSpeed", 0.1, 0.0, 1.0, true, Component.translatable("options.cubeside.rainbowentityhitboxspeed").getString());
        public static final ConfigDouble EntityHitBoxVisibility = new ConfigDouble("EntityHitBoxVisibility", 1, 0.0, 1, true, Component.translatable("options.cubeside.entityhitboxvisibility").getString());
        public static final ConfigColor EntityHitBoxColor = new ConfigColor("EntityHitBoxColor", "#ffffff", Component.translatable("options.cubeside.entityhitboxcolor").getString());
        public static final ConfigBoolean EntityHitBoxDirection = new ConfigBoolean("EntityHitBoxDirection", true, Component.translatable("options.cubeside.entityhitboxdirection").getString());
        public static final ConfigBoolean RainbowBlockHitBox = new ConfigBoolean("RainbowBlockHitBox", false, Component.translatable("options.cubeside.rainbowblockhitbox").getString());
        public static final ConfigColorList RainbowBlockHitBoxColorList = new ConfigColorList("RainbowBlockHitBoxColorList", ImmutableList.of(Color4f.fromColor(16711684), Color4f.fromColor(16754176), Color4f.fromColor(16769280), Color4f.fromColor(65305), Color4f.fromColor(35071), Color4f.fromColor(13959423)), Component.translatable("options.cubeside.rainbowblockhitboxcolorlist").getString());
        public static final ConfigDouble RainbowBlockHitBoxSpeed = new ConfigDouble("RainbowBlockHitBoxSpeed", 0.1, 0.0, 1, true, Component.translatable("options.cubeside.rainbowblockhitboxspeed").getString());
        public static final ConfigDouble BlockHitBoxVisibility = new ConfigDouble("BlockHitBoxVisibility", 0.4, 0.0, 1, true, Component.translatable("options.cubeside.blockhitboxvisibility").getString());
        public static final ConfigColor BlockHitBoxColor = new ConfigColor("BlockHitBoxColor", "#000000", Component.translatable("options.cubeside.blockhitboxcolor").getString());
        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                RainbowEntityHitBox,
                RainbowEntityHitBoxColorList,
                RainbowEntityHitBoxSpeed,
                EntityHitBoxColor,
                EntityHitBoxVisibility,
                EntityHitBoxDirection,
                RainbowBlockHitBox,
                RainbowBlockHitBoxColorList,
                RainbowBlockHitBoxSpeed,
                BlockHitBoxColor,
                BlockHitBoxVisibility
        );

        public static final ConfigBoolean ShowHitBox = new ConfigBoolean("ShowHitBox", false, Component.translatable("options.cubeside.showhitbox").getString());
        public static final ImmutableList<IConfigBase> INVISIBLE_OPTIONS = ImmutableList.of(
                ShowHitBox
        );
    }

    public static class MiningAssistent {
        public static final ConfigBoolean MiningAssistentEnabled = new ConfigBoolean("MiningAssistentEnabled", false, Component.translatable("options.cubeside.miningassistentenabled").getString());
        public static final ConfigInteger MiningAssistentDistance = new ConfigInteger("MiningAssistentDistance", 3, 0, 10, Component.translatable("options.cubeside.miningassistentdistance").getString());
        public static final ConfigInteger MiningAssistentCircles = new ConfigInteger("MiningAssistentCircles", 16, 1, 30, Component.translatable("options.cubeside.miningassistentcircles").getString());
        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                MiningAssistentEnabled,
                MiningAssistentDistance,
                MiningAssistentCircles
        );

        public static final ConfigInteger MiningAssistentStartX = new ConfigInteger("MiningAssistentStartX", 0, Component.translatable("options.cubeside.miningassistentstartx").getString());
        public static final ConfigInteger MiningAssistentStartY = new ConfigInteger("MiningAssistentStartY", 0, Component.translatable("options.cubeside.miningassistentstarty").getString());
        public static final ConfigInteger MiningAssistentStartZ = new ConfigInteger("MiningAssistentStartZ", 0, Component.translatable("options.cubeside.miningassistentstartz").getString());
        public static final ImmutableList<IConfigBase> INVISIBLE_OPTIONS = ImmutableList.of(
                MiningAssistentStartX,
                MiningAssistentStartY,
                MiningAssistentStartZ
        );
    }

    public static class Fixes {
        public static final ConfigBoolean SimpleSignGlow = new ConfigBoolean("SimpleSignGlow", false, Component.translatable("options.cubeside.simplesignglow").getString());
        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                SimpleSignGlow
        );
    }

    public static class PermissionSettings {
        public static final ConfigBoolean AutoChat = new ConfigBoolean("AutoChat", false, "AutoChat Enabled");
        public static final ConfigString AutoChatAntwort = new ConfigString("AutoChatAntwort", "Ich habe grade leider keine Zeit!", "AutoChat Message");
        public static final ConfigStringList AdminList = new ConfigStringList("AdminList", ImmutableList.of("Eiki", "Brokkonaut", "jonibohni", "_Scorcho", "Starjon", "Becky0810", "Scoptixxx"), "Admin Liste");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                AutoChat,
                AutoChatAntwort,
                AdminList
        );
    }

    public static void loadFromFile() {
        File oldConfigFile = new File(fi.dy.masa.malilib.util.FileUtils.getConfigDirectoryAsPath().toFile(), CONFIG_FILE_NAME);
        File configFile = new File(CubesideClientFabric.getConfigDirectory(), CONFIG_FILE_NAME);
        if (oldConfigFile.exists()) {
            try {
                FileUtils.moveFile(oldConfigFile, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!configFile.exists()) {
            saveToFile();
        }

        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
                ConfigUtils.readConfigBase(root, "Chat", Chat.OPTIONS);
                ConfigUtils.readConfigBase(root, "ChunkLoading", ChunkLoading.OPTIONS);
                ConfigUtils.readConfigBase(root, "Fun", Fun.OPTIONS);
                ConfigUtils.readConfigBase(root, "Hitbox", HitBox.OPTIONS);
                ConfigUtils.readConfigBase(root, "Hitbox", HitBox.INVISIBLE_OPTIONS);
                ConfigUtils.readConfigBase(root, "MiningAssistent", MiningAssistent.OPTIONS);
                ConfigUtils.readConfigBase(root, "MiningAssistent", MiningAssistent.INVISIBLE_OPTIONS);
                ConfigUtils.readConfigBase(root, "Fixes", Fixes.OPTIONS);
                ConfigUtils.readConfigBase(root, "PermissionSettings", PermissionSettings.OPTIONS);
            }
        }
    }

    public static void saveToFile() {
        File dir = CubesideClientFabric.getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Chat", Chat.OPTIONS);
            ConfigUtils.writeConfigBase(root, "ChunkLoading", ChunkLoading.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Fun", Fun.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Hitbox", HitBox.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Hitbox", HitBox.INVISIBLE_OPTIONS);
            ConfigUtils.writeConfigBase(root, "MiningAssistent", MiningAssistent.OPTIONS);
            ConfigUtils.writeConfigBase(root, "MiningAssistent", MiningAssistent.INVISIBLE_OPTIONS);
            ConfigUtils.writeConfigBase(root, "Fixes", Fixes.OPTIONS);
            ConfigUtils.writeConfigBase(root, "PermissionSettings", PermissionSettings.OPTIONS);

            root.add("config_version", new JsonPrimitive(CONFIG_VERSION));

            JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));

            CubesideClientFabric.LOGGER.info("[CubesideMod] Config Saved");
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }

}
