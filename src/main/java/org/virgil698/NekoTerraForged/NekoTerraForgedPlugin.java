package org.virgil698.NekoTerraForged;

import org.bukkit.plugin.java.JavaPlugin;
import org.virgil698.NekoTerraForged.bridge.PluginBridge;
import org.virgil698.NekoTerraForged.bridge.PluginBridgeManager;
import org.virgil698.NekoTerraForged.command.RTFCommands;

public final class NekoTerraForgedPlugin extends JavaPlugin {
    private static NekoTerraForgedPlugin instance;
    private RTFCommands commands;
    private PluginBridge bridge;

    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化插件侧 Bridge（会自动初始化 Mixin 侧的 Bridge）
        bridge = PluginBridgeManager.INSTANCE;
        bridge.initialize();
        
        // 注册命令
        commands = new RTFCommands(this);
        commands.register();
        
        getLogger().info("NekoTerraForged enabled - RTF terrain generation active");
    }

    @Override
    public void onDisable() {
        // 清理 Bridge
        if (bridge != null) {
            bridge.shutdown();
        }
        
        getLogger().info("NekoTerraForged disabled");
        instance = null;
    }

    public static NekoTerraForgedPlugin getInstance() {
        return instance;
    }
    
    /**
     * 获取插件侧 Bridge
     */
    public PluginBridge getBridge() {
        return bridge;
    }
}
