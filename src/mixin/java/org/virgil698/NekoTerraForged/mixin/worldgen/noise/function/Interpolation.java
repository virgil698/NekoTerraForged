package org.virgil698.NekoTerraForged.mixin.worldgen.noise.function;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 插值函数枚举
 * 移植自 ReTerraForged
 */
public enum Interpolation {
    LINEAR {
        @Override
        public float apply(float f) {
            return f;
        }
    },
    CURVE3 {
        @Override
        public float apply(float f) {
            return NoiseUtil.interpHermite(f);
        }
    },
    CURVE4 {
        @Override
        public float apply(float f) {
            return NoiseUtil.interpQuintic(f);
        }
    };

    public abstract float apply(float f);
}
