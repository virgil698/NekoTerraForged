package org.virgil698.NekoTerraForged.mixin.worldgen.continent;

/**
 * 简单大陆接口
 * 移植自 ReTerraForged
 */
public interface SimpleContinent extends Continent {
    float getEdgeValue(float x, float z);

    default float getDistanceToEdge(int cx, int cz, float dx, float dy) {
        return 1.0F;
    }

    default float getDistanceToOcean(int cx, int cz, float dx, float dy) {
        return 1.0F;
    }
}
