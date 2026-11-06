package com.mocapui;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier; // Agar error aaye to ResourceLocation try karo
import org.lwjgl.glfw.GLFW;

public class MocapUiClient implements ClientModInitializer {
    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mocapui", "main"));
        openKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.mocapui.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                if (mc.screen == null && mc.player != null) mc.setScreen(new MocapScreen());
            }
        });

        // Mocap ka command output UI ke andar dikhane ke liye
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) Cmd.addLog(message.getString());
        });
    }
}
