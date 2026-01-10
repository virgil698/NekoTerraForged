package org.virgil698.NekoTerraForged.mixin.worldgen.util;

/**
 * 种子管理器，用于生成派生种子
 */
public class Seed {
    private final int root;
    private int value;

    public Seed(int seed) {
        this.root = seed;
        this.value = seed;
    }

    public int root() {
        return root;
    }

    public int next() {
        return ++value;
    }

    public Seed offset(int offset) {
        return new Seed(root + offset);
    }
}
