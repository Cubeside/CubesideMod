package de.fanta.cubeside.util;

import de.iani.cubesideutils.Pair;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;

public class ItemUtils {
    public static Pair<Integer, Integer> getDamageValuesFormCustomItem(ItemLore loreComponent) {
        int durablility = -1;
        int maxDurablility = -1;
        List<Component> lines = loreComponent.lines();
        if (!lines.isEmpty()) {
            Component mutableText = lines.getLast();
            if (mutableText != null) {
                String fullDurabilityString = mutableText.getString();
                if (fullDurabilityString.startsWith("Haltbarkeit:") || fullDurabilityString.startsWith("Durability")) {
                    String[] splitFull = fullDurabilityString.split(" ", 2);
                    if (splitFull.length == 2) {
                        String durabilityString = Component.literal(splitFull[1]).getString();
                        String[] splitDurability = durabilityString.split("/", 2);
                        if (splitDurability.length == 2) {
                            try {
                                durablility = Integer.parseInt(splitDurability[0]);
                                maxDurablility = Integer.parseInt(splitDurability[1]);
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }
                }
            }
        }
        return new Pair<>(durablility, maxDurablility);
    }
}
