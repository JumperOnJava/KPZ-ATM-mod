package io.github.jumperonjava.kpz_atm_mod.client.ui.elements;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;

import java.util.List;

public interface Component extends Element, Drawable, Selectable, ComponentContainer {
    default List<Component> innerComponent(){
        return List.of(this);
    }
}
