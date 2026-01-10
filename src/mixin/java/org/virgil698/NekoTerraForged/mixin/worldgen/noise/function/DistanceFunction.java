package org.virgil698.NekoTerraForged.mixin.worldgen.noise.function;

/**
 * 距离函数枚举
 * 移植自 ReTerraForged
 */
public enum DistanceFunction {
    EUCLIDEAN {
        @Override
        public float apply(float x, float y) {
            return x * x + y * y;
        }
    },
    MANHATTAN {
        @Override
        public float apply(float x, float y) {
            return Math.abs(x) + Math.abs(y);
        }
    },
    NATURAL {
        @Override
        public float apply(float x, float y) {
            return Math.abs(x) + Math.abs(y) + (x * x + y * y);
        }
    };

    public abstract float apply(float x, float z);
}
