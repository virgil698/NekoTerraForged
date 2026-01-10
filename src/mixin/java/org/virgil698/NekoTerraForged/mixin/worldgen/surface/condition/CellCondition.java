package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import org.jetbrains.annotations.Nullable;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.LazyXZCondition;

/**
 * Cell 条件基类
 * 用于基于 RTF Cell 数据的表面规则条件
 * 移植自 ReTerraForged
 */
public abstract class CellCondition extends LazyXZCondition {
    @Nullable
    private Tile.Chunk chunk;
    private long lastXZ;
    private boolean lastResult;

    @Nullable
    protected GeneratorContext generatorContext;

    public CellCondition(SurfaceRules.Context context) {
        super(context);
        
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            Object ctx = bridge.getGeneratorContext();
            if (ctx instanceof GeneratorContext gc) {
                this.generatorContext = gc;
                ChunkPos chunkPos = context.chunk.getPos();
                this.chunk = gc.getCache().provideAtChunk(chunkPos.x, chunkPos.z)
                        .getChunkReader(chunkPos.x, chunkPos.z);
            }
        }
        this.lastXZ = Long.MIN_VALUE;
    }

    public abstract boolean test(Cell cell, int x, int z);

    @Override
    public boolean compute() {
        int x = this.context.blockX;
        int z = this.context.blockZ;
        long packedPos = PosUtil.pack(x, z);
        if (this.lastXZ != packedPos && this.generatorContext != null && this.chunk != null) {
            this.lastXZ = packedPos;
            Cell cell = this.chunk.getCell(x, z);
            this.lastResult = this.test(cell, x, z);
        }
        return this.lastResult;
    }
}
