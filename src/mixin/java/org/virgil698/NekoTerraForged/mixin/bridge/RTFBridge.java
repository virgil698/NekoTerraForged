package org.virgil698.NekoTerraForged.mixin.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;

/**
 * RTF Bridge 接口
 * 用于在插件层和 Mixin 层之间传递数据
 */
public interface RTFBridge {
    
    /**
     * 初始化生成器上下文
     */
    void initializeContext(RegistryAccess registryAccess, long seed);
    
    /**
     * 获取世界种子
     */
    long getSeed();
    
    /**
     * 检查是否已初始化
     */
    boolean isInitialized();
    
    /**
     * 设置出生点搜索中心
     */
    void setSpawnSearchCenter(BlockPos center);
    
    /**
     * 获取出生点搜索中心
     */
    BlockPos getSpawnSearchCenter();
    
    /**
     * 设置配置（从插件层传递）
     * @param configData 配置数据对象
     */
    void setConfigData(Object configData);
    
    /**
     * 获取配置数据
     */
    Object getConfigData();
}
