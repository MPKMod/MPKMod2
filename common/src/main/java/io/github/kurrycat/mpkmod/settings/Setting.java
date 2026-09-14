package io.github.kurrycat.mpkmod.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Setting {

    protected final List<Consumer<Setting>> onChangeListeners = new ArrayList<>();

    protected final String name;
    protected final String displayName;
    protected final String description;

    protected Setting(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public void onChange(Consumer<Setting> consumer) {
        this.onChangeListeners.add(consumer);
    }

    public abstract JsonNode serialize(ObjectMapper mapper);

}
