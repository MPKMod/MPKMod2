package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.mixin;

import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.MPKMod;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At(value = "RETURN"))
    private void onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        if (input.key() != -1) {
            MPKMod.INSTANCE.eventHandler.onKey(input, action);
        }
    }
}
