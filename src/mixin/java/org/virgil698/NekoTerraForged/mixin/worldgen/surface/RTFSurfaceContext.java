package org.virgil698.NekoTerraForged.mixin.worldgen.surface;

import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * RTF 表面上下文接口
 * 用于获取周围生物群系信息
 * 移植自 ReTerraForged RTFSurfaceContext
 */
public interface RTFSurfaceContext {
    
    /**
     * 获取周围的生物群系
     * @return 周围生物群系的资源键集合，可能为 null
     */
    Set<ResourceKey<Biome>> getSurroundingBiomes();
}
