package org.virgil698.NekoTerraForged.mixin.util;

import org.virgil698.NekoTerraForged.mixin.math.Mth;

import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * 高性能缓存实现
 * 提供 Linear、Stamped、Concurrent 三种缓存策略
 */
public interface Cache {
    long NULL_KEY = Long.MIN_VALUE;

    @FunctionalInterface
    interface ComputeFunction<T> {
        T apply(long key);
    }

    static int hash(long l) {
        return Mth.Mix((int) l);
    }

    /**
     * 线性缓存 - 简单的 LRU 缓存
     */
    class Linear<T> {
        protected final int size;
        protected final long[] keys;
        protected final T[] values;

        public Linear(IntFunction<T[]> constructor) {
            this(16, constructor);
        }

        public Linear(int size, IntFunction<T[]> constructor) {
            this.size = size;
            this.keys = new long[size];
            this.values = constructor.apply(size);
            Arrays.fill(this.keys, NULL_KEY);
        }

        public void clear() {
            Arrays.fill(this.keys, NULL_KEY);
            Arrays.fill(this.values, null);
        }

        public T computeIfAbsent(long key, ComputeFunction<T> function) {
            if (this.keys[0] == key) {
                return this.values[0];
            }
            if (this.keys[0] == NULL_KEY) {
                this.keys[0] = key;
                this.values[0] = function.apply(key);
                return this.values[0];
            }
            long lastKey = this.keys[0];
            T lastValue = this.values[0];
            for (int i = 1; i < this.size; i++) {
                long tempKey = this.keys[i];
                this.keys[i] = lastKey;
                lastKey = tempKey;
                T tempValue = this.values[i];
                this.values[i] = lastValue;
                lastValue = tempValue;
                if (lastKey == key) {
                    this.keys[0] = lastKey;
                    this.values[0] = lastValue;
                    break;
                }
                if (lastKey == NULL_KEY) {
                    break;
                }
            }
            if (this.keys[0] != key) {
                this.keys[0] = key;
                this.values[0] = function.apply(key);
            }
            return this.values[0];
        }
    }

    /**
     * Stamped 缓存 - 使用 StampedLock 的线程安全缓存
     */
    class Stamped<T> {
        protected final long[] keys;
        protected final T[] values;
        protected final int mask;
        protected final Consumer<T> removalListener;
        protected final StampedLock lock = new StampedLock();

        public Stamped(int capacity, IntFunction<T[]> constructor, Consumer<T> removalListener) {
            int capacity2 = 1 << Mth.SizeBits(capacity);
            this.mask = capacity2 - 1;
            this.keys = new long[capacity2];
            this.values = constructor.apply(capacity2);
            this.removalListener = removalListener;
            Arrays.fill(this.keys, NULL_KEY);
        }

        public T computeIfAbsent(long key, ComputeFunction<T> function) {
            int index = Cache.hash(key) & this.mask;
            long readStamp = this.lock.tryOptimisticRead();
            long currentKey = this.keys[index];
            T currentValue = this.values[index];
            if (!this.lock.validate(readStamp)) {
                readStamp = this.lock.readLock();
                currentKey = this.keys[index];
                currentValue = this.values[index];
            }
            if (currentKey == key && currentValue != null) {
                if (StampedLock.isReadLockStamp(readStamp)) {
                    this.lock.unlockRead(readStamp);
                }
                return currentValue;
            }
            long writeStamp = this.lock.tryConvertToWriteLock(readStamp);
            if (writeStamp == 0) {
                try {
                    if (StampedLock.isReadLockStamp(readStamp)) {
                        this.lock.unlock(readStamp);
                    }
                    writeStamp = this.lock.writeLock();
                    if (this.keys[index] == key && this.values[index] != null) {
                        T t = this.values[index];
                        this.lock.unlockWrite(writeStamp);
                        if (currentValue != null) {
                            this.removalListener.accept(currentValue);
                        }
                        return t;
                    }
                } catch (Throwable th) {
                    this.lock.unlockWrite(writeStamp);
                    if (currentValue != null) {
                        this.removalListener.accept(currentValue);
                    }
                    throw th;
                }
            }
            T newValue = function.apply(key);
            this.keys[index] = key;
            this.values[index] = newValue;
            this.lock.unlockWrite(writeStamp);
            if (currentValue != null) {
                this.removalListener.accept(currentValue);
            }
            return newValue;
        }

        public void clear() {
            long stamp = this.lock.writeLock();
            try {
                for (int i = 0; i < this.keys.length; i++) {
                    if (this.values[i] != null) {
                        this.removalListener.accept(this.values[i]);
                        this.values[i] = null;
                    }
                    this.keys[i] = NULL_KEY;
                }
            } finally {
                this.lock.unlockWrite(stamp);
            }
        }
    }

    /**
     * 并发缓存 - 分段锁的高并发缓存
     */
    class Concurrent<T> {
        protected static final int HASH_BITS = Integer.MAX_VALUE;
        protected final int mask;
        protected final Stamped<T>[] buckets;
        protected final IntFunction<T[]> constructor;

        public Concurrent(int capacity, int concurrency, IntFunction<T[]> constructor, Consumer<T> removalListener) {
            int concurrency2 = 1 << Mth.SizeBits(concurrency);
            int capacity2 = Mth.Floor(capacity / concurrency2);
            this.mask = concurrency2 - 1;
            this.buckets = createBuckets(concurrency2);
            this.constructor = constructor;
            for (int i = 0; i < concurrency2; i++) {
                this.buckets[i] = new Stamped<>(capacity2, constructor, removalListener);
            }
        }

        public T computeIfAbsent(long key, ComputeFunction<T> function) {
            return this.buckets[index(key)].computeIfAbsent(key, function);
        }

        public void clear() {
            for (Stamped<T> bucket : this.buckets) {
                bucket.clear();
            }
        }

        protected int index(long key) {
            return spread(key) & this.mask;
        }

        protected static int spread(long h) {
            return ((int) (h ^ (h >>> 16))) & HASH_BITS;
        }

        @SuppressWarnings("unchecked")
        protected static <T> Stamped<T>[] createBuckets(int size) {
            return new Stamped[size];
        }
    }
}
