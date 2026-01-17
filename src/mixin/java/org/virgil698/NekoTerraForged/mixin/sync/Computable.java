package org.virgil698.NekoTerraForged.mixin.sync;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可计算任务的并发控制
 * 支持共享/独占模式、阻塞/自旋等待
 */
public class Computable {
    public static final int SHARED = 2;
    public static final int BLOCK = 4;
    public static final int SPIN = 8;
    public static final int DEFAULT_UNIQUE = 4;
    public static final int DEFAULT_SHARED = 6;
    public static final int COUNTDOWN_LATCH = 2;
    
    private static final int STATE_INITIAL = 0;
    private static final int STATE_COMPLETE = -1;
    
    final AtomicInteger state = new AtomicInteger(0);

    public final boolean compute() {
        return compute(4);
    }

    public final void complete() {
        complete(4);
    }

    public final boolean compute(int flags) {
        int currentState;
        if ((flags & 2) == 2) {
            do {
                currentState = this.state.get();
                if (currentState == STATE_COMPLETE) {
                    return false;
                }
            } while (!this.state.compareAndSet(currentState, currentState + 1));
            return true;
        }
        int currentState2 = this.state.get();
        if (currentState2 == 0 && this.state.compareAndSet(0, 1)) {
            return true;
        }
        if (currentState2 != STATE_COMPLETE && (flags & 4) == 4) {
            await(flags);
            return false;
        }
        return false;
    }

    public final void complete(int flags) {
        if ((flags & 2) != 2) {
            int oldState = this.state.getAndSet(STATE_COMPLETE);
            assert oldState != 0;
            assert oldState != STATE_COMPLETE;
            notify(flags);
            return;
        }
        while (true) {
            int currentState = this.state.get();
            if (currentState == 0 || currentState == STATE_COMPLETE) {
                return;
            }
            if (currentState == 1 && this.state.compareAndSet(currentState, STATE_COMPLETE)) {
                notify(flags);
                return;
            } else if (currentState > 1 && this.state.compareAndSet(currentState, currentState - 1)) {
                if ((flags & 4) == 4) {
                    await(flags);
                    return;
                }
                return;
            }
        }
    }

    protected final void notify(int flags) {
        if ((flags & 8) != 8) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    protected final void await(int flags) {
        if ((flags & 8) == 8) {
            while (this.state.get() != STATE_COMPLETE) {
                Thread.onSpinWait();
            }
        } else {
            synchronized (this) {
                while (this.state.get() != STATE_COMPLETE) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
