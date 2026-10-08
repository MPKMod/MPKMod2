package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import io.github.kurrycat.mpkmod.compatibility.API;
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public class MixinGameGui {

    @Definition(id = "overlayMessageCooldown", field = "Lnet/minecraft/client/gui/GameGui;overlayMessageCooldown:I")
    @Expression("this.overlayMessageCooldown > 0")
    @Inject(method = "render(F)V", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.BEFORE))
    public void onRenderGameOverlay(float tickDelta, CallbackInfo ci) {
        API.Events.onRenderOverlay();
    }


}
