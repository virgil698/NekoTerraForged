package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import net.minecraft.world.level.levelgen.SurfaceRules;

/**
 * 常量条件实现
 * 单独的类文件，避免 Mixin lambda 导致的 NoClassDefFoundError
 */
public final class NTFConstantCondition implements SurfaceRules.Condition {
    public static final NTFConstantCondition TRUE = new NTFConstantCondition(true);
    public static final NTFConstantCondition FALSE = new NTFConstantCondition(false);
    
    private final boolean value;
    
    private NTFConstantCondition(boolean value) {
        this.value = value;
    }
    
    public static NTFConstantCondition of(boolean value) {
        return value ? TRUE : FALSE;
    }
    
    @Override
    public boolean test() {
        return value;
    }
}
