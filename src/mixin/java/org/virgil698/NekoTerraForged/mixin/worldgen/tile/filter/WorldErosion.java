package org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter;

import java.util.concurrent.locks.StampedLock;
import java.util.function.IntFunction;

/**
 * 世界侵蚀管理器，使用 StampedLock 实现线程安全的懒加载
 * 移植自 ReTerraForged
 */
public class WorldErosion<T> {
    private volatile T value;
    private final IntFunction<T> factory;
    private final Validator<T> validator;
    private final StampedLock lock;

    public WorldErosion(IntFunction<T> factory, Validator<T> validator) {
        this.value = null;
        this.lock = new StampedLock();
        this.factory = factory;
        this.validator = validator;
    }

    public T get(int ctx) {
        T value = this.readValue();
        if (this.validate(value, ctx)) {
            return value;
        }
        return this.writeValue(ctx);
    }

    private T readValue() {
        long optRead = this.lock.tryOptimisticRead();
        T value = this.value;
        if (!this.lock.validate(optRead)) {
            long stamp = this.lock.readLock();
            try {
                return this.value;
            } finally {
                this.lock.unlockRead(stamp);
            }
        }
        return value;
    }

    private T writeValue(int ctx) {
        long stamp = this.lock.writeLock();
        try {
            if (this.validate(this.value, ctx)) {
                return this.value;
            }
            return this.value = this.factory.apply(ctx);
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    private boolean validate(T value, int ctx) {
        return value != null && this.validator.validate(value, ctx);
    }

    public interface Validator<T> {
        boolean validate(T value, int ctx);
    }
}
