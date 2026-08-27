package de.fanta.cubeside.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.config.option.ArgbColor;
import de.fanta.cubeside.config.option.ConfigBoolean;
import de.fanta.cubeside.config.option.ConfigColor;
import de.fanta.cubeside.config.option.ConfigColorList;
import de.fanta.cubeside.config.option.ConfigDouble;
import de.fanta.cubeside.config.option.ConfigInteger;
import de.fanta.cubeside.config.option.ConfigString;
import de.fanta.cubeside.config.option.ConfigStringList;
import de.fanta.cubeside.config.option.ConfigValue;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class Configs {
    public static final String CONFIG_FILE_NAME = "cubeside.json";
    public static final int CONFIG_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Configs() {
    }

    public static final class Generic {
        public static final ConfigBoolean ThirdPersonElytra = bool("ThirdPersonElytra", false, "options.cubeside.thirdpersonelytra");
        public static final ConfigBoolean ElytraAlarm = bool("ElytraAlarm", false, "options.cubeside.elytraalarm");
        public static final ConfigBoolean ShowInvisibleArmorstands = bool("ShowInvisibleArmorstands", true, "options.cubeside.showinvisiblearmorstands");
        public static final ConfigBoolean ShowInvisibleEntitiesInSpectator = bool("ShowInvisibleEntitiesInSpectator", true, "options.cubeside.showinvisibleentitiesinspectator");
        public static final ConfigBoolean AFKPling = bool("AFKPling", false, "options.cubeside.afkpling");
        public static final ConfigBoolean ClickableTpaMessage = bool("ClickableTpaMessage", true, "options.cubeside.clickabletpamessage");
        public static final ConfigBoolean TpaSound = bool("TpaSound", true, "options.cubeside.tpasound");
        public static final ConfigBoolean GamemodeSwitcher = bool("GamemodeSwitcher", true, "options.cubeside.gamemodeswitcher");
        public static final ConfigBoolean ActionBarShadow = bool("ActionBarShadow", true, "options.cubeside.actionbarshadow");
        public static final ConfigBoolean WoodStriping = bool("WoodStriping", true, "options.cubeside.woodstriping");
        public static final ConfigBoolean CreateGrassPath = bool("CreateGrassPath", true, "options.cubeside.creategrasspath");
        public static final ConfigBoolean SignEdit = bool("SignEdit", true, "options.cubeside.signedit");
        public static final ConfigBoolean ShowAdditionalRepairCosts = bool("ShowAdditionalRepairCosts", false, "options.cubeside.showadditionalrepaircosts");
        public static final ConfigString FastJoinButtonText = string("FastJoinButtonText", "Join Cubeside", "options.cubeside.fastjoinbuttontext");
        public static final ConfigString FastJoinButtonIP = string("FastJoinButtonIP", "cubeside.de", "options.cubeside.fastjoinbuttonip");
        public static final ConfigInteger FastJoinButtonPort = integer("FastJoinButtonPort", 25565, "options.cubeside.fastjoinbuttonport");

        public static final List<ConfigValue<?>> OPTIONS = List.of(
                ThirdPersonElytra, ElytraAlarm, ShowInvisibleArmorstands, ShowInvisibleEntitiesInSpectator,
                AFKPling, ClickableTpaMessage, TpaSound, GamemodeSwitcher, ActionBarShadow,
                WoodStriping, CreateGrassPath, SignEdit, ShowAdditionalRepairCosts,
                FastJoinButtonText, FastJoinButtonIP, FastJoinButtonPort);
    }

    public static final class Chat {
        public static final ConfigBoolean ChatTimeStamps = bool("ChatTimeStamps", false, "options.cubeside.chattimestamps");
        public static final ConfigColor TimeStampColor = color("TimeStampColor", "#ffffff", "options.cubeside.timestampcolor");
        public static final ConfigBoolean SaveMessagesToDatabase = bool("SaveMessagesToDatabase", false, "options.cubeside.savemessagestodatabase");
        public static final ConfigInteger DaysTheMessagesAreStored = integer("DaysTheMessagesAreStored", 10, 1, 30, true, "options.cubeside.daysthemessagesarestored");
        public static final ConfigInteger ChatMessageLimit = integer("ChatMessageLimit", 100, 1, 100000, true, "options.cubeside.chatlimit");
        public static final ConfigBoolean DisplayChatInfo = bool("DisplayChatInfo", true, "options.cubeside.displaychatinfo");
        public static final ConfigBoolean CountDuplicateMessages = bool("CountDuplicateMessages", false, "options.cubeside.countduplicatemessages");
        public static final ConfigString CountDuplicateMessagesFormat = string("CountDuplicateMessagesFormat", " (%sx)", "options.cubeside.countduplicatemessagesformat");
        public static final ConfigColor CountDuplicateMessagesColor = color("CountDuplicateMessagesColor", "#ffffff", "options.cubeside.countduplicatemessagescolor");
        public static final List<ConfigValue<?>> OPTIONS = List.of(
                ChatTimeStamps, TimeStampColor, SaveMessagesToDatabase, DaysTheMessagesAreStored,
                ChatMessageLimit, DisplayChatInfo, CountDuplicateMessages, CountDuplicateMessagesFormat,
                CountDuplicateMessagesColor);
    }

    public static final class ChunkLoading {
        public static final ConfigBoolean FullVerticalView = bool("FullVerticalView", true, "options.cubeside.fullverticalview");
        public static final ConfigBoolean UnloadChunks = bool("UnloadChunks", true, "options.cubeside.unloadchunks");
        public static final ConfigInteger FakeViewDistance = integer("FakeViewDistance", 32, 1, 64, true, "options.cubeside.fakeviewdistance");
        public static final List<ConfigValue<?>> OPTIONS = List.of(FullVerticalView, UnloadChunks, FakeViewDistance);
    }

    public static final class Fun {
        public static final ConfigBoolean DisableChristmasChest = bool("DisableChristmasChest", false, "options.cubeside.removechristmaschest");
        public static final List<ConfigValue<?>> OPTIONS = List.of(DisableChristmasChest);
    }

    public static final class HitBox {
        public static final ConfigBoolean ModifiedEntityHitBox = bool("ModifiedEntityHitBox", false, "options.cubeside.modifiedentityhitbox");
        public static final ConfigBoolean RainbowEntityHitBox = bool("RainbowEntityHitBox", false, "options.cubeside.rainbowentityhitbox");
        public static final ConfigColorList RainbowEntityHitBoxColorList = colorList("RainbowEntityHitBoxColorList", rainbowDefaults(), "options.cubeside.rainbowentityhitboxcolorlist");
        public static final ConfigDouble RainbowEntityHitBoxSpeed = decimal("RainbowEntityHitBoxSpeed", 0.1, 0.0, 1.0, true, "options.cubeside.rainbowentityhitboxspeed");
        public static final ConfigDouble EntityHitBoxVisibility = decimal("EntityHitBoxVisibility", 1.0, 0.0, 1.0, true, "options.cubeside.entityhitboxvisibility");
        public static final ConfigColor EntityHitBoxColor = color("EntityHitBoxColor", "#ffffff", "options.cubeside.entityhitboxcolor");
        public static final ConfigBoolean EntityHitBoxDirection = bool("EntityHitBoxDirection", true, "options.cubeside.entityhitboxdirection");
        public static final ConfigBoolean ModifiedBlockHitBox = bool("ModifiedBlockHitBox", false, "options.cubeside.modifiedblockhitbox");
        public static final ConfigBoolean RainbowBlockHitBox = bool("RainbowBlockHitBox", false, "options.cubeside.rainbowblockhitbox");
        public static final ConfigColorList RainbowBlockHitBoxColorList = colorList("RainbowBlockHitBoxColorList", rainbowDefaults(), "options.cubeside.rainbowblockhitboxcolorlist");
        public static final ConfigDouble RainbowBlockHitBoxSpeed = decimal("RainbowBlockHitBoxSpeed", 0.1, 0.0, 1.0, true, "options.cubeside.rainbowblockhitboxspeed");
        public static final ConfigDouble BlockHitBoxVisibility = decimal("BlockHitBoxVisibility", 0.4, 0.0, 1.0, true, "options.cubeside.blockhitboxvisibility");
        public static final ConfigColor BlockHitBoxColor = color("BlockHitBoxColor", "#000000", "options.cubeside.blockhitboxcolor");
        public static final List<ConfigValue<?>> OPTIONS = List.of(
                ModifiedEntityHitBox, RainbowEntityHitBox, RainbowEntityHitBoxColorList,
                RainbowEntityHitBoxSpeed, EntityHitBoxColor, EntityHitBoxVisibility, EntityHitBoxDirection,
                ModifiedBlockHitBox, RainbowBlockHitBox, RainbowBlockHitBoxColorList,
                RainbowBlockHitBoxSpeed, BlockHitBoxColor, BlockHitBoxVisibility);

        public static final ConfigBoolean ShowHitBox = bool("ShowHitBox", false, "options.cubeside.showhitbox");
        public static final List<ConfigValue<?>> INVISIBLE_OPTIONS = List.of(ShowHitBox);
    }

    public static final class MiningAssistent {
        public static final ConfigBoolean MiningAssistentEnabled = bool("MiningAssistentEnabled", false, "options.cubeside.miningassistentenabled");
        public static final ConfigInteger MiningAssistentDistance = integer("MiningAssistentDistance", 3, 1, 100, true, "options.cubeside.miningassistentdistance");
        public static final ConfigInteger MiningAssistentCircles = integer("MiningAssistentCircles", 16, 1, 50, true, "options.cubeside.miningassistentcircles");
        public static final List<ConfigValue<?>> OPTIONS = List.of(MiningAssistentEnabled, MiningAssistentDistance, MiningAssistentCircles);
        public static final ConfigInteger MiningAssistentStartX = integer("MiningAssistentStartX", 0, "options.cubeside.miningassistentstartx");
        public static final ConfigInteger MiningAssistentStartY = integer("MiningAssistentStartY", 0, "options.cubeside.miningassistentstarty");
        public static final ConfigInteger MiningAssistentStartZ = integer("MiningAssistentStartZ", 0, "options.cubeside.miningassistentstartz");
        public static final List<ConfigValue<?>> INVISIBLE_OPTIONS = List.of(MiningAssistentStartX, MiningAssistentStartY, MiningAssistentStartZ);
    }

    public static final class Fixes {
        public static final ConfigBoolean SimpleSignGlow = bool("SimpleSignGlow", false, "options.cubeside.simplesignglow");
        public static final List<ConfigValue<?>> OPTIONS = List.of(SimpleSignGlow);
    }

    public static final class PermissionSettings {
        public static final ConfigBoolean AutoChat = bool("AutoChat", false, "options.cubeside.autochat");
        public static final ConfigString AutoChatAntwort = string("AutoChatAntwort", "Ich habe grade leider keine Zeit!", "options.cubeside.autochatanswer");
        public static final ConfigStringList AdminList = stringList("AdminList",
                List.of("Eiki", "Brokkonaut", "jonibohni", "_Scorcho", "Starjon", "Becky0810", "Scoptixxx"),
                "options.cubeside.adminlist");
        public static final List<ConfigValue<?>> OPTIONS = List.of(AutoChat, AutoChatAntwort, AdminList);
    }

    public record Category(String jsonName, List<ConfigValue<?>> visible, List<ConfigValue<?>> hidden) {
        public List<ConfigValue<?>> all() {
            if (hidden.isEmpty()) {
                return visible;
            }
            return java.util.stream.Stream.concat(visible.stream(), hidden.stream()).toList();
        }
    }

    private static final List<Category> CATEGORIES = List.of(
            new Category("Generic", Generic.OPTIONS, List.of()),
            new Category("Chat", Chat.OPTIONS, List.of()),
            new Category("ChunkLoading", ChunkLoading.OPTIONS, List.of()),
            new Category("Fun", Fun.OPTIONS, List.of()),
            new Category("Hitbox", HitBox.OPTIONS, HitBox.INVISIBLE_OPTIONS),
            new Category("MiningAssistent", MiningAssistent.OPTIONS, MiningAssistent.INVISIBLE_OPTIONS),
            new Category("Fixes", Fixes.OPTIONS, List.of()),
            new Category("PermissionSettings", PermissionSettings.OPTIONS, List.of()));

    public static List<Category> categories() {
        return CATEGORIES;
    }

    public static void loadFromFile() {
        Path directory = CubesideClientFabric.getConfigDirectory().toPath();
        Path legacyFile = directory.getParent().resolve(CONFIG_FILE_NAME);
        loadFromFile(directory.resolve(CONFIG_FILE_NAME), legacyFile);
    }

    public static void loadFromFile(Path configFile, Path legacyFile) {
        resetToDefaults();
        try {
            Files.createDirectories(configFile.getParent());
            if (legacyFile != null && Files.isRegularFile(legacyFile) && Files.notExists(configFile)) {
                Files.move(legacyFile, configFile);
                CubesideClientFabric.LOGGER.info("[CubesideMod] Migrated legacy config to {}", configFile);
            }
            if (Files.notExists(configFile)) {
                saveToFile(configFile);
                return;
            }
            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (!element.isJsonObject()) {
                    CubesideClientFabric.LOGGER.warn("[CubesideMod] Ignoring config with non-object root: {}", configFile);
                    return;
                }
                readRoot(element.getAsJsonObject());
            }
        } catch (Exception exception) {
            CubesideClientFabric.LOGGER.error("[CubesideMod] Failed to load config {}", configFile, exception);
        }
    }

    static void readRoot(JsonObject root) {
        for (Category category : CATEGORIES) {
            JsonElement categoryElement = root.get(category.jsonName());
            if (categoryElement == null || !categoryElement.isJsonObject()) {
                continue;
            }
            JsonObject object = categoryElement.getAsJsonObject();
            for (ConfigValue<?> option : category.all()) {
                JsonElement value = object.get(option.getName());
                if (value == null) {
                    continue;
                }
                try {
                    option.setValueFromJsonElement(value);
                } catch (Exception exception) {
                    option.resetToDefault();
                    CubesideClientFabric.LOGGER.warn("[CubesideMod] Invalid value for {}.{}; using default", category.jsonName(), option.getName(), exception);
                }
            }
        }
    }

    public static void saveToFile() {
        saveToFile(CubesideClientFabric.getConfigDirectory().toPath().resolve(CONFIG_FILE_NAME));
    }

    public static void saveToFile(Path configFile) {
        Path temporaryFile = null;
        try {
            Files.createDirectories(configFile.getParent());
            temporaryFile = Files.createTempFile(configFile.getParent(), CONFIG_FILE_NAME, ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                GSON.toJson(createRoot(), writer);
            }
            try {
                Files.move(temporaryFile, configFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
            temporaryFile = null;
            CubesideClientFabric.LOGGER.info("[CubesideMod] Config saved");
        } catch (IOException exception) {
            CubesideClientFabric.LOGGER.error("[CubesideMod] Failed to save config {}", configFile, exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    static JsonObject createRoot() {
        JsonObject root = new JsonObject();
        for (Category category : CATEGORIES) {
            JsonObject object = new JsonObject();
            for (ConfigValue<?> option : category.all()) {
                object.add(option.getName(), option.getAsJsonElement());
            }
            root.add(category.jsonName(), object);
        }
        root.add("config_version", new JsonPrimitive(CONFIG_VERSION));
        return root;
    }

    public static void resetToDefaults() {
        CATEGORIES.stream().flatMap(category -> category.all().stream()).forEach(ConfigValue::resetToDefault);
    }

    private static ConfigBoolean bool(String name, boolean defaultValue, String tooltipKey) {
        return new ConfigBoolean(name, defaultValue, tooltipKey);
    }

    private static ConfigInteger integer(String name, int defaultValue, String tooltipKey) {
        return new ConfigInteger(name, defaultValue, tooltipKey);
    }

    private static ConfigInteger integer(String name, int defaultValue, int min, int max, boolean slider, String tooltipKey) {
        return new ConfigInteger(name, defaultValue, min, max, slider, tooltipKey);
    }

    private static ConfigDouble decimal(String name, double defaultValue, double min, double max, boolean slider, String tooltipKey) {
        return new ConfigDouble(name, defaultValue, min, max, slider, tooltipKey);
    }

    private static ConfigString string(String name, String defaultValue, String tooltipKey) {
        return new ConfigString(name, defaultValue, tooltipKey);
    }

    private static ConfigColor color(String name, String defaultValue, String tooltipKey) {
        return new ConfigColor(name, defaultValue, tooltipKey);
    }

    private static ConfigColorList colorList(String name, List<ArgbColor> defaultValue, String tooltipKey) {
        return new ConfigColorList(name, defaultValue, tooltipKey);
    }

    private static ConfigStringList stringList(String name, List<String> defaultValue, String tooltipKey) {
        return new ConfigStringList(name, defaultValue, tooltipKey);
    }

    private static List<ArgbColor> rainbowDefaults() {
        return List.of(16711684, 16754176, 16769280, 65305, 35071, 13959423).stream()
                .map(ArgbColor::fromColor)
                .toList();
    }
}
