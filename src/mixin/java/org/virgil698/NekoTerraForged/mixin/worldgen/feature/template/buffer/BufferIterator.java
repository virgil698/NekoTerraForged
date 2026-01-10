package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.buffer;

/**
 * 缓冲区迭代器接口
 * 移植自ReTerraForged
 */
public interface BufferIterator {
    boolean isEmpty();

    boolean next();

    int nextIndex();
}
