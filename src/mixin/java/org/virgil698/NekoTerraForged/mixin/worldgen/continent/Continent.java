package org.virgil698.NekoTerraForged.mixin.worldgen.continent;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.Rivermap;

/**
 * 大陆接口
 * 移植自 ReTerraForged
 */
public interface Continent extends CellPopulator {
    
    float getEdgeValue(float x, float z);

    default float getLandValue(float x, float z) {
        return this.getEdgeValue(x, z);
    }

    long getNearestCenter(float x, float z);

    Rivermap getRivermap(int x, int z);

    default Rivermap getRivermap(Cell cell) {
        return this.getRivermap(cell.continentX, cell.continentZ);
    }
}
