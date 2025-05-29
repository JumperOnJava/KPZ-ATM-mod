package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenericState implements AtmScreenState{

    protected final AtmScreen parent;
    int centerX;
    int centerY;

    public GenericState(AtmScreen parent) {
        this.parent = parent;
        centerX = parent.width/2;
        centerY = parent.height/2;
    }

    List<Component> children = new ArrayList<>();
    public List<Component> innerComponents() {
        return children.stream().flatMap(component -> component.innerComponents().stream()).collect(Collectors.toList());
    }
}
