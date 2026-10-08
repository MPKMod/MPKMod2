package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.kurrycat.mpkmod.Main;

public class MPKModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new MPKGuiScreen(Main.mainGUI);
    }

}
