package org.virgil698.NekoTerraForged.mixin.bridge;

import org.jetbrains.annotations.Nullable;

/**
 * Bridge 管理器，用于管理 Mixin 和插件之间的通信
 */
public class RTFBridgeManager {
    public static final RTFBridgeManager INSTANCE = new RTFBridgeManager();

    @Nullable
    private RTFBridge bridge;

    private RTFBridgeManager() {}

    /**
     * 初始化并注册 Bridge 实现
     * 由插件调用
     */
    public void initialize() {
        if (this.bridge == null) {
            this.bridge = new RTFBridgeImpl();
        }
    }

    /**
     * 清理 Bridge
     * 由插件调用
     */
    public void shutdown() {
        this.bridge = null;
    }

    public void setBridge(@Nullable RTFBridge bridge) {
        this.bridge = bridge;
    }

    @Nullable
    public RTFBridge getBridge() {
        return bridge;
    }

    public boolean hasBridge() {
        return bridge != null;
    }
}
