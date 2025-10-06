package de.fanta.cubeside.command;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.util.ChatUtils;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.fabric.commands.SubCommand;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.GameMode;

public class AFKCheckCommand extends SubCommand {
    private static boolean teleport = false;
    private static final ArrayList<String> playerList = new ArrayList<>();

    @Override
    public boolean onCommand(FabricClientCommandSource sender, String alias, String commandString, ArgsParser args) {
        if (!args.hasNext()) {
            return false;
        }
        String status = args.getNext();
        switch (status) {
            case "start":
                if (teleport) {
                    ChatUtils.sendErrorMessage("AFK Check bereits aktiv.");
                    return true;
                }
                ClientPlayNetworkHandler clientPlayNetworkHandler = sender.getClient().getNetworkHandler();
                List<PlayerListEntry> list = ENTRY_ORDERING.sortedCopy(clientPlayNetworkHandler.getListedPlayerListEntries());
                for (PlayerListEntry playerListEntry : list) {
                    if (playerListEntry != null) {
                        String playername = playerListEntry.getProfile().name();
                        if (!Objects.equals(playername, MinecraftClient.getInstance().getGameProfile().name()) && !Configs.PermissionSettings.AdminList.getStrings().contains(playername)) {
                            playerList.add(playername);
                        }
                    }
                }

                if (playerList.isEmpty()) {
                    ChatUtils.sendErrorMessage("Es ist kein Spieler online");
                    break;
                }

                teleport = true;
                String teleportPlayer = playerList.getFirst();
                sender.getPlayer().networkHandler.sendChatCommand("tt p " + teleportPlayer);
                ChatUtils.sendNormalMessage("Du wurdest zu " + teleportPlayer + " teleportiert.");
                playerList.remove(teleportPlayer);
                break;
            case "next":
                if (teleport) {
                    if (!playerList.isEmpty()) {
                        String portPlayer = playerList.getFirst();
                        sender.getPlayer().networkHandler.sendChatCommand("tt p " + portPlayer);
                        ChatUtils.sendNormalMessage("Du wurdest zu " + portPlayer + " teleportiert.");
                        playerList.remove(portPlayer);
                    } else {
                        ChatUtils.sendNormalMessage("Du hast dich zu allen Spielern Teleportiert.");
                        teleport = false;
                    }
                } else {
                    ChatUtils.sendErrorMessage("AFKCheck wurde nicht gestartet!");
                }
                break;
            case "stop":
                if (teleport) {
                    playerList.clear();
                    teleport = false;
                    ChatUtils.sendNormalMessage("AFK Check gestoppt.");
                } else {
                    ChatUtils.sendErrorMessage("Aktuell ist kein AFK Check gestartet!");
                }
                break;
            default:
                ChatUtils.sendErrorMessage("/afkcheck [start|next|stop]");
                return true;
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return "cubeside.afkcheck";
    }

    @Override
    public Collection<String> onTabComplete(FabricClientCommandSource sender, String alias, ArgsParser args) {
        return List.of("start", "next", "stop");
    }

    @Override
    public String getUsage() {
        return "<start | next | stop>";
    }

    private static final Ordering<PlayerListEntry> ENTRY_ORDERING = Ordering.from((playerListEntry, playerListEntry2) -> {
        Team team = playerListEntry.getScoreboardTeam();
        Team team2 = playerListEntry2.getScoreboardTeam();
        return ComparisonChain.start().compareTrueFirst(playerListEntry.getGameMode() != GameMode.SPECTATOR, playerListEntry2.getGameMode() != GameMode.SPECTATOR).compare(team != null ? team.getName() : "", team2 != null ? team2.getName() : "")
                .compare(playerListEntry.getProfile().name(), playerListEntry2.getProfile().name(), String::compareToIgnoreCase).result();
    });
}
