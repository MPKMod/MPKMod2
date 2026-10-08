package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kurrycat.mpkmod.compatibility.API;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Redirect(method = "render(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V"))
    private void onRenderLast(Profiler profiler, String name, @Local(argsOnly = true) float tickDelta) {
        if (name.equals("hand")) { // Inject before rendering the hand
            API.Events.onRenderWorldOverlay(tickDelta);
        }
        profiler.swap(name);
    }

}
