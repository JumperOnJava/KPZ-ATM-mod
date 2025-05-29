package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;
import net.minecraft.text.Text;

public class StubState extends GenericState {
    private final String loggedInStub;

    public StubState(AtmScreen parent, String loggedInStub) {
        super(parent);
        this.loggedInStub = loggedInStub;
    }

    @Override
    public Text title() {
        return Text.literal("Stub State: " + loggedInStub);
    }

    @Override
    public void renderState() {
    }

}
