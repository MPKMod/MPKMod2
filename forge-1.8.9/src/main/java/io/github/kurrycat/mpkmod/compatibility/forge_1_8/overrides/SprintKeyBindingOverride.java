package io.github.kurrycat.mpkmod.compatibility.forge_1_8.overrides;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class SprintKeyBindingOverride extends KeyBinding {
    public SprintKeyBindingOverride(KeyBinding k) {
        super(k.getKeyDescription(), k.getKeyCodeDefault(), k.getKeyCategory());
    }

    @Override
    public boolean isKeyDown() {
        return Minecraft.isSprintToggled() || super.isKeyDown();
    }
}
