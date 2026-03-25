package io.github.kurrycat.mpkmod.compatibility.fabric_26_1.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kurrycat.mpkmod.compatibility.fabric_26_1.MPKMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "submitBlockEntities", at = @At("HEAD"))
    public void render(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeStorage submitNodeStorage, CallbackInfo ci) {
        MPKMod.INSTANCE.eventHandler.onRenderWorldOverlay(
                new PoseStack(),
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true)
        );
    }
}
