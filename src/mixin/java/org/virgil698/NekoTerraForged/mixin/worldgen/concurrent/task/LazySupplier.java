package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.task;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 延迟计算的 Supplier
 * 继承自 LazyCallable，提供更简洁的 API
 * 移植自 ReTerraForged
 */
public class LazySupplier<T> extends LazyCallable<T> {
    private final Supplier<T> supplier;

    public LazySupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected T create() {
        return this.supplier.get();
    }

    /**
     * 链式转换
     */
    public <V> LazySupplier<V> then(Function<T, V> mapper) {
        return supplied(this, mapper);
    }

    /**
     * 创建 LazySupplier
     */
    public static <T> LazySupplier<T> of(Supplier<T> supplier) {
        return new LazySupplier<>(supplier);
    }

    /**
     * 从值和函数创建
     */
    public static <V, T> LazySupplier<T> factory(V value, Function<V, T> function) {
        return of(() -> function.apply(value));
    }

    /**
     * 从 Supplier 和函数创建
     */
    public static <V, T> LazySupplier<T> supplied(Supplier<V> supplier, Function<V, T> function) {
        return of(() -> function.apply(supplier.get()));
    }
}
