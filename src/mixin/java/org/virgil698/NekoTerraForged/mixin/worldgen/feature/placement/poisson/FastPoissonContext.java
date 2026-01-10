package org.virgil698.NekoTerraForged.mixin.worldgen.feature.placement.poisson;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * 快速泊松采样上下文
 * 存储泊松分布放置所需的参数
 * 移植自 ReTerraForged
 */
public record FastPoissonContext(int radius, float jitter, float frequency, Noise density) {

    public FastPoissonContext {
        frequency = Math.min(0.5F, frequency);
        jitter = NoiseUtil.clamp(jitter, 0.0F, 1.0F);
    }
    
    public float scale() {
        return 1.0F / this.frequency;
    }
    
    public int radiusSq() {
        return this.radius * this.radius;
    }
    
    public float pad() {
        return (1.0F - this.jitter) * 0.5F;
    }
}
