package de.fanta.cubeside.util;

import de.fanta.cubeside.CubesideClientFabric;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtils {

    private final Thread timer;
    private static final HashMap<String, Object> messageQueue = new HashMap<>();

    public ChatUtils() {
        // prevent instances
        this.timer = new Thread(() -> {
            while (true) {
                synchronized (messageQueue) {
                    messageQueue.values().forEach(ChatUtils::sendMessage);
                    messageQueue.clear();
                }
                try {
                    Thread.sleep(3500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        this.timer.start();
    }

    public static void sendMessage(Object message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null ) {
            mc.player.displayClientMessage(Component.nullToEmpty(CubesideClientFabric.PREFIX + message), false);
        }
    }

    public static void sendNormalMessage(Object message) {
        sendMessage("§a" + message);
    }

    public static void sendWarningMessage(Object message) {
        sendMessage("§6" + message);
    }

    public static void sendErrorMessage(Object message) {
        sendMessage("§c" + message);
    }

    public static void queueMessage(Object message, String key) {
        synchronized (messageQueue) {
            messageQueue.putIfAbsent(key, "§a" + message);
        }
    }
}
