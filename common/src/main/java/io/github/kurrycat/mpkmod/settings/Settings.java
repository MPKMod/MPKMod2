package io.github.kurrycat.mpkmod.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.JSONConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Settings {

    public static BooleanSetting displayOverlay = new BooleanSetting(
            "displayOverlay",
            "Display Overlay",
            "Whether to show all the components on the overlay while playing",
            true
    );

    public static BooleanSetting renderLastTimingMS = new BooleanSetting(
            "renderLastTimingMS",
            "Display ms for lastTiming",
            "Enable whether milliseconds should be shown in the lastTiming infoVar",
            false
    );

    public static BooleanSetting highlightLandingBlocks = new BooleanSetting(
            "highlightLandingBlocks",
            "Highlight Landing Blocks",
            "Whether to highlight all enabled landing blocks",
            true
    );

    public static BooleanSetting copyPositionShortcutEnabled = new BooleanSetting(
            "copyPositionShortcutEnabled",
            "Copy Position Shortcut",
            "Whether to override the vanilla F3+C shortcut to copy your precise position",
            true
    );

    public static BooleanSetting discordRPCEnabled = new BooleanSetting(
            "discordRPCEnabled",
            "Discord Rich Presence",
            "\"Show \\\"Playing MPKMod\\\" in Discord\"",
            true
    );

    private static final List<Setting> SETTINGS_LIST = new ArrayList<>();

    public static List<Setting> getSettingsList() {
        return SETTINGS_LIST;
    }

    public static void init() {
        for (Field field : Settings.class.getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                try {
                    SETTINGS_LIST.add((Setting) field.get(null));
                } catch (IllegalAccessException ignored) {}
            }
        }
    }

    // For modules
    public static void registerSetting(Setting setting) {
        SETTINGS_LIST.add(setting);
    }

    public static void saveSettings() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode settings = mapper.createArrayNode();

        for (Setting setting : SETTINGS_LIST) {
            settings.add(setting.serialize(mapper));
        }

        Serializer.serialize(JSONConfig.optionsFile, settings, ArrayNode.class);
    }

    public static void loadSettings() {
        ArrayNode settings = Serializer.deserialize(JSONConfig.optionsFile, ArrayNode.class);
        if (settings == null) return;

        for (Setting setting : SETTINGS_LIST) {
            for (int i = 0; i < settings.size(); i++) {
                if (settings.get(i).get("name").asText().equals(setting.getName())) {
                    if (setting instanceof BooleanSetting) {
                        ((BooleanSetting)setting).setValueNoChangeListener(settings.get(i).get("value").asBoolean());
                    }
                    break;
                }
            }
        }
    }

}
