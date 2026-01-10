package org.virgil698.NekoTerraForged.mixin.worldgen.continent.simple;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil.Vec2i;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Seed;

/**
 * 单大陆生成器
 * 移植自 ReTerraForged
 */
public class SingleContinentGenerator extends ContinentGenerator {
    private Vec2i center;

    public SingleContinentGenerator(Seed seed, GeneratorContext context) {
        super(seed, context);
        long center = this.getNearestCenter(0.0F, 0.0F);
        int cx = PosUtil.unpackLeft(center);
        int cz = PosUtil.unpackRight(center);
        this.center = new Vec2i(cx, cz);
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        super.apply(cell, x, y);
        if (cell.continentX != this.center.x() || cell.continentZ != this.center.y()) {
            cell.continentId = 0.0F;
            cell.continentEdge = 0.0F;
            cell.continentX = 0;
            cell.continentZ = 0;
        }
    }
}
