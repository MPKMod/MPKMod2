package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11.mixin.KeyMappingAccessor;
import io.github.kurrycat.mpkmod.ticks.ButtonMS;
import io.github.kurrycat.mpkmod.ticks.ButtonMSList;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class EventHandler {
    private static final ButtonMSList timeQueue = new ButtonMSList();

    /**
     * @param input The Minecraft {@link KeyEvent} object.
     * @param action   The action, where 0 = unpressed, 1 = pressed, 2 = held.
     */
    public void onKey(KeyEvent input, int action) {
        Options options = Minecraft.getInstance().options;
        long eventNanos = Util.getNanos();

        InputConstants.Key inputKey = InputConstants.getKey(new KeyEvent(input.key(), input.scancode(), input.modifiers()));

        int[] keys = {
                ((KeyMappingAccessor) options.keyUp).getKey().getValue(),
                ((KeyMappingAccessor) options.keyLeft).getKey().getValue(),
                ((KeyMappingAccessor) options.keyDown).getKey().getValue(),
                ((KeyMappingAccessor) options.keyRight).getKey().getValue(),
                ((KeyMappingAccessor) options.keySprint).getKey().getValue(),
                ((KeyMappingAccessor) options.keyShift).getKey().getValue(),
                ((KeyMappingAccessor) options.keyJump).getKey().getValue()
        };

        for (int i = 0; i < keys.length; i++) {
            if (input.key() == keys[i]) {
                timeQueue.add(ButtonMS.of(ButtonMS.Button.values()[i], eventNanos, action == 1));
            }
        }

        if (action == 1) {
            FunctionCompatibility.pressedButtons.add(inputKey.getValue());
        } else if (action == 0) {
            FunctionCompatibility.pressedButtons.remove(inputKey.getValue());
        }

        API.Events.onKeyInput(input.key(), inputKey.getDisplayName().getString(), action == 1);

        if (action != 0) {
            checkKeyBinding(input.key());
        }
    }

    public void onMouseMove(double x, double y, double dx, double dy) {
        API.Events.onMouseInput(
                io.github.kurrycat.mpkmod.util.Mouse.Button.NONE,
                io.github.kurrycat.mpkmod.util.Mouse.State.NONE,
                (int) x, (int) y, (int) dx, (int) dy,
                0, System.nanoTime()
        );
    }

    public void onMouseScroll(double vertical, double x, double y) {
        API.Events.onMouseInput(
                io.github.kurrycat.mpkmod.util.Mouse.Button.NONE,
                io.github.kurrycat.mpkmod.util.Mouse.State.NONE,
                (int) x, (int) y, 0, 0,
                (int) vertical, System.nanoTime()
        );
    }

    public void onMouseButton(MouseButtonInfo input, int action, double x, double y) {
        API.Events.onMouseInput(
                io.github.kurrycat.mpkmod.util.Mouse.Button.fromInt(input.button()),
                input.button() == -1 ? io.github.kurrycat.mpkmod.util.Mouse.State.NONE :
                        (action == 1 ? io.github.kurrycat.mpkmod.util.Mouse.State.DOWN : io.github.kurrycat.mpkmod.util.Mouse.State.UP),
                (int) x, (int) y, 0, 0,
                0, System.nanoTime()
        );

        if (action == 1)
            checkKeyBinding(input.button());
    }

    private void checkKeyBinding(int keyCode) {
        if (Minecraft.getInstance().screen != null) return;

        for (Map.Entry<String, KeyMapping> keyBindingEntry : MPKMod.keyBindingMap.entrySet()) {
            InputConstants.Key boundKey = ((KeyMappingAccessor) keyBindingEntry.getValue()).getKey();
            String keyBindId = keyBindingEntry.getKey();

            if (boundKey.getValue() == keyCode) {
                API.Events.onKeybind(keyBindId);
                return;
            }
        }
    }

    public void onInGameOverlayRender(GuiGraphics drawContext, DeltaTracker renderTickCounter) {
        drawContext.pose().pushMatrix();
        API.<FunctionCompatibility>getFunctionHolder().drawContext = drawContext;
        API.Events.onRenderOverlay();
        drawContext.pose().popMatrix();
    }

    public void onRenderWorldOverlay(PoseStack matrixStack, float tickDelta) {
        MPKMod.INSTANCE.matrixStack = matrixStack;
        matrixStack.pushPose();
        Vec3 pos = Minecraft.getInstance().gameRenderer.getMainCamera().position().reverse();
        MPKMod.INSTANCE.matrixStack.translate(pos);
        API.Events.onRenderWorldOverlay(tickDelta);
        matrixStack.popPose();
    }

    public void onClientTickStart(Minecraft mc) {
        if (mc.isPaused() || mc.level == null) return;
        API.Events.onTickStart();
    }

    public void onClientTickEnd(Minecraft mc) {
        if (mc.isPaused() || mc.level == null) return;
        LocalPlayer mcPlayer = mc.player;

        if (mcPlayer != null) {
            AABB playerBB = mcPlayer.getBoundingBox();
            new Player()
                    .setPos(new Vector3D(mcPlayer.getX(), mcPlayer.getY(), mcPlayer.getZ()))
                    .setLastPos(new Vector3D(mcPlayer.xo, mcPlayer.yo, mcPlayer.zo))
                    .setMotion(new Vector3D(mcPlayer.getDeltaMovement().x, mcPlayer.getDeltaMovement().y, mcPlayer.getDeltaMovement().z))
                    .setRotation(mcPlayer.getRotationVector().y, mcPlayer.getRotationVector().x)
                    .setOnGround(mcPlayer.onGround())
                    .setSprinting(mcPlayer.isSprinting())
                    .setBoundingBox(new BoundingBox3D(
                            new Vector3D(playerBB.minX, playerBB.minY, playerBB.minZ),
                            new Vector3D(playerBB.maxX, playerBB.maxY, playerBB.maxZ)
                    ))
                    .setFlying(mcPlayer.getAbilities().flying)
                    .constructKeyInput()
                    .setKeyMSList(timeQueue)
                    .buildAndSave();
            timeQueue.clear();
        }

        API.Events.onTickEnd();
    }


    public void onServerConnect(ClientPacketListener clientPlayNetworkHandler, PacketSender packetSender, Minecraft minecraftClient) {
        API.Events.onServerConnect(clientPlayNetworkHandler.getConnection().isMemoryConnection());
    }

    public void onServerDisconnect(ClientPacketListener clientPlayNetworkHandler, Minecraft minecraftClient) {
        API.Events.onServerDisconnect();
    }
}
