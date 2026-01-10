package org.virgil698.NekoTerraForged.mixin.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * RTF 方块标签定义
 * 移植自 ReTerraForged
 */
public class RTFBlockTags {
    private static final String NAMESPACE = "nekoterraforged";

    // 土壤类型
    public static final TagKey<Block> SOIL = resolve("soil");
    
    // 岩石类型
    public static final TagKey<Block> ROCK = resolve("rock");
    
    // 可生成矿石的岩石
    public static final TagKey<Block> ORE_COMPATIBLE_ROCK = resolve("ore_compatible_rock");
    
    // 粘土类型
    public static final TagKey<Block> CLAY = resolve("clay");
    
    // 沉积物类型
    public static final TagKey<Block> SEDIMENT = resolve("sediment");
    
    // 可侵蚀方块
    public static final TagKey<Block> ERODIBLE = resolve("erodible");

    private static TagKey<Block> resolve(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(NAMESPACE, path));
    }
}
