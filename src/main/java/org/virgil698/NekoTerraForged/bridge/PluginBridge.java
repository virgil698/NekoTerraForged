package org.virgil698.NekoTerraForged.bridge;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 插件侧的 Bridge 接口
 * 插件通过此接口与 Mixin 侧通信
 * 不依赖任何 MC 类，只使用基本类型和 Java 标准库
 */
public interface PluginBridge {
    
    // ==================== 生命周期 ====================
    
    /**
     * 初始化 Bridge
     */
    void initialize();
    
    /**
     * 关闭 Bridge
     */
    void shutdown();
    
    /**
     * 检查是否已初始化
     */
    boolean isInitialized();
    
    // ==================== 配置相关 ====================
    
    /**
     * 获取配置值
     */
    <T> T getConfig(String key, T defaultValue);
    
    /**
     * 设置配置值
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
     */
    Map<String, Object> getAllConfig();
    
    // ==================== 调试信息 ====================
    
    /**
     * 获取指定坐标的调试信息
     */
    String getDebugInfo(int x, int z);
    
    /**
     * 获取地形类型名称
     */
    @Nullable
    String getTerrainType(int x, int z);
    
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
     * 获取生物群系边缘值
     */
    float getBiomeEdge(int x, int z);
    
    /**
     * 导出高度图
     */
    boolean exportHeightmap(int centerX, int centerZ, int radius, String outputPath);
    
    // ==================== 地形定位 ====================
    
    /**
     * 获取所有可用的地形类型名称
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
     */
    List<String> getPresetNames();
    
    /**
     * 加载预设
     * @param presetName 预设名称
     * @return 是否成功
     */
    boolean loadPreset(String presetName);
    
    // ==================== 世界生成状态 ====================
    
    /**
     * 获取种子
     */
    long getSeed();
    
    /**
     * 获取是否剔除噪声区段
     */
    boolean isCullNoiseSections();
    
    /**
     * 设置是否剔除噪声区段
     */
    void setCullNoiseSections(boolean cull);
    
    /**
     * 获取是否使用快速查找
     */
    boolean isFastLookups();
    
    /**
     * 设置是否使用快速查找
     */
    void setFastLookups(boolean fast);
    
    /**
     * 获取是否使用快速 Cell 查找
     */
    boolean isFastCellLookups();
    
    /**
     * 设置是否使用快速 Cell 查找
     */
    void setFastCellLookups(boolean fast);
}
