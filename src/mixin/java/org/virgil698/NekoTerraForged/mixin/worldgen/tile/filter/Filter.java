package org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

/**
 * Tile 过滤器接口
 * 移植自 ReTerraForged
 */
public interface Filter {
    /**
     * 应用过滤器到 Tile
     * @param map Tile 实例
     * @param regionX 区域 X 坐标
     * @param regionZ 区域 Z 坐标
     * @param iterations 迭代次数
     */
    void apply(Tile map, int regionX, int regionZ, int iterations);

    /**
     * 遍历 Tile 中的所有 Cell
     */
    default void iterate(Tile map, Visitor visitor) {
        int size = map.getBlockSize().total();
        for (int dz = 0; dz < size; ++dz) {
            for (int dx = 0; dx < size; ++dx) {
                Cell cell = map.getCellRaw(dx, dz);
                visitor.visit(map, cell, dx, dz);
            }
        }
    }

    /**
     * Cell 访问器接口
     */
    interface Visitor {
        void visit(Tile map, Cell cell, int dx, int dz);
    }
}
