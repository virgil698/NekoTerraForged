package org.virgil698.NekoTerraForged.mixin.world;

/**
 * 地形平滑器
 * 使用插值和滤波技术平滑地形
 */
public class TerrainSmoother {
    
    /**
     * 双线性插值
     */
    public static double bilinearInterpolate(double v00, double v10, double v01, double v11, double tx, double ty) {
        double v0 = lerp(v00, v10, tx);
        double v1 = lerp(v01, v11, tx);
        return lerp(v0, v1, ty);
    }

    /**
     * 三次插值（更平滑）
     */
    public static double cubicInterpolate(double v0, double v1, double v2, double v3, double t) {
        double a0 = v3 - v2 - v0 + v1;
        double a1 = v0 - v1 - a0;
        double a2 = v2 - v0;
        double a3 = v1;
        
        double t2 = t * t;
        double t3 = t2 * t;
        
        return a0 * t3 + a1 * t2 + a2 * t + a3;
    }

    /**
     * 双三次插值
     */
    public static double bicubicInterpolate(double[][] values, double tx, double ty) {
        double[] temp = new double[4];
        for (int i = 0; i < 4; i++) {
            temp[i] = cubicInterpolate(values[i][0], values[i][1], values[i][2], values[i][3], tx);
        }
        return cubicInterpolate(temp[0], temp[1], temp[2], temp[3], ty);
    }

    /**
     * 线性插值
     */
    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * 平滑步进函数
     */
    public static double smoothstep(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    /**
     * 更平滑的步进函数
     */
    public static double smootherstep(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    /**
     * 高斯模糊（简化版）
     */
    public static double gaussianBlur(double center, double[] neighbors, double sigma) {
        double sum = center;
        double weight = 1.0;
        
        for (double neighbor : neighbors) {
            double w = Math.exp(-(neighbor - center) * (neighbor - center) / (2.0 * sigma * sigma));
            sum += neighbor * w;
            weight += w;
        }
        
        return sum / weight;
    }

    /**
     * 中值滤波（用于去除噪点）
     */
    public static double medianFilter(double[] values) {
        double[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }

    /**
     * 梯度限制（防止过陡的坡度）
     */
    public static double limitGradient(double current, double target, double maxGradient) {
        double diff = target - current;
        if (Math.abs(diff) > maxGradient) {
            return current + Math.signum(diff) * maxGradient;
        }
        return target;
    }
}
