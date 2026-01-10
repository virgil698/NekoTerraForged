package org.virgil698.NekoTerraForged.mixin.worldgen.continent;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 岛屿填充器
 * 移植自 ReTerraForged
 */
@Deprecated
public record IslandPopulator(CellPopulator ocean, CellPopulator coast, CellPopulator land, float coastPoint, float inlandPoint) implements CellPopulator {
    public static final float DEFAULT_INLAND_POINT = 0.0F;
    public static final float DEFAULT_COAST_POINT = DEFAULT_INLAND_POINT + 0.1F;
    
    @Override
    public void apply(Cell cell, float x, float z) {
        if(cell.continentEdge < this.inlandPoint) {
            this.land.apply(cell, x, z);
            return;
        }
        
        if(cell.continentEdge < this.coastPoint) {
            this.coast.apply(cell, x, z);
            float coastHeight = cell.height;
            
            this.land.apply(cell, x, z);
            float landHeight = cell.height;
            
            cell.height = NoiseUtil.lerp(landHeight, coastHeight, NoiseUtil.map(cell.continentEdge, 0.0F, 1.0F, this.inlandPoint, this.coastPoint));
            return;
        }
        
        this.ocean.apply(cell, x, z);
    }
}
