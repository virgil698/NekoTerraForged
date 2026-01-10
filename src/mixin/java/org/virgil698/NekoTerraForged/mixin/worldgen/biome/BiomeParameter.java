package org.virgil698.NekoTerraForged.mixin.worldgen.biome;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;

/**
 * 生物群系参数接口
 * 移植自 ReTerraForged
 */
public interface BiomeParameter {
    float min();

    float max();

    default float lerp(float alpha) {
        return NoiseUtil.lerp(this.min(), this.max(), alpha);
    }

    default float midpoint() {
        return (this.min() + this.max()) / 2.0F;
    }

    default Noise source() {
        return Noises.constant(this.midpoint());
    }
}
