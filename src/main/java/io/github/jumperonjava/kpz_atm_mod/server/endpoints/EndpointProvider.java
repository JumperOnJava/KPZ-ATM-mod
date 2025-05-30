package io.github.jumperonjava.kpz_atm_mod.server.endpoints;

import java.util.Map;

public interface EndpointProvider {
    Map<String,Endpoint> getEndpoints();
}
