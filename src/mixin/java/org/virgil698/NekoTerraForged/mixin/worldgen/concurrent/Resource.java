package org.virgil698.NekoTerraForged.mixin.worldgen.concurrent;

/**
 * 资源接口，用于池化对象管理
 * 移植自 ReTerraForged
 */
public interface Resource<T> extends AutoCloseable {
    Resource<?> NONE = new Resource<>() {
        @Override
        public Object get() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public void close() {
        }
    };

    T get();

    boolean isOpen();

    @Override
    void close();

    @SuppressWarnings("unchecked")
    static <T> Resource<T> empty() {
        return (Resource<T>) Resource.NONE;
    }
}
