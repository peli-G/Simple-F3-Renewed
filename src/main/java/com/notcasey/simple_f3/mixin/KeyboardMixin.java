package com.notcasey.simple_f3.mixin;

import com.notcasey.simple_f3.SimpleF3;
import com.notcasey.simple_f3.config.SimpleF3Config;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow
    private MinecraftClient client;

    @Shadow
    private boolean switchF3State;

    @Inject(method = "onKey(JIIII)V", at = @At("TAIL"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (SimpleF3Config.IsDisabled())
            return;

        // passEvents was removed in 1.21.x; just check for null screen
        if (this.client.currentScreen == null) {
            if (action == 0 && key == 292 && !this.switchF3State) {
                // debugEnabled/debugProfilerEnabled/debugTpsEnabled were removed from GameOptions in 1.21.x.
                // Use getDebugHud().shouldShowDebugHud() to check vanilla debug HUD state.
                boolean vanillaDebugVisible = this.client.getDebugHud().shouldShowDebugHud();
                if (!vanillaDebugVisible && !SimpleF3.simpleDebugEnabled) {
                    SimpleF3.simpleDebugEnabled = true;
                } else if (vanillaDebugVisible && SimpleF3.simpleDebugEnabled) {
                    SimpleF3.simpleDebugEnabled = false;
                }
            }
        }
    }
}
