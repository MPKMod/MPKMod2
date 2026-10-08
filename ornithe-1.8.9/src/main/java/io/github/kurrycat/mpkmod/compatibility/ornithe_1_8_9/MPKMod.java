package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpknetapi.common.MPKNetworking;
import io.github.kurrycat.mpknetapi.common.network.packet.MPKPacket;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.resource.Identifier;
import net.ornithemc.osl.core.api.util.NamespacedIdentifier;
import net.ornithemc.osl.core.impl.util.MinecraftVersion;
import net.ornithemc.osl.keybinds.api.KeybindEvents;
import net.ornithemc.osl.keybinds.api.KeybindRegistry;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.networking.api.ChannelRegistry;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class MPKMod implements ModInitializer {

    public static NamespacedIdentifier MPK_ID = new Identifier(MPKNetworking.CHANNEL_NAMESPACE, MPKNetworking.CHANNEL_PATH);

    public static Map<String, KeyBinding> keyBindingMap = new HashMap<>();

    @Override
    public void onInitialize() {
        API.LOGGER.info("Loading " + API.NAME + " " + API.VERSION);
        API.preInit(getClass());

        MinecraftClientEvents.TICK_START.register(EventListener::onTickStart);
        MinecraftClientEvents.TICK_END.register(EventListener::onTickEnd);
        ClientConnectionEvents.PLAY_READY.register(EventListener::onServerConnect);
        ClientConnectionEvents.DISCONNECT.register(EventListener::onServerDisconnect);

        KeybindEvents.REGISTER_KEYBINDS.register(() -> {
            API.guiScreenMap.forEach((id, guiScreen) -> {
                if (guiScreen.shouldCreateKeyBind())
                    registerKeyBinding(id);
            });

            API.keyBindingMap.forEach((id, consumer) -> registerKeyBinding(id));
        });

        ChannelRegistry.register(MPK_ID);
        ClientPlayNetworking.registerListener(MPK_ID, (context, payload) -> {
            byte[] data = new byte[payload.readableBytes()];
            payload.readBytes(data);
            MPKPacket packet = MPKPacket.handle(API.PACKET_LISTENER_CLIENT, data, null);
            if (packet != null) {
                API.Events.onPluginMessage(packet);
            }
        });
    }

    public static void onPostInit() {
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registering compatibility functions...");
        API.registerFunctionHolder(new FunctionCompatibility());
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Done");

        registerKeyBindings();
        API.init(MinecraftVersion.resolve().semanticVersion());

        API.Events.onLoadComplete();
    }

    public static void registerKeyBinding(String id) {
        KeyBinding keyBinding = new KeyBinding(
                API.MODID + ".key." + id + ".desc",
                Keyboard.KEY_NONE,
                API.KEYBINDING_CATEGORY
        );
        keyBindingMap.put(id, keyBinding);
        KeybindRegistry.register(keyBinding);
    }

    private static void registerKeyBindings() {
        for (KeyBinding k : Minecraft.getInstance().options.keyBindings) {
            new io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding(
                    () -> GameOptions.getKeyName(k.getKeyCode()),
                    k.getName(),
                    () -> GameOptions.isPressed(k)
            );
        }
        API.LOGGER.info(API.COMPATIBILITY_MARKER, "Registered {} Keybindings",
                io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding.getKeyMap().size());
    }


}
