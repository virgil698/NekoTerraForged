package org.virgil698.NekoTerraForged.mixin.world;

import org.virgil698.NekoTerraForged.mixin.math.Node;
import org.virgil698.NekoTerraForged.mixin.math.Spline;

/**
 * 地形生成器
 * 组合多个噪声层生成复杂地形
 */
public class TerrainGenerator {
    private final Node continentNode;
    private final Node erosionNode;
    private final Node peaksValleysNode;
    private final Node temperatureNode;
    private final Node moistureNode;
    private final Spline heightSpline;

    public TerrainGenerator(int seed) {
        // 大陆噪声 - 控制陆地和海洋的分布
        this.continentNode = Continent.CreateDefault().node();
        
        // 侵蚀噪声 - 控制地形的平滑度
        Node erosionBase = Node.Fbm(Node.Scale(Node.Simplex(), 800), 3, 2.0, 0.5);
        this.erosionNode = Node.Remap(erosionBase, new Spline(
            new double[]{-1.0, 0.0, 1.0},
            new double[]{0.0, 0.5, 1.0}
        ));
        
        // 山峰和山谷噪声 - 添加局部高度变化
        this.peaksValleysNode = Node.Fbm(Node.Scale(Node.Simplex(), 400), 4, 2.0, 0.5);
        
        // 温度噪声 - 用于生物群系
        this.temperatureNode = Node.Fbm(Node.Scale(Node.Simplex(), 2000), 2, 2.0, 0.5);
        
        // 湿度噪声 - 用于生物群系
        this.moistureNode = Node.Fbm(Node.Scale(Node.Simplex(), 2000), 2, 2.0, 0.5);
        
        // 高度映射样条
        this.heightSpline = new Spline(
            new double[]{-1.5, -0.5, -0.19, 0.0, 0.5, 1.0, 1.5},
            new double[]{-64, 20, 63, 80, 120, 180, 250}
        );
    }

    /**
     * 获取指定位置的地形数据
     */
    public TerrainData sample(int seed, int x, int z) {
        TerrainData data = new TerrainData();
        
        // 采样各个噪声层
        data.continentValue = continentNode.eval(seed, x, z);
        data.erosionValue = erosionNode.eval(seed + 1, x, z);
        data.peaksValleysValue = peaksValleysNode.eval(seed + 2, x, z);
        data.temperatureValue = temperatureNode.eval(seed + 3, x, z);
        data.moistureValue = moistureNode.eval(seed + 4, x, z);
        
        // 计算最终高度
        double baseHeight = data.continentValue;
        
        // 应用侵蚀效果（在陆地上）
        if (baseHeight > -0.19) {
            double erosionEffect = (data.erosionValue - 0.5) * 0.2;
            baseHeight += erosionEffect;
        }
        
        // 应用山峰和山谷效果（在高地上）
        if (baseHeight > 0.2) {
            double pvEffect = data.peaksValleysValue * 0.3 * (baseHeight - 0.2);
            baseHeight += pvEffect;
        }
        
        data.finalValue = baseHeight;
        data.height = (int) heightSpline.eval(baseHeight);
        data.terrainType = TerrainType.fromValue(baseHeight);
        
        return data;
    }

    /**
     * 地形数据
     */
    public static class TerrainData {
        public double continentValue;
        public double erosionValue;
        public double peaksValleysValue;
        public double temperatureValue;
        public double moistureValue;
        public double finalValue;
        public int height;
        public TerrainType terrainType;
    }
}
