package org.virgil698.NekoTerraForged;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.virgil698.NekoTerraForged.bridge.PluginBridge;
import org.virgil698.NekoTerraForged.bridge.PluginBridgeManager;
import org.virgil698.NekoTerraForged.command.RTFCommands;
import org.virgil698.NekoTerraForged.config.RTFConfig;

public final class NekoTerraForgedPlugin extends JavaPlugin {
    private static NekoTerraForgedPlugin instance;
    private PluginBridge bridge;
    private RTFConfig rtfConfig;

    @Override
    public void onEnable() {
        instance = this;
        
        // 加载配置文件
        rtfConfig = new RTFConfig(this);
        
        // 检查是否启用
        if (!rtfConfig.isEnabled()) {
            getLogger().warning("NekoTerraForged is disabled in config.yml");
            getLogger().warning("Set 'enabled: true' to enable terrain generation");
            return;
        }
        
        // 初始化插件侧 Bridge（会自动初始化 Mixin 侧的 Bridge）
        bridge = PluginBridgeManager.INSTANCE;
        bridge.initialize();
        
        // 将配置传递给 Bridge
        bridge.setConfig(rtfConfig);
        
        // 使用 Paper 的 Lifecycle API 注册命令
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            RTFCommands commands = new RTFCommands(this);
            event.registrar().register(
                "rtf",
                "NekoTerraForged terrain generator commands",
                commands
            );
        });
        
        getLogger().info("NekoTerraForged enabled - RTF terrain generation active");
        getLogger().info("Continent scale: " + rtfConfig.getContinentConfig().scale);
        getLogger().info("River scale: " + rtfConfig.getRiverConfig().scale);
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
    
    /**
     * 获取配置
     */
    public RTFConfig getRTFConfig() {
        return rtfConfig;
    }
}