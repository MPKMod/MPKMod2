package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.gui.components.Anchor;
import io.github.kurrycat.mpkmod.gui.components.CheckButton;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.settings.BooleanSetting;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class OptionListItemBoolean extends OptionListItem {
    private final CheckButton checkButton;

    public OptionListItemBoolean(ScrollableList<OptionListItem> parent, BooleanSetting option) {
        super(parent, option);

        checkButton = new CheckButton(new Vector2D(50, 0), option::setValue);
        checkButton.setChecked(option.getValue());
        addChild(checkButton, PERCENT.NONE, Anchor.CENTER_RIGHT);

    }

    @Override
    public void loadDefaultValue() {
        if (checkButton.enabled) {
            checkButton.setChecked(((BooleanSetting)option).getDefaultValue());
        }
    }

    protected void updateDisplayValue() {
        if (checkButton.enabled) {
            checkButton.setChecked(((BooleanSetting)option).getValue());
        }
    }

    @Override
    public void update() {
        if (checkButton.enabled) {
            ((BooleanSetting)option).setValue(checkButton.isChecked());
        }
    }

    @Override
    protected boolean isDefaultValue() {
        return ((BooleanSetting)option).getValue() == ((BooleanSetting)option).getDefaultValue();
    }

    protected void renderTypeSpecific(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
        checkButton.render(mouse);
    }
}
