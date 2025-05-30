package io.github.jumperonjava.kpz_atm_mod.client.ui;

import io.github.jumperonjava.kpz_atm_mod.client.RequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.SimpleRequestQueue;
import io.github.jumperonjava.kpz_atm_mod.client.ui.state.AtmScreenState;
import io.github.jumperonjava.kpz_atm_mod.client.ui.state.LoginState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class AtmScreen extends Screen {

    public int viewWidth = 200;
    public int viewHeight = 250;
    public RequestQueue requestQueue;
    AtmScreenState state;

    public AtmScreen(RequestQueue queue) {
        super(Text.empty());

        this.requestQueue = queue;
        state = new LoginState(this);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        clearChildren();

        state.renderState();
        state.innerComponents().forEach(this::addDrawableChild);
    }


    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawCenteredTextWithShadow(textRenderer,state.title(),width/2,height/2-viewHeight/2-14,0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        context.fill(width/2-viewWidth/2,height/2-viewHeight/2,width/2+viewWidth/2,height/2+viewHeight/2,AtmColors.BACKGROUND);
    }

    public void setState(AtmScreenState state) {
        this.state = state;
        init();
    }
}
