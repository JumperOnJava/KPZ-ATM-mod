package io.github.jumperonjava.kpz_atm_mod.endpoints;

import java.io.Serial;

public class EndpointException extends RuntimeException {
    public final Status status;
    public final String message;

    public EndpointException(Status status, String message) {
        super("%s %s".formatted(status.name(), message));
        this.status = status;
        this.message = message;
    }
}
