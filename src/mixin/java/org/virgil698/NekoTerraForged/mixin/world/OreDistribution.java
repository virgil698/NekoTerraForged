package org.virgil698.NekoTerraForged.mixin.world;

import org.virgil698.NekoTerraForged.mixin.math.Node;

/**
 * 矿石分布生成器
 * 控制不同矿石在不同高度的分布
 */
public class OreDistribution {
    private final Node oreNoise;

    public OreDistribution(int seed) {
        this.oreNoise = Node.Fbm(Node.Scale(Node.Simplex(), 40), 2, 2.0, 0.5);
    }

    /**
     * 获取矿石密度
     */
    public double getOreDensity(int seed, int x, int y, int z, OreType oreType) {
        // 检查高度范围
        if (y < oreType.minY || y > oreType.maxY) {
            return 0.0;
        }

        // 计算高度权重
        double heightFactor = getHeightFactor(y, oreType);

        // 采样噪声
        double noise = oreNoise.eval(seed + oreType.seed, x, z);

        // 组合
        double density = noise * heightFactor * oreType.frequency;

        return Math.max(0.0, density);
    }

    private double getHeightFactor(int y, OreType oreType) {
        int range = oreType.maxY - oreType.minY;
        int optimal = oreType.optimalY;

        if (y < optimal) {
            return (double) (y - oreType.minY) / (optimal - oreType.minY);
        } else {
            return (double) (oreType.maxY - y) / (oreType.maxY - optimal);
        }
    }

    /**
     * 矿石类型
     */
    public enum OreType {
        COAL(0, -64, 192, 96, 0.15),
        IRON(100, -64, 256, 16, 0.12),
        COPPER(200, -16, 112, 48, 0.10),
        GOLD(300, -64, 32, -16, 0.08),
        REDSTONE(400, -64, 16, -32, 0.09),
        DIAMOND(500, -64, 16, -48, 0.06),
        LAPIS(600, -32, 64, 0, 0.07),
        EMERALD(700, -16, 256, 128, 0.04);

        public final int seed;
        public final int minY;
        public final int maxY;
        public final int optimalY;
        public final double frequency;

        OreType(int seed, int minY, int maxY, int optimalY, double frequency) {
            this.seed = seed;
            this.minY = minY;
            this.maxY = maxY;
            this.optimalY = optimalY;
            this.frequency = frequency;
        }
    }
}
