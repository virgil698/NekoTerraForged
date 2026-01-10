package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import com.mojang.serialization.Codec;

/**
 * 噪声接口，所有噪声模块的基础
 * 移植自 ReTerraForged
 */
public interface Noise {
    /**
     * 简化的CODEC - 在插件环境中不使用注册表
     */
    Codec<Noise> DIRECT_CODEC = Codec.unit(ConstantNoise.ZERO);

    /**
     * 计算指定坐标的噪声值
     */
    float compute(float x, float z, int seed);

    /**
     * 获取噪声最小值
     */
    float minValue();

    /**
     * 获取噪声最大值
     */
    float maxValue();

    /**
     * 映射所有子噪声
     */
    Noise mapAll(Visitor visitor);

    /**
     * 噪声访问器接口
     */
    @FunctionalInterface
    interface Visitor {
        Noise apply(Noise input);
    }

    /**
     * 常量噪声实现
     */
    record ConstantNoise(float value) implements Noise {
        public static final ConstantNoise ZERO = new ConstantNoise(0.0F);

        @Override
        public float compute(float x, float z, int seed) {
            return value;
        }

        @Override
        public float minValue() {
            return value;
        }

        @Override
        public float maxValue() {
            return value;
        }

        @Override
        public Noise mapAll(Visitor visitor) {
            return visitor.apply(this);
        }
    }
}
