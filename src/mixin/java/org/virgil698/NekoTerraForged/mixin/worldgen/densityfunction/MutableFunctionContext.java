package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import net.minecraft.world.level.levelgen.DensityFunction.FunctionContext;

/**
 * 可变的函数上下文
 * 用于在不创建新对象的情况下更新坐标
 * 移植自 ReTerraForged
 */
public class MutableFunctionContext implements FunctionContext {
    private int blockX;
    private int blockY;
    private int blockZ;

    public MutableFunctionContext() {
    }

    public MutableFunctionContext(int x, int y, int z) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
    }

    public MutableFunctionContext at(int x, int y, int z) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
        return this;
    }

    @Override
    public int blockX() {
        return this.blockX;
    }

    @Override
    public int blockY() {
        return this.blockY;
    }

    @Override
    public int blockZ() {
        return this.blockZ;
    }
}
