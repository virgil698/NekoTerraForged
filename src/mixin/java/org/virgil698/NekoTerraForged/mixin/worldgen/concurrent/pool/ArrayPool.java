package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.Resource;

/**
 * 数组对象池
 * 用于复用数组对象，减少 GC 压力
 * 移植自 ReTerraForged
 */
public class ArrayPool<T> {
    private final int capacity;
    private final IntFunction<T[]> constructor;
    private final List<Item<T>> pool;
    private final Object lock;

    public ArrayPool(int size, IntFunction<T[]> constructor) {
        this.lock = new Object();
        this.capacity = size;
        this.constructor = constructor;
        this.pool = new ArrayList<>(size);
    }

    /**
     * 获取指定大小的数组资源
     */
    public Resource<T[]> get(int arraySize) {
        synchronized (this.lock) {
            if (!this.pool.isEmpty()) {
                Item<T> resource = this.pool.remove(this.pool.size() - 1);
                if (resource.get().length >= arraySize) {
                    return resource.retain();
                }
            }
        }
        return new Item<>(this.constructor.apply(arraySize), this);
    }

    /**
     * 归还资源到池中
     */
    private boolean restore(Item<T> item) {
        synchronized (this.lock) {
            if (this.pool.size() < this.capacity) {
                this.pool.add(item);
                return true;
            }
        }
        return false;
    }

    /**
     * 创建数组池
     */
    public static <T> ArrayPool<T> of(int size, IntFunction<T[]> constructor) {
        return new ArrayPool<>(size, constructor);
    }

    /**
     * 创建带初始化的数组池
     */
    public static <T> ArrayPool<T> of(int size, Supplier<T> supplier, IntFunction<T[]> constructor) {
        return new ArrayPool<>(size, new ArrayConstructor<>(supplier, constructor));
    }

    /**
     * 数组资源项
     */
    public static class Item<T> implements Resource<T[]> {
        private final T[] value;
        private final ArrayPool<T> pool;
        private boolean released;

        private Item(T[] value, ArrayPool<T> pool) {
            this.released = false;
            this.value = value;
            this.pool = pool;
        }

        @Override
        public T[] get() {
            return this.value;
        }

        @Override
        public boolean isOpen() {
            return !this.released;
        }

        @Override
        public void close() {
            if (!this.released) {
                this.released = true;
                this.released = this.pool.restore(this);
            }
        }

        private Item<T> retain() {
            this.released = false;
            return this;
        }
    }

    /**
     * 数组构造器 - 创建并初始化数组元素
     */
    private static class ArrayConstructor<T> implements IntFunction<T[]> {
        private final Supplier<T> element;
        private final IntFunction<T[]> array;

        private ArrayConstructor(Supplier<T> element, IntFunction<T[]> array) {
            this.element = element;
            this.array = array;
        }

        @Override
        public T[] apply(int size) {
            T[] t = this.array.apply(size);
            for (int i = 0; i < t.length; ++i) {
                t[i] = this.element.get();
            }
            return t;
        }
    }
}
