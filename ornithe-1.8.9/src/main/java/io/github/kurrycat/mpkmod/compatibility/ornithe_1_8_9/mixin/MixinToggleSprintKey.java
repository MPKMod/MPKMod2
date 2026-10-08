package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinToggleSprintKey {

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    private void sprintOverride(CallbackInfoReturnable<Boolean> cir) {
        KeyBinding self = (KeyBinding) (Object) this;
        KeyBinding sprintKey = Minecraft.getInstance().options.sprintKey;

        if (self == sprintKey && io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft.isSprintToggled())
            cir.setReturnValue(true);
    }

}
