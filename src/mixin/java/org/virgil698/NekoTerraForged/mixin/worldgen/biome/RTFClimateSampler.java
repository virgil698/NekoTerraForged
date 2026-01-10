package org.virgil698.NekoTerraForged.mixin.worldgen.biome;

import net.minecraft.core.BlockPos;

/**
 * RTF 气候采样器接口
 * 用于设置和获取出生点搜索中心
 * 移植自 ReTerraForged
 */
public interface RTFClimateSampler {
    void setSpawnSearchCenter(BlockPos center);

    BlockPos getSpawnSearchCenter();
}
