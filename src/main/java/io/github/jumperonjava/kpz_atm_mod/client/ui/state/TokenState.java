package io.github.jumperonjava.kpz_atm_mod.client.ui.state;

import io.github.jumperonjava.kpz_atm_mod.client.ui.AtmScreen;

public abstract class TokenState extends GenericState {
    String token;

    public TokenState(AtmScreen parent, String token) {
        super(parent);
        this.token = token;
    }
}
