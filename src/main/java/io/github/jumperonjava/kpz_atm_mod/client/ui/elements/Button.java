package io.github.jumperonjava.kpz_atm_mod.client.ui.elements;

import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmColors;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.event.Listener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.ArrayList;
import java.util.List;

public class Button implements Component{
    private final Text text;
    private final int centerX;
    private final int centerY;
    private final int width;
    private final int height;
    private final Action action;

    private static MinecraftClient client = MinecraftClient.getInstance();

    private Button(Text text, int centerX, int centerY, int width, int height, Action action) {
        this.text = text;
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.action = action;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(centerX - width / 2, centerY - height / 2, centerX + width / 2, centerY + height / 2, isMouseOver(mouseX,mouseY) ? AtmColors.BUTTON_HOVER : AtmColors.BUTTON);
        int expectedWidth = client.textRenderer.getWidth(text);
        context.drawWrappedTextWithShadow(client.textRenderer, text, centerX-expectedWidth/2, centerY-4, width, Colors.WHITE);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return Math.abs(mouseX - centerX) < (double) width /2 && Math.abs(mouseY - centerY) < (double) height /2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            action.execute();
            return true;
        }
        return false;
    }


    //Required by Element interface to allow focusing and tab execution

    boolean focused = false;
    @Override
    public void setFocused(boolean focused) {
        this.focused = true;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }


    //Required by Selectable interface to allow narrator

    @Override
    public SelectionType getType() {
        return SelectionType.HOVERED;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, text);
    }

    public static class Builder {
        private Text text = Text.empty();
        private int centerX = 0;
        private int centerY = 0;
        private int width = 0;
        private int height = 20;
        private Action action = ()->{};

        List<Listener<Integer>> listeners = new ArrayList<>();

        public Builder text(Text text) {
            this.text = text;
            return this;
        }

        public Builder centerX(int centerX) {
            this.centerX = centerX;
            return this;
        }

        public Builder centerY(int centerY) {
            this.centerY = centerY;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder position(int x, int y) {
            this.centerX = x;
            this.centerY = y;
            return this;
        }

        public Builder action(Action action) {
            this.action = action;
            return this;
        }


        public Button build() {
            return new Button(this.text, this.centerX, this.centerY, this.width, this.height, this.action);
        }
    }

}
