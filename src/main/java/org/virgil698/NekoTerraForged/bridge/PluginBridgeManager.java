package org.virgil698.NekoTerraForged.bridge;

import org.jetbrains.annotations.Nullable;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import java.util.List;
import java.util.Map;

/**
 * 插件侧 Bridge 管理器
 * 作为插件与 Mixin Bridge 之间的适配层
 */
public class PluginBridgeManager implements PluginBridge {
    
    public static final PluginBridgeManager INSTANCE = new PluginBridgeManager();
    
    private PluginBridgeManager() {}
    
    /**
     * 获取 Mixin 侧的 Bridge
     */
    @Nullable
    private RTFBridge getMixinBridge() {
        return RTFBridgeManager.INSTANCE.getBridge();
    }
    
    // ==================== 生命周期 ====================
    
    @Override
    public void initialize() {
        // 初始化 Mixin 侧的 Bridge
        RTFBridgeManager.INSTANCE.initialize();
    }
    
    @Override
    public void shutdown() {
        // 关闭 Mixin 侧的 Bridge
        RTFBridgeManager.INSTANCE.shutdown();
    }
    
    @Override
    public boolean isInitialized() {
        RTFBridge bridge = getMixinBridge();
        return bridge != null && bridge.isInitialized();
    }
    
    // ==================== 配置相关 ====================
    
    @Override
    public <T> T getConfig(String key, T defaultValue) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getConfig(key, defaultValue);
        }
        return defaultValue;
    }
    
    @Override
    public void setConfig(String key, Object value) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            bridge.setConfig(key, value);
        }
    }
    
    @Override
    public void reloadConfig() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            bridge.reloadConfig();
        }
    }
    
    @Override
    public void saveConfig() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            bridge.saveConfig();
        }
    }
    
    @Override
    public Map<String, Object> getAllConfig() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getAllConfig();
        }
        return Map.of();
    }
    
    // ==================== 调试信息 ====================
    
    @Override
    public String getDebugInfo(int x, int z) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getDebugInfo(x, z);
        }
        return "Bridge not initialized";
    }
    
    @Override
    @Nullable
    public String getTerrainType(int x, int z) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getTerrainType(x, z);
        }
        return null;
    }
    
    @Override
    public float getHeight(int x, int z) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getHeight(x, z);
        }
        return 0.0F;
    }
    
    @Override
    public float getContinentalness(int x, int z) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getContinentalness(x, z);
        }
        return 0.0F;
    }
    
    @Override
    public float getErosion(int x, int z) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getErosion(x, z);
        }
        return 0.0F;
    }
    
    @Override
    public float getBiomeEdge(int x, int z) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getBiomeEdge(x, z);
        }
        return 0.0F;
    }
    
    @Override
    public boolean exportHeightmap(int centerX, int centerZ, int radius, String outputPath) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.exportHeightmap(centerX, centerZ, radius, outputPath);
        }
        return false;
    }
    
    // ==================== 地形定位 ====================
    
    @Override
    public List<String> getTerrainTypes() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getTerrainTypes();
        }
        return List.of();
    }
    
    @Override
    @Nullable
    public int[] locateTerrain(int originX, int originZ, String terrainName, int step, int minRadius, int maxRadius, int timeoutSeconds) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.locateTerrain(originX, originZ, terrainName, step, minRadius, maxRadius, timeoutSeconds);
        }
        return null;
    }
    
    // ==================== 预设相关 ====================
    
    @Override
    public List<String> getPresetNames() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getPresetNames();
        }
        return List.of();
    }
    
    @Override
    public boolean loadPreset(String presetName) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.loadPreset(presetName);
        }
        return false;
    }
    
    // ==================== 世界生成状态 ====================
    
    @Override
    public long getSeed() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.getSeed();
        }
        return 0L;
    }
    
    @Override
    public boolean isCullNoiseSections() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.isCullNoiseSections();
        }
        return true;
    }
    
    @Override
    public void setCullNoiseSections(boolean cull) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            bridge.setCullNoiseSections(cull);
        }
    }
    
    @Override
    public boolean isFastLookups() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.isFastLookups();
        }
        return true;
    }
    
    @Override
    public void setFastLookups(boolean fast) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            bridge.setFastLookups(fast);
        }
    }
    
    @Override
    public boolean isFastCellLookups() {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            return bridge.isFastCellLookups();
        }
        return true;
    }
    
    @Override
    public void setFastCellLookups(boolean fast) {
        RTFBridge bridge = getMixinBridge();
        if (bridge != null) {
            bridge.setFastCellLookups(fast);
        }
    }
}
