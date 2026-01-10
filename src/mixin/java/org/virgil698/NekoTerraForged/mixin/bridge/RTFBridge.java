package org.virgil698.NekoTerraForged.mixin.bridge;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.Map;

/**
 * Mixin 与插件之间的桥接接口
 * Mixin 通过此接口调用插件的功能
 */
public interface RTFBridge {
    
    // ==================== 配置相关 ====================
    
    /**
     * 获取配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    <T> T getConfig(String key, T defaultValue);
    
    /**
     * 设置配置值
     * @param key 配置键
     * @param value 配置值
     */
    void setConfig(String key, Object value);
    
    /**
     * 重新加载配置
     */
    void reloadConfig();
    
    /**
     * 保存配置
     */
    void saveConfig();
    
    /**
     * 获取所有配置
     * @return 配置映射
     */
    Map<String, Object> getAllConfig();
    
    // ==================== 调试信息 ====================
    
    /**
     * 获取指定坐标的调试信息
     * @param x 世界 X 坐标
     * @param z 世界 Z 坐标
     * @return 调试信息字符串
     */
    String getDebugInfo(int x, int z);
    
    /**
     * 获取地形类型名称
     * @param x 世界 X 坐标
     * @param z 世界 Z 坐标
     * @return 地形类型名称
     */
    @Nullable
    String getTerrainType(int x, int z);
    
    /**
     * 获取生物群系边缘值
     * @param x 世界 X 坐标
     * @param z 世界 Z 坐标
     * @return 边缘值
     */
    float getBiomeEdge(int x, int z);
    
    /**
     * 导出高度图
     * @param centerX 中心 X 坐标
     * @param centerZ 中心 Z 坐标
     * @param radius 半径（区块数）
     * @param outputPath 输出路径
     * @return 是否成功
     */
    boolean exportHeightmap(int centerX, int centerZ, int radius, String outputPath);
    
    // ==================== 地形定位 ====================
    
    /**
     * 获取所有可用的地形类型名称
     * @return 地形类型名称列表
     */
    List<String> getTerrainTypes();
    
    /**
     * 定位指定地形类型
     * @param originX 起始 X 坐标
     * @param originZ 起始 Z 坐标
     * @param terrainName 地形类型名称
     * @param step 搜索步长
     * @param minRadius 最小搜索半径
     * @param maxRadius 最大搜索半径
     * @param timeoutSeconds 超时秒数
     * @return 找到的坐标 [x, z]，未找到返回 null
     */
    @Nullable
    int[] locateTerrain(int originX, int originZ, String terrainName, int step, int minRadius, int maxRadius, int timeoutSeconds);
    
    // ==================== 预设相关 ====================
    
    /**
     * 获取所有可用的预设名称
     * @return 预设名称列表
     */
    List<String> getPresetNames();
    
    /**
     * 加载预设
     * @param presetName 预设名称
     * @return 是否成功
     */
    boolean loadPreset(String presetName);
    
    // ==================== 原有方法 ====================
    /**
     * 初始化生成器上下文
     */
    void initializeContext(RegistryAccess registryAccess, long seed);

    /**
     * 获取生成器上下文（返回 Object 避免 mixin 直接依赖插件类）
     */
    @Nullable
    Object getGeneratorContext();

    /**
     * 设置出生点搜索中心
     */
    void setSpawnSearchCenter(BlockPos center);

    /**
     * 获取出生点搜索中心
     */
    BlockPos getSpawnSearchCenter();

    /**
     * 获取种子
     */
    long getSeed();

    /**
     * 检查是否已初始化
     */
    boolean isInitialized();

    /**
     * 应用单元格数据到指定坐标
     * @param x 世界 X 坐标
     * @param z 世界 Z 坐标
     * @return 单元格数据对象
     */
    @Nullable
    Object applyCell(int x, int z);

    /**
     * 获取指定坐标的高度
     */
    float getHeight(int x, int z);

