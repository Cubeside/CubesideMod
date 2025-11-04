package de.fanta.cubeside;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
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
        CATEGORY_CUBESIDE = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("cubeside", "cubeside"));

        NARRATOR_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "options.narrator",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KeyMapping.Category.MISC));

        F3_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "options.debugscreen",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F3,
                KeyMapping.Category.MISC));

        AUTO_CHAT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cubeside.autochat",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        TOGGLE_SHOW_ENTITIES_IN_SPECTATOR_MODE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cubeside.showentitiesinspectator",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                CATEGORY_CUBESIDE));

        TOGGLE_MINING_ASSISTANT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cubeside.miningassistant",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        SET_MINING_ASSISTANT_START_POINT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cubeside.setminingassistantstartpoint",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        WOOD_STRIPING = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cubeside.woodstriping",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        CREATE_GRASS_PATH = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cubeside.creategrasspath",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        EDIT_SIGN = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cubeside.editsign",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));
    }

}
