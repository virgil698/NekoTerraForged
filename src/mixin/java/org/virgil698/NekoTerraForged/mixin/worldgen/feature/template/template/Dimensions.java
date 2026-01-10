package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template;

import net.minecraft.core.BlockPos;

/**
 * 模板尺寸
 * 移植自ReTerraForged
 */
public record Dimensions(BlockPos min, BlockPos max) {
    
    public int getSizeX() {
        return this.max.getX() - this.min.getX();
    }

    public int getSizeY() {
        return this.max.getY() - this.min.getY();
    }

    public int getSizeZ() {
        return this.max.getZ() - this.min.getZ();
    }
}
