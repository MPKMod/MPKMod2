package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.mixin;

import io.github.kurrycat.mpkmod.compatibility.API;
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
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().handle()) {
            API.Events.onMouseInput(
                    io.github.kurrycat.mpkmod.util.Mouse.Button.NONE,
                    io.github.kurrycat.mpkmod.util.Mouse.State.NONE,
                    (int) x, (int) y, (int) accumulatedDX, (int) -accumulatedDY,
                    0, System.nanoTime()
            );
        }
    }

    @Inject(method = "onScroll", at = @At(value = "TAIL"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        API.Events.onMouseInput(
                io.github.kurrycat.mpkmod.util.Mouse.Button.NONE,
                io.github.kurrycat.mpkmod.util.Mouse.State.NONE,
                (int) xpos, (int) ypos, 0, 0,
                (int) vertical, System.nanoTime()
        );
    }

    @Inject(method = "onButton", at = @At(value = "TAIL"))
    private void onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        API.Events.onMouseInput(
                io.github.kurrycat.mpkmod.util.Mouse.Button.fromInt(input.button()),
                input.button() == -1 ? io.github.kurrycat.mpkmod.util.Mouse.State.NONE :
                        (action == 1 ? io.github.kurrycat.mpkmod.util.Mouse.State.DOWN : io.github.kurrycat.mpkmod.util.Mouse.State.UP),
                (int) xpos, (int) ypos, 0, 0,
                0, System.nanoTime()
        );
    }
}
