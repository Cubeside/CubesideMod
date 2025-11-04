package de.fanta.cubeside;

import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.data.ChatDatabase;
import de.fanta.cubeside.event.CubesideModChannelHandler;
import de.fanta.cubeside.util.ChatInfo;
import de.iani.cubesideutils.fabric.scheduler.Scheduler;
import fi.dy.masa.malilib.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class CubesideClientFabric implements ClientModInitializer {

    public static final String MODID = "cubeside";
    public static final String PREFIX = "§9[§aCubeside§9] ";
    public static final Logger LOGGER = LogManager.getLogger("CubesideMod");
    // GAMA
    public static double minGamma = -1.5;
    public static double maxGamma = 12.0;

    public static double brightnessSliderInterval = 0.05;

    public static Commands commands;

    private static boolean loadingMessages;
    public static boolean databaseinuse = false;
    public static List<Component> messageQueue = new ArrayList<>();

    private static long time;

    private static ChatInfo chatInfo;

    private static File configDirectory;
    private static ChatDatabase chatDatabase;

    @Override
    public void onInitializeClient() {
        configDirectory = new File(FileUtils.getConfigDirectoryAsPath() + "/CubesideMod");
        if (!configDirectory.isDirectory()) {
            configDirectory.mkdirs();
        }
        File chatStorage = new File(configDirectory, "/chatStorage");
        if (!chatStorage.isDirectory()) {
            chatStorage.mkdir();
        }
        Configs.loadFromFile();

        KeyBinds keyBinds = new KeyBinds();
        keyBinds.initKeys();

        Events events = new Events();
        events.init();

        LogicalZoom logicalZoom = new LogicalZoom();
        logicalZoom.initLogicalZoom();
        new CubesideModChannelHandler();

        LOGGER.info(MODID + "Mod Loaded");
        commands = new Commands();

        time = 0;
        this.restartTask(1);
    }

    public static void setLoadingMessages(boolean setloadingMessages) {
        loadingMessages = setloadingMessages;
    }

    public static boolean isLoadingMessages() {
        return loadingMessages;
    }

    public void restartTask(int l) {
        Scheduler.scheduleSyncRepeatingTask(() -> time++, 0, l);
    }

    public static long getTime() {
        return time;
    }

    public static ChatInfo getChatInfo() {
        return chatInfo;
    }

    public static void setChatInfo(ChatInfo chatInfo) {
        CubesideClientFabric.chatInfo = chatInfo;
    }

    public static File getConfigDirectory() {
        return configDirectory;
    }

    public static ChatDatabase getChatDatabase() {
        return chatDatabase;
    }

    public static void setChatDatabase(ChatDatabase chatDatabase) {
        CubesideClientFabric.chatDatabase = chatDatabase;
    }
}
