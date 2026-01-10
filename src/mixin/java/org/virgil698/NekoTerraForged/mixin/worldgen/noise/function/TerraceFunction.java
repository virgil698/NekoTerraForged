package org.virgil698.NekoTerraForged.mixin.worldgen.noise.function;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 阶梯函数
 * 移植自 ReTerraForged
 */
public record TerraceFunction(float inputRange, float ramp, float cliff, float rampHeight, float blendRange, Step[] steps) implements CurveFunction {

    public TerraceFunction(float inputRange, float ramp, float cliff, float rampHeight, float blendRange, int steps) {
        this(inputRange, ramp, cliff, rampHeight, blendRange, createSteps(inputRange, blendRange, steps));
    }

    @Override
    public float apply(float f) {
        float input = NoiseUtil.clamp(f, 0.0F, 0.999999F);
        int index = NoiseUtil.floor(input * this.steps.length);
        Step step = this.steps[index];
        if (index == this.steps.length - 1) {
            return step.value;
        }
        if (input < step.lowerBound) {
            return step.value;
        }
        if (input > step.upperBound) {
            Step next = this.steps[index + 1];
            return next.value;
        }
        float ramp = 1.0F - this.ramp * 0.5F;
        float cliff = 1.0F - this.cliff * 0.5F;
        float alpha = (input - step.lowerBound) / (step.upperBound - step.lowerBound);
        float value = step.value;
        if (alpha > ramp) {
            Step next2 = this.steps[index + 1];
            float rampSize = 1.0F - ramp;
            float rampAlpha = (alpha - ramp) / rampSize;
            float rampHeight = this.rampHeight;
            value += (next2.value - value) * rampAlpha * rampHeight;
        }
        if (alpha > cliff) {
            Step next2 = this.steps[index + 1];
            float cliffAlpha = (alpha - cliff) / (1.0F - cliff);
            value = NoiseUtil.lerp(value, next2.value, cliffAlpha);
        }
        return value;
    }

    private static Step[] createSteps(float inputRange, float blendRange, int steps) {
        float spacing = inputRange / (steps - 1);
        Step[] array = new Step[steps];
        for (int i = 0; i < steps; ++i) {
            float value = i * spacing;
            array[i] = Step.create(value, spacing, blendRange);
        }
        return array;
    }

    private record Step(float value, float lowerBound, float upperBound) {
        public static Step create(float value, float distance, float blendRange) {
            float blend = distance * blendRange;
            float bound = (distance - blend) / 2.0F;
            return new Step(value, value - bound, value + bound);
        }
    }
}
