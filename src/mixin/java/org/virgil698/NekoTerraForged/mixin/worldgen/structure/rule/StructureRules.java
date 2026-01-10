package org.virgil698.NekoTerraForged.mixin.worldgen.structure.rule;

import com.google.common.collect.ImmutableSet;

import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;

/**
 * 结构规则工厂类
 * 移植自 ReTerraForged
 */
public class StructureRules {

    public static void bootstrap() {
        // 在 Leaves 插件环境中，注册通过其他方式完成
    }

    public static CellTest cellTest(float cutoff, Terrain... terrainTypeBlacklist) {
        return new CellTest(cutoff, ImmutableSet.copyOf(terrainTypeBlacklist));
    }
}
