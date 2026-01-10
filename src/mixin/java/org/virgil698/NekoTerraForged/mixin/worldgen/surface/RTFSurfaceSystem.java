package org.virgil698.NekoTerraForged.mixin.worldgen.surface;

import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/**
 * RTF 表面系统接口
 * 移植自 ReTerraForged
 */
public interface RTFSurfaceSystem {
    /**
     * 获取或创建地层数据
     * @param name 缓存 ID
     * @param factory 工厂函数
     * @return 地层列表
     */
    List<Strata> getOrCreateStrata(ResourceLocation name, Function<RandomSource, List<Strata>> factory);

    /**
     * 地层数据
     */
    record Strata(List<Stratum> stratum) {
    }

    /**
     * 单层地层
     */
    record Stratum(net.minecraft.world.level.block.state.BlockState state, float depth) {
    }
}
