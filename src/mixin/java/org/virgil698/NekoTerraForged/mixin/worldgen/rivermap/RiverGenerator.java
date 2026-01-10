package org.virgil698.NekoTerraForged.mixin.worldgen.rivermap;

/**
 * 河流生成器接口
 * 移植自 ReTerraForged
 */
public interface RiverGenerator {
    /**
     * 生成河流地图
     * @param x 大陆 X 坐标
     * @param z 大陆 Z 坐标
     * @param id 唯一标识符
     * @return 河流地图
     */
    Rivermap generateRivers(int x, int z, long id);
}
