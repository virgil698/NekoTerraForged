package org.virgil698.NekoTerraForged.mixin.math;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 种子工具类
 * 移植自 Valley
 */
public class Seed {
    public static int NextInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    public static int Ensure(int seed) {
        return seed != 0 ? seed : ThreadLocalRandom.current().nextInt();
    }
}
