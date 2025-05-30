package io.github.jumperonjava.kpz_atm_mod.server.networking;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ServerThreadExecutor implements ServerTickEvents.EndTick {
    public CountDownLatch runOnServerThread(Runnable runnable) {
        var ref = new Object() {
            RuntimeException e;
        };

        CountDownLatch latch = new CountDownLatch(1){
            @Override
            public void await() throws InterruptedException {
                super.await();
                if(ref.e != null){
                    throw ref.e;
                }
            }
        };

        tasks.add(()->{
            try{
                runnable.run();
            }
            catch (RuntimeException runtimeException) {
                ref.e = runtimeException;
            }
            catch (Exception e) {
                ref.e = new RuntimeException(e);
            }

            latch.countDown();
        });

        return latch;
    }

    List<Runnable> tasks = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onEndTick(MinecraftServer minecraftServer) {
        var copy = new ArrayList<>(tasks);
        copy.forEach(Runnable::run);
        tasks.removeAll(copy);
    }
}
