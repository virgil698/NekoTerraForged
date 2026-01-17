package org.virgil698.NekoTerraForged.mixin.sync;

/**
 * 类似 Go 的 WaitGroup，用于等待多个任务完成
 */
public class WaitGroup extends Computable implements AutoCloseable {
    public WaitGroup(int count) {
        this.state.set(count);
    }

    public void done() {
        super.complete(2);
    }

    @Override
    public void close() {
        super.await(2);
    }
}
