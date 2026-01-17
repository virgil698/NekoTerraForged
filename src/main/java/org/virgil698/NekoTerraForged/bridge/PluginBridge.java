package org.virgil698.NekoTerraForged.bridge;

import net.minecraft.core.BlockPos;
import org.virgil698.NekoTerraForged.config.RTFConfig;

/**
 * 插件侧 Bridge 接口
 * 只包含插件层需要的方法
 */
public interface PluginBridge {
    
    /**
     * 初始化 Bridge
     */
    void initialize();
    
    /**
     * 关闭 Bridge
     */
    void shutdown();
    
    /**
     * 设置配置
     */
    void setConfig(RTFConfig config);
    
    /**
     * 获取配置
     */
    RTFConfig getConfig();
    
    /**
     * 获取世界种子
     */
    long getSeed();
    
    /**
     * 检查是否已初始化
     */
    boolean isInitialized();
    
    /**
     * 获取出生点搜索中心
     */
    BlockPos getSpawnSearchCenter();
}
