package org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.lake;

import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;

/**
 * 湖泊配置
 * 移植自 ReTerraForged
 */
public class LakeConfig {
    public float depth;
    public float chance;
    public float sizeMin;
    public float sizeMax;
    public float sizeRange;
    public float bankMin;
    public float bankMax;

    private LakeConfig(Builder builder) {
        this.depth = builder.depth;
        this.chance = builder.chance;
        this.sizeMin = builder.sizeMin;
        this.sizeMax = builder.sizeMax;
        this.sizeRange = this.sizeMax - this.sizeMin;
        this.bankMin = builder.bankMin;
        this.bankMax = builder.bankMax;
    }

    public static LakeConfig of(float chance, float sizeMin, float sizeMax, int depth, int minBankHeight, int maxBankHeight, Levels levels) {
        Builder builder = new Builder();
        builder.chance = chance;
        builder.sizeMin = sizeMin;
        builder.sizeMax = sizeMax;
        builder.depth = levels.water(-depth);
        builder.bankMin = levels.water(minBankHeight);
        builder.bankMax = levels.water(maxBankHeight);
        return new LakeConfig(builder);
    }

    public static LakeConfig createDefault(Levels levels) {
        return of(0.2F, 30.0F, 100.0F, 10, 1, 8, levels);
    }

    public static LakeConfig of(Levels levels) {
        return createDefault(levels);
    }

    public static class Builder {
        public float chance;
        public float depth;
        public float sizeMin;
        public float sizeMax;
        public float bankMin;
        public float bankMax;

        public Builder() {
            this.depth = 10.0F;
            this.sizeMin = 30.0F;
            this.sizeMax = 100.0F;
            this.bankMin = 1.0F;
            this.bankMax = 8.0F;
        }
    }
}
