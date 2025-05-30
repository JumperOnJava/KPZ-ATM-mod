package io.github.jumperonjava.kpz_atm_mod.server.networking;

import java.util.Map;

public interface RequestHandlerProvider {
    Map<String, RequestTypeHandler> getRequestHandlers();
}
