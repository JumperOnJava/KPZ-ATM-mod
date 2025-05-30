package io.github.jumperonjava.kpz_atm_mod.client.ui.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.List;

public class TextComponent implements Component {

    private Text text;

    public void setText(Text text) {
        this.text = text;
    }
    private int posY;
    private int posX;

    public TextComponent(Text text, int centerX, int centerY) {
        this.text = text;
        this.posX = centerX;
        this.posY = centerY;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, text, this.posX, this.posY-4, Colors.WHITE);
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public SelectionType getType() {
        return SelectionType.HOVERED;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, text);
    }
    public List<Component> innerComponents(){
        return List.of(this);
    }

}
