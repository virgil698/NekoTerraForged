package org.virgil698.NekoTerraForged.mixin.preset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 内置预设定义
 * 提供几种常用的地形生成预设
 */
public class BuiltinPresets {
    
    private static final Map<String, RTFPreset> PRESETS = new HashMap<>();

    static {
        // 默认预设
        PRESETS.put("default", createDefault());
        // 大陆预设 - 更大的大陆
        PRESETS.put("large_continents", createLargeContinents());
        // 群岛预设 - 多个小岛
        PRESETS.put("archipelago", createArchipelago());
        // 高山预设 - 更高的山脉
        PRESETS.put("mountains", createMountains());
        // 平原预设 - 平坦地形
        PRESETS.put("flatlands", createFlatlands());
        // 河流预设 - 更多河流
        PRESETS.put("rivers", createRivers());
    }

    /**
     * 获取预设
     */
    public static RTFPreset get(String name) {
        return PRESETS.getOrDefault(name, PRESETS.get("default"));
    }

    /**
     * 获取所有预设名称
     */
    public static Set<String> getNames() {
        return PRESETS.keySet();
    }

    /**
     * 默认预设
     */
    private static RTFPreset createDefault() {
        return new RTFPreset();
    }

    /**
     * 大陆预设
     */
    private static RTFPreset createLargeContinents() {
        RTFPreset preset = new RTFPreset();
        preset.world.continentScale = 5000;
        preset.world.continentShape = 1;
        preset.world.continentJitter = 0.5f;
        preset.terrain.regionSize = 1500;
        return preset;
    }

    /**
     * 群岛预设
     */
    private static RTFPreset createArchipelago() {
        RTFPreset preset = new RTFPreset();
        preset.world.continentScale = 1500;
        preset.world.continentShape = 0;
        preset.world.continentJitter = 0.9f;
        preset.world.continentSkipping = 0.5f;
        preset.terrain.regionSize = 500;
        return preset;
    }

    /**
     * 高山预设
     */
    private static RTFPreset createMountains() {
        RTFPreset preset = new RTFPreset();
        preset.terrain.mountainHeight = 1.5f;
        preset.terrain.globalVerticalScale = 1.3f;
        preset.terrain.volcanoChance = 0.9f;
        preset.terrain.fancyMountains = true;
        return preset;
    }

    /**
     * 平原预设
     */
    private static RTFPreset createFlatlands() {
        RTFPreset preset = new RTFPreset();
        preset.terrain.mountainHeight = 0.3f;
        preset.terrain.globalVerticalScale = 0.6f;
        preset.terrain.volcanoChance = 0.1f;
        preset.surface.erosionEnabled = false;
        return preset;
    }

    /**
     * 河流预设
     */
    private static RTFPreset createRivers() {
        RTFPreset preset = new RTFPreset();
        preset.rivers.riverCount = 25;
        preset.rivers.mainRiverCount = 3;
        preset.rivers.lakesEnabled = true;
        preset.rivers.lakeChance = 0.5f;
        preset.rivers.wetlandsEnabled = true;
        preset.rivers.wetlandChance = 0.8f;
        return preset;
    }
}
