package org.virgil698.NekoTerraForged.mixin.sync;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 线程本地存储，支持动态重载
 */
public class Local<T> extends ThreadLocal<Local.Instance<T>> {
    private final AtomicInteger generation = new AtomicInteger(0);
    private volatile Supplier<T> factory = null;

    public void set(Supplier<T> factory) {
        synchronized (this.generation) {
            this.generation.addAndGet(1);
            this.factory = factory;
            System.out.println("Invalidated locals");
        }
    }

    public T value() {
        Instance<T> instance = super.get();
        if (instance.gen != this.generation.get()) {
            synchronized (this.generation) {
                instance = new Instance<>(this.factory.get(), this.generation.get());
                set(instance);
                System.out.println("Reloaded local instance");
            }
        }
        return instance.value;
    }

    @Override
    protected Instance<T> initialValue() {
        return new Instance<>(this.factory.get(), this.generation.get());
    }

    protected static class Instance<T> {
        final T value;
        final int gen;

        Instance(T value, int generation) {
            this.value = value;
            this.gen = generation;
        }
    }
}
