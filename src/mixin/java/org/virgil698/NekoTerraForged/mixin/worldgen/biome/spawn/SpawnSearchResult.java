package org.virgil698.NekoTerraForged.mixin.worldgen.biome.spawn;

import net.minecraft.core.BlockPos;

/**
 * 出生点搜索结果
 * 从 MixinSpawnFinder 中提取出来，避免 Mixin 内部类导致的 NoClassDefFoundError
 */
public record SpawnSearchResult(BlockPos location, long fitness) {
}
