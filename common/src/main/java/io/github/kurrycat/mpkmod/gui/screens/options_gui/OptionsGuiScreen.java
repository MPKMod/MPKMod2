package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Anchor;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.settings.BooleanSetting;
import io.github.kurrycat.mpkmod.settings.Setting;
import io.github.kurrycat.mpkmod.settings.Settings;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.ArrayList;

// TODO: This whole gui needs a cleanup
// We no longer need the apply button
// Theres a lot of most likely unnecessary code
public class OptionsGuiScreen extends ComponentScreen {
    private OptionList optionList;

    @Override
    public boolean shouldCreateKeyBind() {
        return true;
    }

    @Override
    public void onGuiInit() {
        super.onGuiInit();
        optionList = new OptionList(
                new Vector2D(0, 16),
                new Vector2D(3 / 5D, -40),
                (ArrayList<Setting>) Settings.getSettingsList()
        );
        addChild(optionList, PERCENT.SIZE_X, Anchor.TOP_CENTER);

        optionList.topCover.addChild(
                new Button(
                        "x",
                        new Vector2D(5, 1),
                        new Vector2D(11, 11),
                        mouseButton -> close()
                ),
                PERCENT.NONE, Anchor.CENTER_RIGHT
        );

        optionList.bottomCover.setHeight(24, false);
        optionList.bottomCover.backgroundColor = null;

        optionList.bottomCover.addChild(new Button(
                        "Apply",
                        new Vector2D(-2, 2),
                        new Vector2D(100, 20),
                        mouseButton -> optionList.updateAll()
                ),
                PERCENT.NONE, Anchor.BOTTOM_RIGHT, Anchor.BOTTOM_CENTER
        );

        optionList.bottomCover.addChild(new Button(
                        "Reset all",
                        new Vector2D(2, 2),
                        new Vector2D(100, 20),
                        mouseButton -> optionList.resetAllToDefault()
                ),
                PERCENT.NONE, Anchor.BOTTOM_LEFT, Anchor.BOTTOM_CENTER
        );
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Settings.saveSettings();
    }

    public void render(Vector2D mouse, float partialTicks) {
        super.render(mouse, partialTicks);
        optionList.renderHover(mouse);
    }

    public static class OptionList extends ScrollableList<OptionListItem> {
        public OptionList(Vector2D pos, Vector2D size, ArrayList<Setting> options) {
            this.setPos(pos);
            this.setSize(size);
            this.setTitle("Options");
            items.clear();
            for (Setting option : options) {
                //if(!option.shouldShowInOptionList()) continue;

                OptionListItem item;
                if (option instanceof BooleanSetting) {
                    item = new OptionListItemBoolean(this, (BooleanSetting) option);
                } else {
                    continue;
                }
                items.add(item);
            }
            this.scrollBar.constrainScrollAmountToScreen();
        }

        @Override
        public void render(Vector2D mouse) {
            super.render(mouse);
            renderComponents(mouse);
        }

        public void resetAllToDefault() {
            for (OptionListItem item : items) {
                item.loadDefaultValue();
            }
        }

        public void updateAll() {
            for (OptionListItem item : items) {
                item.update();
            }
        }
    }
}
