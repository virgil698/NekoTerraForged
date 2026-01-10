package org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.wetland;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Variance;

/**
 * 湿地配置
 * 移植自 ReTerraForged
 */
public class WetlandConfig {
    public int skipSize;
    public Variance length;
    public Variance width;

    public WetlandConfig(float chance, float sizeMin, float sizeMax) {
        this.skipSize = Math.max(1, NoiseUtil.round((1.0F - chance) * 10.0F));
        this.length = Variance.of(sizeMin, sizeMax);
        this.width = Variance.of(50.0F, 150.0F);
    }

    public static WetlandConfig defaults() {
        return new WetlandConfig(0.5F, 150.0F, 450.0F);
    }
}
