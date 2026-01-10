package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent;

import java.util.function.Consumer;

/**
 * 简单资源实现
 * 移植自 ReTerraForged
 */
public class SimpleResource<T> implements Resource<T> {
    private final T value;
    private final Consumer<T> closer;
    private boolean open;

    public SimpleResource(T value, Consumer<T> closer) {
        this.open = false;
        this.value = value;
        this.closer = closer;
    }

    @Override
    public T get() {
        this.open = true;
        return this.value;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public void close() {
        if (this.open) {
            this.open = false;
            this.closer.accept(this.value);
        }
    }
}
