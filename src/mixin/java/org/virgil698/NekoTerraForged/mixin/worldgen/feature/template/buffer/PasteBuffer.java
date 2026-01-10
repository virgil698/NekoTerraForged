package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.buffer;

import java.util.BitSet;

/**
 * 粘贴缓冲区
 * 移植自ReTerraForged
 */
public class PasteBuffer implements BufferIterator {
    private int index = -1;
    private BitSet placed = new BitSet();
    private boolean recordPlaced = false;

    public void reset() {
        this.index = -1;
    }

    public void clear() {
        this.index = -1;
        this.placed.clear();
    }

    public void setRecording(boolean recording) {
        this.recordPlaced = recording;
    }

    @Override
    public boolean isEmpty() {
        return this.placed == null;
    }

    @Override
    public boolean next() {
        this.index = this.placed.nextSetBit(this.index + 1);
        return this.index != -1;
    }

    @Override
    public int nextIndex() {
        return this.index;
    }

    public void record(int i) {
        if (this.recordPlaced) {
            this.placed.set(i);
        }
    }

    public void exclude(int i) {
        if (this.recordPlaced) {
            this.placed.set(i, false);
        }
    }
}
