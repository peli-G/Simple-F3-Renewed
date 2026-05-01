package com.notcasey.simple_f3;

import com.notcasey.simple_f3.config.SimpleF3Config;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleF3 implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("simple_f3");

    public static boolean simpleDebugEnabled = false;
    public static int rainbowHue = 0;

    // F3 key state tracking for press detection (no mixin needed)
    private static boolean f3WasDown = false;

    // Lazily created HUD instance
    private static SimpleDebugHud simpleDebugHud;

    private static SimpleDebugHud getOrCreateHud(MinecraftClient client) {
        if (simpleDebugHud == null) {
            simpleDebugHud = new SimpleDebugHud(client);
        }
        return simpleDebugHud;
    }

    @Override
    public void onInitializeClient() {
        MidnightConfig.init("simple_f3", SimpleF3Config.class);

        // Rainbow hue tick
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!simpleDebugEnabled) return;
            rainbowHue += 1;
            if (rainbowHue > 255) rainbowHue = 0;
        });

        // F3 key detection — replaces KeyboardMixin, no refmap needed
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (SimpleF3Config.IsDisabled()) {
                simpleDebugEnabled = false;
                return;
            }

            boolean f3Down = InputUtil.isKeyPressed(
                    client.getWindow(), GLFW.GLFW_KEY_F3);

            // Trigger on key release, matching original behaviour
            if (!f3Down && f3WasDown) {
                simpleDebugEnabled = !simpleDebugEnabled;
            }
            f3WasDown = f3Down;
        });

        // HUD rendering — replaces InGameHudMixin, no refmap needed
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && simpleDebugEnabled) {
                getOrCreateHud(client).render(context);
            }
        });

        // Reset simpleDebugEnabled on title screen — replaces TitleScreenMixin, no refmap needed
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                simpleDebugEnabled = SimpleF3Config.auto_enable;
                f3WasDown = false;
            }
        });
    }
}
