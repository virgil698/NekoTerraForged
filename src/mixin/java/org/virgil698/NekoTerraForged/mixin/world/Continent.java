package org.virgil698.NekoTerraForged.mixin.world;

import org.virgil698.NekoTerraForged.mixin.math.Node;
import org.virgil698.NekoTerraForged.mixin.math.Spline;

/**
 * 大陆生成器
 * 移植自 Valley
 */
public class Continent {
    
    public static class Config {
        private final Node node;

        public Config(Node node) {
            this.node = node;
        }

        public Node node() {
            return node;
        }
    }

    /**
     * 创建默认的大陆配置
     */
    public static Config CreateDefault() {
        // 基础噪声：Simplex + FBM + Scale
        Node base = Node.Fbm(Node.Scale(Node.Simplex(), 1600), 2, 1.5, 0.25);
        
        // 扭曲噪声
        Node warp = Node.Fbm(Node.Simplex(), 3, 2.5, 0.4);
        
        // 应用域扭曲
        Node result = Node.Warp(base, warp, 0.00125, 175.0);
        
        // 使用样条重映射值
        double[] inputs = {-1.0, -0.5, -0.3, 0.0, 1.0};
        double[] outputs = {-1.2, -0.3, -0.19, 0.0, 1.0};
        
        return new Config(Node.Remap(result, new Spline(inputs, outputs)));
    }
}
