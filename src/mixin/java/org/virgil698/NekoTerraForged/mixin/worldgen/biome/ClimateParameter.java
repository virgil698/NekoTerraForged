package org.virgil698.NekoTerraForged.mixin.worldgen.biome;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;

/**
 * 气候参数接口
 * 移植自 ReTerraForged
 */
public interface ClimateParameter {
    float min();
    
    float max();
    
    default float mid() {
        return (this.min() + this.max()) / 2.0F;
    }
    
    default Noise source() {
        return Noises.constant(this.mid());
    }
}
