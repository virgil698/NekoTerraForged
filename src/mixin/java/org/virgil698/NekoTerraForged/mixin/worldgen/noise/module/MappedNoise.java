package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

/**
 * 映射噪声接口
 * 移植自 ReTerraForged
 */
public interface MappedNoise extends Noise {
    
    @Override
    default Noise mapAll(Visitor visitor) {
        return visitor.apply(this);
    }
    
    /**
     * 标记接口，用于延迟计算的噪声
     */
    public interface Marker extends Noise {

        @Override
        default float compute(float x, float z, int seed) {
            throw new UnsupportedOperationException();
        }
            
        @Override
        default float minValue() {
            return Float.NEGATIVE_INFINITY;
        }

        @Override
        default float maxValue() {
            return Float.POSITIVE_INFINITY;
        }
    }
}
