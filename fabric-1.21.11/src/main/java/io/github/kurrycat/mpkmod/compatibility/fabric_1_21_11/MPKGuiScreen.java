package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_11;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Profiler;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MPKGuiScreen extends Screen {
    public io.github.kurrycat.mpkmod.gui.MPKGuiScreen eventReceiver;

    public MPKGuiScreen(io.github.kurrycat.mpkmod.gui.MPKGuiScreen screen) {
        super(Component.translatable(API.MODID + ".gui.title"));
        eventReceiver = screen;
    }

    @Override
    public void init() {
        eventReceiver.onInit();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        eventReceiver.onResize(width, height);
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        drawContext.pose().pushMatrix();
        API.<FunctionCompatibility>getFunctionHolder().drawContext = drawContext;
        Profiler.startSection(eventReceiver.getID() == null ? "mpk_gui" : eventReceiver.getID());
        try {
            eventReceiver.drawScreen(new Vector2D(mouseX, mouseY), delta);
        } catch (Exception e) {
            API.LOGGER.warn("Error in drawScreen with id: " + eventReceiver.getID(), e);
        }
        Profiler.endSection();
        drawContext.pose().popMatrix();
    }

    @Override
    public void onClose() {
        super.onClose();
        eventReceiver.onGuiClosed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        eventReceiver.onMouseClicked(new Vector2D(click.x(), click.y()), click.button());
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        eventReceiver.onMouseReleased(new Vector2D(click.x(), click.y()), click.button());
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        eventReceiver.onMouseClickMove(new Vector2D(click.x(), click.y()), click.button(), 0);
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        eventReceiver.onKeyEvent(input.key(), input.scancode(), input.modifiers(), false);
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        eventReceiver.onKeyEvent(input.codepoint(), 0, input.modifiers(), true);
        return super.charTyped(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        eventReceiver.onMouseScroll(
                new Vector2D(mouseX, mouseY),
                (int) (MathUtil.constrain(verticalAmount, -1, 1) * 7)
        );
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
