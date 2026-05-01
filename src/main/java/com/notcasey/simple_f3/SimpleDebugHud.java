package com.notcasey.simple_f3;

import com.google.common.base.Strings;
import com.notcasey.simple_f3.config.SimpleF3Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class SimpleDebugHud {
    // 0xFF000000 alpha must be set — in 1.21.x drawText respects alpha, 0x00E0E0E0 renders invisible
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private final MinecraftClient client;
    private final TextRenderer textRenderer;

    public SimpleDebugHud(MinecraftClient client) {
        this.client = client;
        this.textRenderer = client.textRenderer;
    }

    public void render(DrawContext context) {
        if (SimpleF3Config.IsDisabled()) {
            SimpleF3.simpleDebugEnabled = false;
            return;
        }

        BlockPos blockPos = this.client.getCameraEntity().getBlockPos();

        List<String> list = new ArrayList<>();
        if (SimpleF3Config.show_game_version)
            // MinecraftClient#getGameVersion() returns the version string directly (e.g. "1.21.11")
            list.add("Minecraft ".concat(this.client.getGameVersion()));
        if (SimpleF3Config.show_fps)
            list.add(Integer.toString(this.client.getCurrentFps()).concat(" fps"));

        if (SimpleF3Config.show_game_version || SimpleF3Config.show_fps)
            list.add("");

        if (SimpleF3Config.show_coords)
            list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f",
                    this.client.getCameraEntity().getX(),
                    this.client.getCameraEntity().getY(),
                    this.client.getCameraEntity().getZ()));

        // getTopY now requires (Heightmap.Type, int x, int z) — use world height bounds for range check
        if (SimpleF3Config.show_biome
                && blockPos.getY() >= this.client.world.getBottomY()
                && blockPos.getY() < this.client.world.getTopY(Heightmap.Type.WORLD_SURFACE, blockPos.getX(), blockPos.getZ())) {
            RegistryEntry<Biome> biome = this.client.world.getBiome(blockPos);
            list.add("Biome: " + getBiomeString(biome));
        }

        for (int i = 0; i < list.size(); ++i) {
            String string = list.get(i);
            if (!Strings.isNullOrEmpty(string)) {
                int lineHeight = 9;
                int textWidth = this.textRenderer.getWidth(string);
                int x = 2;
                int y = 2 + lineHeight * i;

                if (SimpleF3Config.right_text)
                    x = this.client.getWindow().getScaledWidth() - 2 - textWidth;

                int textColor = TEXT_COLOR;
                if (SimpleF3Config.rainbow_text)
                    textColor = Color.HSBtoRGB((float) SimpleF3.rainbowHue / 255f, 1f, 1f);

                if (SimpleF3Config.classic_style) {
                    context.drawTextWithShadow(this.textRenderer, string, x, y, textColor);
                } else {
                    context.fill(x - 1, y - 1, x + textWidth + 1, y + lineHeight - 1, -1873784752);
                    context.drawText(this.textRenderer, string, x, y, textColor, false);
                }
            }
        }
    }

    private static String getBiomeString(RegistryEntry<Biome> biome) {
        return biome.getKeyOrValue().map(
                (biomeKey) -> biomeKey.getValue().toString(),
                (biome_) -> "[unregistered " + biome_ + "]"
        );
    }
}
