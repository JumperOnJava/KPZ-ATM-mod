package io.github.jumperonjava.kpz_atm_mod.server.bank;

import io.github.jumperonjava.kpz_atm_mod.server.Status;

public class EndpointException extends RuntimeException {
    public final Status status;
    public final String message;

    public EndpointException(Status status, String message) {
        super("%s %s".formatted(status.name(), message));
        this.status = status;
        this.message = message;
    }
}
