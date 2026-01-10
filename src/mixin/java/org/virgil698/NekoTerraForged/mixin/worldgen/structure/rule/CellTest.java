package org.virgil698.NekoTerraForged.mixin.worldgen.structure.rule;

import java.util.Set;

import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Cell 测试规则
 * 基于 Cell 数据判断结构是否可以生成
 * 移植自 ReTerraForged
 */
public record CellTest(float cutoff, Set<Terrain> terrainTypeBlacklist) implements StructureRule {

    @Override
    public boolean test(RandomState randomState, BlockPos pos) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            // 检查河流距离
            float riverDistance = bridge.getCellField(pos.getX(), pos.getZ(), "RIVER_DISTANCE");
            if (riverDistance < this.cutoff) {
                return false;
            }
            // TODO: 检查地形类型黑名单
        }
        return true;
    }
}
