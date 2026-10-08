package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9.mixin;

import io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9.EventListener;
import io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9.MPKMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        MPKMod.onPostInit();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I"))
    private void onMouse(CallbackInfo ci) {
        int eventButton = org.lwjgl.input.Mouse.getEventButton();
        boolean buttonstate = org.lwjgl.input.Mouse.getEventButtonState();
        int x = org.lwjgl.input.Mouse.getEventX();
        int y = org.lwjgl.input.Mouse.getEventY();
        int dx = org.lwjgl.input.Mouse.getEventDX();
        int dy = org.lwjgl.input.Mouse.getEventDY();
        int dwheel = org.lwjgl.input.Mouse.getEventDWheel();
        long nanoseconds = org.lwjgl.input.Mouse.getEventNanoseconds();

        EventListener.onMouseEvent(eventButton, buttonstate, x, y, dx, dy, dwheel, nanoseconds);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", ordinal = 0))
    private void onKey(CallbackInfo ci) {
        EventListener.onKey();
    }

}
