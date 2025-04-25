package com.taghighlight;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class TagHighlightKeybinds {
    // Keybinding for toggling outline
    private static KeyBinding toggleOutlineKey;

    public static void register() {
        // Register the keybinding (default key: O)
        toggleOutlineKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tag-highlight.toggle_outline",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.tag-highlight.keybinds"
        ));

        // Register the tick event for handling key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleOutlineKey.wasPressed()) {
                TagHighlightClient.CONFIG.outlineEnabled = !TagHighlightClient.CONFIG.outlineEnabled;
                TagHighlightClient.saveConfig();
            }
        });
    }
}