package io.github.jumperonjava.kpz_atm_mod.client.ui.elements;

import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.event.Listener;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.event.Notifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextInput extends TextFieldWidget implements Component, Notifier<String> {

    public TextInput(int centerX, int centerY, int width, int height, Text placeholder) {
        super(MinecraftClient.getInstance().textRenderer, centerX-width/2, centerY-height/2, width, height, Text.empty());
        setPlaceholder(placeholder);
        super.setChangedListener(this::notify);
    }

    List<Listener<String>> listeners = new ArrayList<>();

    @Override
    public void startListen(Listener<String> listener) {
        listeners.add(listener);
    }

    @Override
    public void stopListen(Listener<String> listener) {
        listeners.remove(listener);
    }

    @Override
    public void notify(String s) {
        listeners.forEach(listener->listener.accept(s));
    }


    @Override
    public List<Component> realComponents() {
        return List.of(this);
    }
}
