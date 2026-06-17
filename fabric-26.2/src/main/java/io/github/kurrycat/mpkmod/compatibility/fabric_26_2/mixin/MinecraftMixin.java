package io.github.kurrycat.mpkmod.compatibility.fabric_26_2.mixin;

import io.github.kurrycat.mpkmod.compatibility.fabric_26_2.MPKMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(CallbackInfo info) {
		MPKMod.INSTANCE.init();
	}
}
