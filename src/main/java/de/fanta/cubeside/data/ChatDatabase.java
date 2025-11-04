package de.fanta.cubeside.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.config.Configs;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.StrictJsonParser;
import org.apache.logging.log4j.Level;

public class ChatDatabase {
    private final String server;
    private final RegistryAccess registry;

    private File dbFile;
    private ArrayList<ChatMessage> chatMessages;
    private ArrayList<ChatMessage> commands;
    private boolean markNextMessageDeleted;

    private DataOutputStream dataOut;

    public ChatDatabase(String server, RegistryAccess registry) {
        long time = System.nanoTime();
        this.server = server;
        this.registry = registry;

        chatMessages = new ArrayList<>();
        commands = new ArrayList<>();

        long minTime = System.currentTimeMillis() - Configs.Chat.DaysTheMessagesAreStored.getIntegerValue() * 24L * 60 * 60 * 1000L;

        dbFile = new File(CubesideClientFabric.getConfigDirectory(), "/chatStorage/" + server.toLowerCase() + ".dat");
        if (dbFile.isFile()) {
            try (DataInputStream dataIn = new DataInputStream(new BufferedInputStream(new FileInputStream(dbFile)))) {
                while (true) {
                    int type = dataIn.readByte();
                    if (type == 0) {
                        if (dataIn.readBoolean()) {
                            if (!chatMessages.isEmpty()) {
                                chatMessages.removeLast();
                            }
                        }
                        String msg = dataIn.readUTF();
                        long msgtime = dataIn.readLong();
                        if (msgtime >= minTime) {
                            chatMessages.addLast(new ChatMessage(msg, msgtime));
                        }
                    } else if (type == 1) {
                        String msg = dataIn.readUTF();
                        long msgtime = dataIn.readLong();
                        if (msgtime >= minTime) {
                            commands.addLast(new ChatMessage(msg, msgtime));
                        }
                    }
                }
            } catch (EOFException ignored) {
            } catch (IOException e) {
                CubesideClientFabric.LOGGER.log(Level.ERROR, "Could not load chat messages", e);
                dbFile = null;
            }
        }
        long delta = System.nanoTime() - time;
        CubesideClientFabric.LOGGER.info("Loaded " + chatMessages.size() + " + " + commands.size() + " chatmessages for " + server + " in " + (delta / 1000) + " micros");

        File oldDbFile = new File(CubesideClientFabric.getConfigDirectory(), "/chatStorage/" + server.toLowerCase() + "_1_" + ".db");
        if (oldDbFile.isFile()) {
            oldDbFile.delete();
        }

        time = System.nanoTime();
        if (dbFile != null) {
            try {
                dataOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dbFile)));
                for (ChatMessage message : chatMessages) {
                    writeChatMessage(message.text(), message.date(), false);
                }
                for (ChatMessage message : commands) {
                    writeCommand(message.text(), message.date(), false);
                }
                dataOut.flush();
            } catch (IOException e) {
                CubesideClientFabric.LOGGER.log(Level.ERROR, "Could not open chat messages file for writing", e);
            }
        }
        delta = System.nanoTime() - time;
        CubesideClientFabric.LOGGER.info("Saved " + chatMessages.size() + " + " + commands.size() + " chatmessages for " + server + " in " + (delta / 1000) + " micros");

    }

    public void addMessageEntry(String message) {
        long now = System.currentTimeMillis();
        chatMessages.addLast(new ChatMessage(message, now));
        writeChatMessage(message, now, true);
    }

    private void writeChatMessage(String message, long now, boolean flush) {
        if (dbFile != null) {
            try {
                dataOut.writeByte(0); // chat message
                dataOut.writeBoolean(markNextMessageDeleted);
                try {
                    dataOut.writeUTF(message);
                } catch (UTFDataFormatException e) {
                    dataOut.writeUTF("");
                }
                dataOut.writeLong(now);
                if (flush) {
                    dataOut.flush();
                }
            } catch (IOException e) {
                CubesideClientFabric.LOGGER.log(Level.ERROR, "Could not serialize chat message", e);
            }
            markNextMessageDeleted = false;
        }
    }

    public void addCommandEntry(String command) {
        long now = System.currentTimeMillis();
        commands.add(new ChatMessage(command, now));
        writeCommand(command, now, true);
    }

    private void writeCommand(String command, long now, boolean flush) {
        if (dbFile != null) {
            try {
                dataOut.writeByte(1); // chat command
                try {
                    dataOut.writeUTF(command);
                } catch (UTFDataFormatException e) {
                    dataOut.writeUTF("");
                }
                dataOut.writeLong(now);
                if (flush) {
                    dataOut.flush();
                }
            } catch (IOException e) {
                CubesideClientFabric.LOGGER.log(Level.ERROR, "Could not serialize command", e);
            }
        }
    }

    public void close() {
        if (dataOut != null) {
            try {
                dataOut.close();
            } catch (IOException e) {
                CubesideClientFabric.LOGGER.log(Level.ERROR, "Could not close chat messages file", e);
            }
        }
    }

    public List<Component> loadMessages() {
        return loadMessages(-1);
    }

    public List<Component> loadMessages(int limit) {
        long time = System.nanoTime();
        List<Component> entries = new ArrayList<>();
        int size = chatMessages.size();
        int num = 0;
        RegistryOps<JsonElement> ops = registry.createSerializationContext(JsonOps.INSTANCE);
        for (ChatMessage entry : chatMessages) {
            if (limit == -1 || num >= size - limit) {
                try {
                    JsonElement jsonElement = StrictJsonParser.parse(entry.text());
                    DataResult<Pair<Component, JsonElement>> result = ComponentSerialization.CODEC.decode(ops, jsonElement);
                    if (result.isSuccess()) {
                        entries.add(result.getOrThrow().getFirst());
                    }
                } catch (JsonParseException ignore) {
                }
            }
            num++;
        }
        long delta = System.nanoTime() - time;
        CubesideClientFabric.LOGGER.info(entries.size() + " messages were loaded in " + (delta / 1000) + " micros");
        return entries;
    }

    public List<String> loadCommands() {
        long time = System.nanoTime();
        List<String> entries = new ArrayList<>();
        for (ChatMessage entry : commands) {
            entries.add(entry.text());
        }
        long delta = System.nanoTime() - time;
        CubesideClientFabric.LOGGER.info(entries.size() + " commands were loaded in " + (delta / 1000) + " micros");
        return entries;
    }

    public void deleteNewestMessage() {
        markNextMessageDeleted = true;
        if (!chatMessages.isEmpty()) {
            chatMessages.removeLast();
        }
    }

    public String getServer() {
        return server;
    }
}
