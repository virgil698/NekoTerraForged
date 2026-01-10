package org.virgil698.NekoTerraForged.mixin.worldgen.noise.function;

/**
 * æ²çº¿å½æ°工厂
 * 移植è?ReTerraForged
 */
public class CurveFunctions {

    public static SCurveFunction scurve(float lower, float upper) {
        return new SCurveFunction(lower, upper);
    }

    public static TerraceFunction terrace(float inputRange, float ramp, float cliff, float rampHeight, float blendRange, int steps) {
        return new TerraceFunction(inputRange, ramp, cliff, rampHeight, blendRange, steps);
    }
}
