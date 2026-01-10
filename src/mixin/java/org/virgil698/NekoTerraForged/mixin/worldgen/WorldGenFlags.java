package org.virgil698.NekoTerraForged.mixin.worldgen;

/**
 * 世界生成标志，用于控制生成行为
 * 移植自 ReTerraForged
 */
public class WorldGenFlags {
    private static final ThreadLocal<Boolean> FAST_LOOKUP = ThreadLocal.withInitial(() -> true);
    private static boolean CULL_NOISE_SECTIONS = true;

    /**
     * 设置是否使用快速 Cell 查找
     * 在结构生成期间应该禁用以确保准确性
     */
    public static void setFastCellLookups(boolean fastLookups) {
        FAST_LOOKUP.set(fastLookups);
    }

    /**
     * 设置是否使用快速查找（别名方法）
     */
    public static void setFastLookups(boolean fastLookups) {
        FAST_LOOKUP.set(fastLookups);
    }

    /**
     * 检查是否使用快速查找
     */
    public static boolean fastLookups() {
        return FAST_LOOKUP.get();
    }

    /**
     * 检查是否使用快速 Cell 查找（别名方法）
     */
    public static boolean fastCellLookups() {
        return FAST_LOOKUP.get();
    }

    /**
     * 设置是否裁剪噪声区段
     */
    public static void setCullNoiseSections(boolean cullNoiseSections) {
        CULL_NOISE_SECTIONS = cullNoiseSections;
    }

    /**
     * 检查是否裁剪噪声区段
     */
    public static boolean cullNoiseSections() {
        return CULL_NOISE_SECTIONS;
    }
}
