package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.InputConstants;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.ticks.ButtonMS;
import io.github.kurrycat.mpkmod.ticks.ButtonMSList;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;
import org.lwjgl.input.Keyboard;

import java.util.Map;

@SuppressWarnings("unused")
public class EventListener {
    private static final ButtonMSList timeQueue = new ButtonMSList();

    public static void onKey() {
        int keyCode = Keyboard.getEventKey();
        String key = Keyboard.getKeyName(keyCode);
        boolean pressed = Keyboard.getEventKeyState();

        if (keyCode == 0) {
            char c = Keyboard.getEventCharacter();
            keyCode = 256 + c;
            key = String.valueOf(c);
        }

        GameOptions gameSettings = Minecraft.getInstance().options;

        int[] keys = {
                gameSettings.forwardKey.getKeyCode(),
                gameSettings.leftKey.getKeyCode(),
                gameSettings.backKey.getKeyCode(),
                gameSettings.rightKey.getKeyCode(),
                gameSettings.sprintKey.getKeyCode(),
                gameSettings.sneakKey.getKeyCode(),
                gameSettings.jumpKey.getKeyCode()
        };

        for (int i = 0; i < keys.length; i++)
            if (keyCode == keys[i])
                timeQueue.add(ButtonMS.of(ButtonMS.Button.values()[i], Keyboard.getEventNanoseconds(), pressed));


        API.Events.onKeyInput(InputConstants.convert(keyCode), key, pressed);

        if (pressed) {
            checkKeyBinding(keyCode);
        }
    }

    public static void onMouseEvent(int button, boolean buttonstate, int x, int y, int dx, int dy, int dwheel, long nanoseconds) {
        API.Events.onMouseInput(
                Mouse.Button.fromInt(button),
                button == -1 ? Mouse.State.NONE : (buttonstate ? Mouse.State.DOWN : Mouse.State.UP),
                x, y, dx, dy,
                dwheel, nanoseconds
        );

        if (buttonstate)
            checkKeyBinding(button - 100);
    }

    private static void checkKeyBinding(int keyCode) {
        if (Minecraft.getInstance().screen != null) return;

        for (Map.Entry<String, KeyBinding> keyBindingEntry : MPKMod.keyBindingMap.entrySet()) {
            KeyBinding keyBinding = keyBindingEntry.getValue();
            String keyBindingId = keyBindingEntry.getKey();

            if (keyBinding.getKeyCode() == keyCode) {
                API.Events.onKeybind(keyBindingId);
                return;
            }
        }
    }

    public static void onTickStart(Minecraft mc) {
        if (mc.isPaused() || mc.world == null) return;
        API.Events.onTickStart();
    }

    public static void onTickEnd(Minecraft mc) {
        if (mc.isPaused() || mc.world == null) return;
        Entity cameraEntity = mc.getCamera();

        if (cameraEntity != null) {
            Box cameraEntityBB = cameraEntity.getShape();
            Player mpkPlayer = new Player()
                    .setPos(new Vector3D(cameraEntity.x, cameraEntity.y, cameraEntity.z))
                    .setLastPos(new Vector3D(cameraEntity.lastX, cameraEntity.lastY, cameraEntity.lastZ))
                    .setMotion(new Vector3D(cameraEntity.velocityX, cameraEntity.velocityY, cameraEntity.velocityZ))
                    .setRotation(cameraEntity.yaw, cameraEntity.pitch)
                    .setOnGround(cameraEntity.onGround)
                    .setSprinting(cameraEntity.isSprinting())
                    .setBoundingBox(new BoundingBox3D(
                        new Vector3D(cameraEntityBB.minX, cameraEntityBB.minY, cameraEntityBB.minZ),
                        new Vector3D(cameraEntityBB.maxX, cameraEntityBB.maxY, cameraEntityBB.maxZ)
                    ))
                    .constructKeyInput()
                    .setKeyMSList(timeQueue.copy());

            if (cameraEntity instanceof PlayerEntity) {
                mpkPlayer.setFlying(((PlayerEntity) cameraEntity).abilities.flying);
            }

            mpkPlayer.buildAndSave();

            timeQueue.clear();
        }
            API.Events.onTickEnd();
    }

    public static void onServerConnect(ClientConnectionEvents.PlayReadyContext ctx) {
        API.Events.onServerConnect(ctx.isServerLocal());
    }

    public static void onServerDisconnect(ClientConnectionEvents.DisconnectContext ctx) {
        API.Events.onServerDisconnect();
    }
}
