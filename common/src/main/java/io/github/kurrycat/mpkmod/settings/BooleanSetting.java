package io.github.kurrycat.mpkmod.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BooleanSetting extends Setting {

    private boolean value;
    private final boolean defaultValue;

    public BooleanSetting(String name, String displayName, String description,  boolean defaultValue) {
        super(name, displayName, description);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public boolean getValue() {
        return value;
    }
    public boolean getDefaultValue() {
        return defaultValue;
    }
    public void setValue(boolean value) {
        boolean oldValue = this.value;
        this.value = value;

        if (oldValue != value) {
            onChangeListeners.forEach(l -> l.accept(this));
        }
    }

    public void setValueNoChangeListener(boolean value) {
        this.value = value;
    }

    @Override
    public JsonNode serialize(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.put("name", name);
        node.put("value", value);
        return node;
    }
}
