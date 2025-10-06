package de.fanta.cubeside;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
    public static KeyBinding.Category CATEGORY_CUBESIDE;
    public static KeyBinding NARRATOR_KEYBINDING;
    public static KeyBinding F3_KEYBINDING;
    public static KeyBinding AUTO_CHAT;
    public static KeyBinding TOGGLE_SHOW_ENTITIES_IN_SPECTATOR_MODE;
    public static KeyBinding TOGGLE_MINING_ASSISTANT;
    public static KeyBinding SET_MINING_ASSISTANT_START_POINT;
    public static KeyBinding WOOD_STRIPING;
    public static KeyBinding CREATE_GRASS_PATH;
    public static KeyBinding EDIT_SIGN;

    public void initKeys() {
        CATEGORY_CUBESIDE = KeyBinding.Category.create(Identifier.of("cubeside", "cubeside"));

        NARRATOR_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "options.narrator",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KeyBinding.Category.MISC));

        F3_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "options.debugscreen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F3,
                KeyBinding.Category.MISC));

        AUTO_CHAT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cubeside.autochat",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        TOGGLE_SHOW_ENTITIES_IN_SPECTATOR_MODE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cubeside.showentitiesinspectator",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                CATEGORY_CUBESIDE));

        TOGGLE_MINING_ASSISTANT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cubeside.miningassistant",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        SET_MINING_ASSISTANT_START_POINT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cubeside.setminingassistantstartpoint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        WOOD_STRIPING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cubeside.woodstriping",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        CREATE_GRASS_PATH = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cubeside.creategrasspath",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));

        EDIT_SIGN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cubeside.editsign",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_CUBESIDE));
    }

}
