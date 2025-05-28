package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.ui.elements.ComponentContainer;
import net.minecraft.text.Text;

public interface AtmScreenState extends ComponentContainer {
    Text title();
    void initComponents();
}
