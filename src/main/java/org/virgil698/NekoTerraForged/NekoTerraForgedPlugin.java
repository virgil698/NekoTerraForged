package org.virgil698.NekoTerraForged;

import org.bukkit.plugin.java.JavaPlugin;
import org.virgil698.NekoTerraForged.bridge.PluginBridge;
import org.virgil698.NekoTerraForged.bridge.PluginBridgeManager;
import org.virgil698.NekoTerraForged.command.RTFCommands;

import java.io.File;

public final class NekoTerraForgedPlugin extends JavaPlugin {
    private static NekoTerraForgedPlugin instance;
    private RTFCommands commands;
    private PluginBridge bridge;

    @Override
    public void onEnable() {
        instance = this;
        
        // 先确保配置文件存在（使用 Bukkit API 从 jar 中提取）
        ensureConfigExists();
        
        // 初始化插件侧 Bridge（会自动初始化 Mixin 侧的 Bridge）
        bridge = PluginBridgeManager.INSTANCE;
        bridge.initialize();
        
        // 注册命令
        commands = new RTFCommands(this);
        commands.register();
        
        getLogger().info("NekoTerraForged enabled - RTF terrain generation active");
    }
    
    /**
     * 确保配置文件存在
     * 使用 Bukkit 的 saveResource API，可以正确从 jar 中提取资源
     */
    private void ensureConfigExists() {
        File configFile = new File(getDataFolder(), "config.json");
        if (!configFile.exists()) {
            // 确保插件目录存在
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            // 从 jar 中提取 config/default-config.json 到 plugins/NekoTerraForged/config.json
            saveResource("config/default-config.json", false);
            // 重命名文件
            File defaultConfig = new File(getDataFolder(), "config/default-config.json");
            if (defaultConfig.exists()) {
                defaultConfig.renameTo(configFile);
                // 删除空的 config 目录
                new File(getDataFolder(), "config").delete();
                getLogger().info("Created default config at: " + configFile.getAbsolutePath());
            }
        }
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
