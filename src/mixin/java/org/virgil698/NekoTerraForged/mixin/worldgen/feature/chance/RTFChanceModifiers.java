package org.virgil698.NekoTerraForged.mixin.worldgen.feature.chance;

/**
 * RTF概率修改器工厂
 * 移植自 ReTerraForged
 */
public class RTFChanceModifiers {

    public static void bootstrap() {
        // 注册概率修改器（简化版本，不使用注册表）
    }
    
    public static ElevationChanceModifier elevation(float from, float to) {
        return elevation(from, to, false);
    }
    
    public static ElevationChanceModifier elevation(float from, float to, boolean exclusive) {
        return new ElevationChanceModifier(from, to, exclusive);
    }
    
    public static BiomeEdgeChanceModifier biomeEdge(float from, float to) {
        return biomeEdge(from, to, false);
    }
    
    public static BiomeEdgeChanceModifier biomeEdge(float from, float to, boolean exclusive) {
        return new BiomeEdgeChanceModifier(from, to, exclusive);
    }
}
