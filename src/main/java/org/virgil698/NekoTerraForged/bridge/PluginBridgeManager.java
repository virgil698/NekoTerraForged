package org.virgil698.NekoTerraForged.bridge;

import org.virgil698.NekoTerraForged.config.RTFConfig;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.core.BlockPos;

/**
 * 插件侧的 Bridge 管理器
 * 提供对 Mixin 侧 Bridge 的访问
 */
public class PluginBridgeManager implements PluginBridge {
    public static final PluginBridgeManager INSTANCE = new PluginBridgeManager();
    
    private RTFConfig config;
    
    private PluginBridgeManager() {}
    
    private RTFBridge getBridge() {
        return RTFBridgeManager.INSTANCE.getBridge();
    }
    
    @Override
    public void initialize() {
        // Bridge 会在 MixinRandomState 中自动初始化
        System.out.println("[NekoTerraForged] Plugin bridge initialized");
    }
    
    @Override
    public void shutdown() {
        System.out.println("[NekoTerraForged] Plugin bridge shutdown");
    }
    
    @Override
    public void setConfig(RTFConfig config) {
        this.config = config;
        
        // 将配置传递给 Mixin 层
        RTFBridge bridge = getBridge();
        if (bridge != null) {
            bridge.setConfigData(config);
            System.out.println("[NekoTerraForged] Config passed to Mixin layer");
        } else {
            System.out.println("[NekoTerraForged] Config stored, will be passed when bridge initializes");
        }
    }
    
    @Override
    public RTFConfig getConfig() {
        return config;
    }
    
    @Override
    public long getSeed() {
        RTFBridge bridge = getBridge();
        if (bridge != null) {
            return bridge.getSeed();
        }
        return 0;
    }
    
    @Override
    public boolean isInitialized() {
        RTFBridge bridge = getBridge();
        return bridge != null && bridge.isInitialized();
    }
    
    @Override
    public BlockPos getSpawnSearchCenter() {
        RTFBridge bridge = getBridge();
        if (bridge != null) {
            return bridge.getSpawnSearchCenter();
        }
        return BlockPos.ZERO;
    }
}
