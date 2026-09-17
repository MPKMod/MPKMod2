package io.github.kurrycat.mpkmod.compatibility.fabric_26_3.mixin;

import io.github.kurrycat.mpkmod.compatibility.fabric_26_3.MPKMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Inject(method = "onMove", at = @At(value = "TAIL"))
    private void onCursorPos(long handle, double xpos, double ypos, double xrel, double yrel, CallbackInfo ci) {
        if (handle == Minecraft.getInstance().getWindow().handle()) {
            MPKMod.INSTANCE.eventHandler.onMouseMove(xpos, ypos, accumulatedDX, -accumulatedDY);
        }
    }

    @Inject(method = "onScroll", at = @At(value = "TAIL"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MPKMod.INSTANCE.eventHandler.onMouseScroll(vertical, xpos, ypos);
    }

    @Inject(method = "onButton", at = @At(value = "TAIL"))
    private void onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        if (input.button() != -1) {
            MPKMod.INSTANCE.eventHandler.onMouseButton(input, action, xpos, ypos);
        }
    }
}
