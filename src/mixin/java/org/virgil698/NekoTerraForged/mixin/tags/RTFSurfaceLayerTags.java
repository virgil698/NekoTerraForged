package org.virgil698.NekoTerraForged.mixin.tags;

import net.minecraft.tags.TagKey;

import org.virgil698.NekoTerraForged.mixin.registries.RTFRegistryKeys;
import org.virgil698.NekoTerraForged.mixin.worldgen.surface.rule.LayeredSurfaceRule;

/**
 * RTF 表面层标签定义
 * 移植自 ReTerraForged
 */
public class RTFSurfaceLayerTags {
    
    /**
     * TerraBlender 兼容层标签
     */
    public static final TagKey<LayeredSurfaceRule.Layer> TERRABLENDER = resolve("terrablender");
    
    /**
     * 默认层标签
     */
    public static final TagKey<LayeredSurfaceRule.Layer> DEFAULT = resolve("default");
    
    /**
     * 侵蚀层标签
     */
    public static final TagKey<LayeredSurfaceRule.Layer> EROSION = resolve("erosion");
    
    /**
     * 地层层标签
     */
    public static final TagKey<LayeredSurfaceRule.Layer> STRATA = resolve("strata");

    private static TagKey<LayeredSurfaceRule.Layer> resolve(String path) {
        return TagKey.create(RTFRegistryKeys.SURFACE_LAYERS, RTFRegistryKeys.location(path));
    }
}
