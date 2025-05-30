package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import net.minecraft.text.Text;

public class StubState extends GenericState {
    private final String text;

    public StubState(AtmScreen parent, String text) {
        super(parent);
        this.text = text;
    }

    @Override
    public Text title() {
        return Text.literal(text);
    }

    @Override
    public void renderState() {
        super.renderState();
    }

}
