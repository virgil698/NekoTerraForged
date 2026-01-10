package org.virgil698.NekoTerraForged.mixin.worldgen.feature.chance;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import org.virgil698.NekoTerraForged.mixin.registries.RTFBuiltInRegistries;

/**
 * RTF 概率修改器注册与工厂
 * 移植自 ReTerraForged
 */
public class RTFChanceModifiers {
    public static final String MOD_ID = "nekoterraforged";
    
    private static boolean registered = false;

    /**
     * 初始化并注册所有概率修改器类型
     * 应在插件启动时调用
     */
    public static void bootstrap() {
        if (registered) {
            return;
        }
        registered = true;
        
        register("elevation", ElevationChanceModifier.CODEC);
        register("biome_edge", BiomeEdgeChanceModifier.CODEC);
    }
    
    /**
     * 创建高度概率修改器
     * @param from 起始高度比例
     * @param to 结束高度比例
     */
    public static ElevationChanceModifier elevation(float from, float to) {
        return elevation(from, to, false);
    }
    
    /**
     * 创建高度概率修改器
     * @param from 起始高度比例
     * @param to 结束高度比例
     * @param exclusive 是否排除范围内
     */
    public static ElevationChanceModifier elevation(float from, float to, boolean exclusive) {
        return new ElevationChanceModifier(from, to, exclusive);
    }
    
    /**
     * 创建生物群系边缘概率修改器
     * @param from 起始边缘距离
     * @param to 结束边缘距离
     */
    public static BiomeEdgeChanceModifier biomeEdge(float from, float to) {
        return biomeEdge(from, to, false);
    }
    
    /**
     * 创建生物群系边缘概率修改器
     * @param from 起始边缘距离
     * @param to 结束边缘距离
     * @param exclusive 是否排除范围内
     */
    public static BiomeEdgeChanceModifier biomeEdge(float from, float to, boolean exclusive) {
        return new BiomeEdgeChanceModifier(from, to, exclusive);
    }
    
    /**
     * 注册概率修改器类型
     */
    private static void register(String name, MapCodec<? extends ChanceModifier> codec) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        Registry.register(RTFBuiltInRegistries.CHANCE_MODIFIER_TYPE, id, codec);
    }
    
    /**
     * 获取资源位置
     */
    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
