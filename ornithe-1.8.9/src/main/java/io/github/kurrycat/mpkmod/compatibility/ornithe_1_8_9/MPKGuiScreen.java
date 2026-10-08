package io.github.kurrycat.mpkmod.compatibility.ornithe_1_8_9;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.InputConstants;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Profiler;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Window;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class MPKGuiScreen extends Screen {
    public boolean repeatEventsEnabled;
    public io.github.kurrycat.mpkmod.gui.MPKGuiScreen eventReceiver;

    public MPKGuiScreen(io.github.kurrycat.mpkmod.gui.MPKGuiScreen screen) {
        super();
        eventReceiver = screen;
    }

    public static int createModifiers() {
        int i = 0;
        if (Screen.isShiftDown()) i |= 1;
        if (Screen.isControlDown()) i |= 2;
        if (Screen.isAltDown()) i |= 4;
        return i;
    }

    @Override
    public void init() {
        repeatEventsEnabled = Keyboard.areRepeatEventsEnabled();
        Keyboard.enableRepeatEvents(true);
        super.init();
        eventReceiver.onInit();
    }

    @Override
    public void resize(Minecraft mcIn, int width, int height) {
        super.resize(mcIn, width, height);
        eventReceiver.onResize(width, height);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        Profiler.startSection(eventReceiver.getID() == null ? "unknown" : eventReceiver.getID());
        try {
            eventReceiver.drawScreen(new Vector2D(mouseX, mouseY), partialTicks);
        } catch (Exception e) {
            API.LOGGER.warn("Error in drawScreen with id: " + eventReceiver.getID(), e);
        }
        Profiler.endSection();
    }

    @Override
    public void removed() {
        Keyboard.enableRepeatEvents(repeatEventsEnabled);
        super.removed();
        eventReceiver.onGuiClosed();
    }

    @Override
    protected void keyPressed(char typedChar, int keyCode) {
        super.keyPressed(typedChar, keyCode);
        char c = Keyboard.getEventCharacter();

        eventReceiver.onKeyEvent(InputConstants.convert(Keyboard.getEventKey()), 0, createModifiers(), false);
        if (c >= 32 && c != 127)
            eventReceiver.onKeyEvent(c, 0, createModifiers(), true);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        eventReceiver.onMouseClicked(new Vector2D(mouseX, mouseY), io.github.kurrycat.mpkmod.util.Mouse.Button.fromInt(mouseButton));
    }

    @Override
    protected void mouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        eventReceiver.onMouseClickMove(new Vector2D(mouseX, mouseY), io.github.kurrycat.mpkmod.util.Mouse.Button.fromInt(clickedMouseButton), timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        eventReceiver.onMouseReleased(new Vector2D(mouseX, mouseY), io.github.kurrycat.mpkmod.util.Mouse.Button.fromInt(state));
    }

    @Override
    public void handleMouse() {
        super.handleMouse();

        if (Mouse.getEventDWheel() != 0) {
            Window scaledresolution = new Window(this.minecraft);
            eventReceiver.onMouseScroll(
                    new Vector2D(
                            Mouse.getX() * scaledresolution.getScaledWidth() / this.minecraft.width,
                            scaledresolution.getScaledHeight() - Mouse.getY() * scaledresolution.getScaledHeight() / this.minecraft.height - 1
                    ),
                    Mouse.getEventDWheel() / 40
            );
        }
    }

    @Override
    public boolean shouldPauseGame() {
        return false;
    }
}