    /**
     * 获取指定坐标的大陆性
     */
    float getContinentalness(int x, int z);

    /**
     * 获取指定坐标的侵蚀度
     */
    float getErosion(int x, int z);

    /**
     * 获取指定坐标的 Cell 字段值
     * @param x 世界 X 坐标
     * @param z 世界 Z 坐标
     * @param fieldName 字段名称
     * @return 字段值
     */
    float getCellField(int x, int z, String fieldName);

    /**
     * 设置表面生成区域（用于 buildSurface）
     */
    void setSurfaceRegion(@Nullable Object region);

    /**
     * 获取表面生成区域
     */
    @Nullable
    Object getSurfaceRegion();

    /**
     * 从 Marker 创建 CellSampler 密度函数
     * @param marker CellSampler.Marker 实例
     * @return CellSampler 密度函数实例
     */
    @Nullable
    Object createCellSampler(Object marker);

    /**
     * 从 Marker 创建 NoiseSampler 密度函数
     * @param marker NoiseSampler.Marker 实例
     * @param seed 世界种子
     * @return NoiseSampler 密度函数实例
     */
    @Nullable
    Object createNoiseSampler(Object marker, int seed);

    /**
     * 获取指定区块的生成高度
     * @param chunkX 区块 X 坐标
     * @param chunkZ 区块 Z 坐标
     * @param settings 噪声生成器设置
     * @return 生成高度
     */
    int getGenerationHeight(int chunkX, int chunkZ, NoiseGeneratorSettings settings);

    /**
     * 创建 Cache2d 实例
     * @return Cache2d 实例
     */
    @Nullable
    Object createCache2d();

    /**
     * 获取 Tile.Chunk 实例
     * @param chunkX 区块 X 坐标
     * @param chunkZ 区块 Z 坐标
     * @return Tile.Chunk 实例
     */
    @Nullable
    Object getTileChunk(int chunkX, int chunkZ);

    /**
     * 创建带缓存的 CellSampler
     * @param cellSampler 原始 CellSampler
     * @param tileChunk Tile.Chunk 实例
     * @param cache2d Cache2d 实例
     * @param chunkX 区块 X 坐标
     * @param chunkZ 区块 Z 坐标
     * @return 带缓存的 CellSampler
     */
    @Nullable
    Object createCachedCellSampler(Object cellSampler, @Nullable Object tileChunk, 
            @Nullable Object cache2d, int chunkX, int chunkZ);

    /**
     * 获取岩浆层高度
     * @return 岩浆层高度，如果返回 0 或负数则使用默认值
     */
    int getLavaLevel();

    /**
     * 设置是否剔除噪声区段
     * @param cull 是否剔除
     */
    void setCullNoiseSections(boolean cull);

    /**
     * 获取是否剔除噪声区段
     * @return 是否剔除
     */
    boolean isCullNoiseSections();

    /**
     * 设置是否使用快速查找
     * @param fast 是否使用快速查找
     */
    void setFastLookups(boolean fast);

    /**
     * 获取是否使用快速查找
     * @return 是否使用快速查找
     */
    boolean isFastLookups();

    /**
     * 设置是否使用快速 Cell 查找
     * @param fast 是否使用快速查找
     */
    void setFastCellLookups(boolean fast);

    /**
     * 获取是否使用快速 Cell 查找
     * @return 是否使用快速查找
     */
    boolean isFastCellLookups();

    /**
     * 队列化指定区块的 Tile 缓存
     * @param chunkX 区块 X 坐标
     * @param chunkZ 区块 Z 坐标
     */
    void queueTileAtChunk(int chunkX, int chunkZ);

    /**
     * 释放指定区块的 Tile 缓存
     * @param chunkX 区块 X 坐标
     * @param chunkZ 区块 Z 坐标
     */
    void dropTileAtChunk(int chunkX, int chunkZ);
}
