package io.github.jumperonjava.kpz_atm_mod.client.ui.elements.event;

public interface Notifier<T>{
    void startListen(Listener<T> listener);
    void stopListen(Listener<T> listener);
    void notify(T t);
}
