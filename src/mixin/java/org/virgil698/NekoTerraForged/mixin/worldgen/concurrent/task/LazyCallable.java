package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent.task;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * 延迟计算的 Callable
 * 使用 StampedLock 实现高效的延迟初始化
 * 移植自 ReTerraForged
 */
public abstract class LazyCallable<T> implements Callable<T>, Future<T>, Supplier<T> {
    private final StampedLock lock;
    protected volatile T value;

    public LazyCallable() {
        this.lock = new StampedLock();
        this.value = null;
    }

    @Override
    public T call() {
        // 乐观读
        long optRead = this.lock.tryOptimisticRead();
        T result = this.value;
        if (this.lock.validate(optRead) && result != null) {
            return result;
        }
        
        // 悲观读
        long read = this.lock.readLock();
        try {
            result = this.value;
            if (result != null) {
                return result;
            }
        } finally {
            this.lock.unlockRead(read);
        }
        
        // 写锁创建
        long write = this.lock.writeLock();
        try {
            result = this.value;
            if (result == null) {
                result = this.create();
                Objects.requireNonNull(result);
                this.value = result;
            }
            return result;
        } finally {
            this.lock.unlockWrite(write);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        long optRead = this.lock.tryOptimisticRead();
        boolean done = this.value != null;
        if (this.lock.validate(optRead)) {
            return done;
        }
        long read = this.lock.readLock();
        try {
            return this.value != null;
        } finally {
            this.lock.unlockRead(read);
        }
    }

    @Override
    public T get() {
        return this.call();
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return this.call();
    }

    /**
     * 创建值的抽象方法
     */
    protected abstract T create();

    /**
     * 适配 Runnable
     */
    public static LazyCallable<Void> adapt(Runnable runnable) {
        return new RunnableAdapter(runnable);
    }

    /**
     * 适配 Callable
     */
    public static <T> LazyCallable<T> adapt(Callable<T> callable) {
        if (callable instanceof LazyCallable<T> c) {
            return c;
        }
        return new CallableAdapter<>(callable);
    }

    /**
     * 适配已完成的 Callable
     */
    public static <T> LazyCallable<T> adaptComplete(Callable<T> callable) {
        return new CompleteAdapter<>(callable);
    }

    /**
     * Callable 适配器
     */
    public static class CallableAdapter<T> extends LazyCallable<T> {
        private final Callable<T> callable;

        public CallableAdapter(Callable<T> callable) {
            this.callable = callable;
        }

        @Override
        protected T create() {
            try {
                return this.callable.call();
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Future 适配器
     */
    public static class FutureAdapter<T> extends LazyCallable<T> {
        private final Future<T> future;

        public FutureAdapter(Future<T> future) {
            this.future = future;
        }

        @Override
        public boolean isDone() {
            return this.future.isDone();
        }

        @Override
        protected T create() {
            try {
                return this.future.get();
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Runnable 适配器
     */
    public static class RunnableAdapter extends LazyCallable<Void> {
        private final Runnable runnable;

        public RunnableAdapter(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        protected Void create() {
            this.runnable.run();
            return null;
        }
    }

    /**
     * 已完成适配器 - isDone 始终返回 true
     */
    public static class CompleteAdapter<T> extends LazyCallable<T> {
        private final Callable<T> callable;

        public CompleteAdapter(Callable<T> callable) {
            this.callable = callable;
        }

        @Override
        protected T create() {
            try {
                return this.callable.call();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public boolean isDone() {
            return true;
        }
    }
}
