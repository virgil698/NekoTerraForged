package org.virgil698.NekoTerraForged.mixin.worldgen.cell;

/**
 * Cell 查找接口
 * 移植自 ReTerraForged
 */
public interface CellLookup {
    /**
     * 查找指定坐标的 Cell
     * @param blockX 方块 X 坐标
     * @param blockZ 方块 Z 坐标
     * @return Cell 实例
     */
    Cell lookup(int blockX, int blockZ);
}
