package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenericState implements AtmScreenState{

    protected final AtmScreen parent;

    public GenericState(AtmScreen parent) {
        this.parent = parent;
    }

    List<Component> children;
    public List<Component> innerComponent() {
        var list = new ArrayList<Component>();
        return children.stream().flatMap(component -> component.innerComponent().stream()).collect(Collectors.toList());
    }
}
