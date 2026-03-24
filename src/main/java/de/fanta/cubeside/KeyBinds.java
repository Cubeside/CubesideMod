package de.fanta.cubeside;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
    public static KeyMapping.Category CATEGORY_CUBESIDE;
    public static KeyMapping NARRATOR_KEYBINDING;
    public static KeyMapping F3_KEYBINDING;
    public static KeyMapping AUTO_CHAT;
    public static KeyMapping TOGGLE_SHOW_ENTITIES_IN_SPECTATOR_MODE;
    public static KeyMapping TOGGLE_MINING_ASSISTANT;
    public static KeyMapping SET_MINING_ASSISTANT_START_POINT;
    public static KeyMapping WOOD_STRIPING;
    public static KeyMapping CREATE_GRASS_PATH;
    public static KeyMapping EDIT_SIGN;

    public void initKeys() {
        CATEGORY_CUBESIDE = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("cubeside", "cubeside"));

        NARRATOR_KEYBINDING = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "options.narrator",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KeyMapping.Category.MISC));

        AUTO_CHAT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cubeside.autochat",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        TOGGLE_SHOW_ENTITIES_IN_SPECTATOR_MODE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cubeside.showentitiesinspectator",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                CATEGORY_CUBESIDE));

        TOGGLE_MINING_ASSISTANT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cubeside.miningassistant",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        SET_MINING_ASSISTANT_START_POINT = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cubeside.setminingassistantstartpoint",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        WOOD_STRIPING = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cubeside.woodstriping",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        CREATE_GRASS_PATH = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cubeside.creategrasspath",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        EDIT_SIGN = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cubeside.editsign",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));
    }

}
