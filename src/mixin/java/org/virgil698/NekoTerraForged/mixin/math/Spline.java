package org.virgil698.NekoTerraForged.mixin.math;

/**
 * 样条插值
 * 移植自 Valley
 */
public class Spline {
    private final double[] inputs;
    private final double[] outputs;

    public Spline(double[] inputs, double[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public double eval(double value) {
        if (inputs.length == 0) {
            return value;
        }
        if (value <= inputs[0]) {
            return outputs[0];
        }
        for (int i = 1; i < inputs.length; i++) {
            if (value <= inputs[i]) {
                double t = (value - inputs[i - 1]) / (inputs[i] - inputs[i - 1]);
                return outputs[i - 1] + ((outputs[i] - outputs[i - 1]) * t);
            }
        }
        return outputs[outputs.length - 1];
    }

    public static Spline Of(double[][] points) {
        double[] inputs = new double[points.length];
        double[] outputs = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            inputs[i] = points[i][0];
            outputs[i] = points[i][1];
        }
        return new Spline(inputs, outputs);
    }
}
