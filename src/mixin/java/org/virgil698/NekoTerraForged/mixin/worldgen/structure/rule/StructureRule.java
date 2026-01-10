package org.virgil698.NekoTerraForged.mixin.worldgen.structure.rule;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * 结构规则接口
 * 用于控制结构生成的条件
 * 移植自 ReTerraForged
 */
public interface StructureRule {
    /**
     * 测试指定位置是否满足结构生成条件
     * @param randomState 随机状态
     * @param pos 位置
     * @return 是否满足条件
     */
    boolean test(RandomState randomState, BlockPos pos);
}
