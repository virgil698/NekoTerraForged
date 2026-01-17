package org.virgil698.NekoTerraForged.mixin.world;

import org.virgil698.NekoTerraForged.mixin.math.Node;

/**
 * 洞穴生成器
 * 使用 3D 噪声生成洞穴系统
 */
public class CaveGenerator {
    private final Node caveNoise;
    private final double threshold;

    public CaveGenerator(int seed) {
        // 使用多层噪声生成洞穴
        Node base = Node.Fbm(Node.Scale(Node.Simplex(), 80), 3, 2.0, 0.5);
        this.caveNoise = base;
        this.threshold = 0.6; // 洞穴阈值
    }

    /**
     * 检查指定位置是否应该是洞穴
     */
    public boolean isCave(int seed, int x, int y, int z) {
        // 只在地下生成洞穴
        if (y > 50 || y < -50) {
            return false;
        }

        // 使用 2D 噪声模拟 3D（简化版本）
        double noise1 = caveNoise.eval(seed, x, y + z);
        double noise2 = caveNoise.eval(seed + 1000, x + y, z);
        double combined = (noise1 + noise2) * 0.5;

        // 深度影响洞穴密度
        double depthFactor = 1.0 - Math.abs(y / 50.0);
        double adjustedThreshold = threshold * depthFactor;

        return combined > adjustedThreshold;
    }

    /**
     * 获取洞穴密度（0-1，1 表示完全是洞穴）
     */
    public double getCaveDensity(int seed, int x, int y, int z) {
        if (y > 50 || y < -50) {
            return 0.0;
        }

        double noise1 = caveNoise.eval(seed, x, y + z);
        double noise2 = caveNoise.eval(seed + 1000, x + y, z);
        double combined = (noise1 + noise2) * 0.5;

        double depthFactor = 1.0 - Math.abs(y / 50.0);
        double adjustedThreshold = threshold * depthFactor;

        if (combined > adjustedThreshold) {
            return (combined - adjustedThreshold) / (1.0 - adjustedThreshold);
        }
        return 0.0;
    }
}
