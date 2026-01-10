package org.virgil698.NekoTerraForged.mixin.worldgen.noise.function;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * Worley 噪声的单元格函数
 * 移植自 ReTerraForged
 */
public enum CellFunction {
    CELL_VALUE("CELL_VALUE") {
        @Override
        public float apply(int seed, int xc, int yc, float distance, NoiseUtil.Vec2f vec2f, Noise lookup) {
            return NoiseUtil.valCoord2D(seed, xc, yc);
        }
    },
    NOISE_LOOKUP("NOISE_LOOKUP") {
        @Override
        public float apply(int seed, int xc, int yc, float distance, NoiseUtil.Vec2f vec2f, Noise lookup) {
            return lookup.compute(xc + vec2f.x(), yc + vec2f.y(), seed);
        }

        @Override
        public float mapValue(float value, float min, float max, float range) {
            return value;
        }
    },
    DISTANCE("DISTANCE") {
        @Override
        public float apply(int seed, int xc, int yc, float distance, NoiseUtil.Vec2f vec2f, Noise lookup) {
            return distance - 1.0F;
        }

        @Override
        public float mapValue(float value, float min, float max, float range) {
            return 0.0F;
        }
    };

    private final String name;

    CellFunction(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract float apply(int seed, int xc, int yc, float distance, NoiseUtil.Vec2f vec2f, Noise lookup);

    public float mapValue(float value, float min, float max, float range) {
        return NoiseUtil.map(value, min, max, range);
    }
}
