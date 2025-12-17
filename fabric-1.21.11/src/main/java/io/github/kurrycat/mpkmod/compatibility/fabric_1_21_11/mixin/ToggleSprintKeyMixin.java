package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.mixin;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class ToggleSprintKeyMixin {
    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void sprintOverride(CallbackInfoReturnable<Boolean> cir) {
        KeyMapping self = (KeyMapping) (Object) this;
        KeyMapping sprintKey = net.minecraft.client.Minecraft.getInstance().options.keySprint;

        if (self == sprintKey && Minecraft.isSprintToggled())
            cir.setReturnValue(true);
    }
}
