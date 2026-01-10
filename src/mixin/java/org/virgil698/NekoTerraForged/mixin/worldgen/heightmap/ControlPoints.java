package org.virgil698.NekoTerraForged.mixin.worldgen.heightmap;

/**
 * 控制点配置 - 定义大陆各区域的边界值
 * 移植自 ReTerraForged
 */
public record ControlPoints(
    float deepOcean,
    float shallowOcean,
    float beach,
    float coast,
    float nearInland,
    float midInland,
    float farInland
) {
    /**
     * 默认控制点
     */
    public static final ControlPoints DEFAULT = new ControlPoints(
        0.1F,   // deepOcean
        0.25F,  // shallowOcean
        0.35F,  // beach
        0.38F,  // coast
        0.45F,  // nearInland
        0.6F,   // midInland
        0.8F    // farInland
    );
}
