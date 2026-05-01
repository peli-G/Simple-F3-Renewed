package com.notcasey.simple_f3.mixin;

import com.notcasey.simple_f3.SimpleDebugHud;
import com.notcasey.simple_f3.SimpleF3;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow @Final private MinecraftClient client;

	private static SimpleDebugHud simpleDebugHud;

	// In 1.21.1+, InGameHud constructor only takes MinecraftClient (ItemRenderer was removed)
	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/MinecraftClient;)V")
	private void constructorInject(MinecraftClient client, CallbackInfo info) {
		this.simpleDebugHud = new SimpleDebugHud(client);
	}

	// In 1.21.2+, render takes RenderTickCounter instead of float tickDelta
	@Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
		if (SimpleF3.simpleDebugEnabled) {
			simpleDebugHud.render(context);
		}
	}
}
