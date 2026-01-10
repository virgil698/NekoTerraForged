package org.virgil698.NekoTerraForged.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.virgil698.NekoTerraForged.NekoTerraForgedPlugin;
import org.virgil698.NekoTerraForged.bridge.PluginBridge;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

/**
 * RTF 命令注册
 * 使用 Paper Brigadier Command API
 */
@SuppressWarnings("UnstableApiUsage")
public class RTFCommands {
    
    private final NekoTerraForgedPlugin plugin;
    
    public RTFCommands(NekoTerraForgedPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 获取插件侧 Bridge
     */
    private PluginBridge getBridge() {
        return plugin.getBridge();
    }
    
    /**
     * 注册所有命令
     */
    public void register() {
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            
            // 主命令 /rtf
            LiteralArgumentBuilder<CommandSourceStack> rtfCommand = literal("rtf")
                .requires(source -> source.getSender().hasPermission("nekoterraforged.admin"))
                // /rtf reload - 重载配置
                .then(literal("reload")
                    .executes(this::reloadConfig))
                // /rtf debug [x] [z] - 获取调试信息
                .then(literal("debug")
                    .executes(this::debugAtPlayer)
                    .then(argument("x", IntegerArgumentType.integer())
                        .then(argument("z", IntegerArgumentType.integer())
                            .executes(this::debugAtCoords))))
                // /rtf terrain [x] [z] - 获取地形类型
                .then(literal("terrain")
                    .executes(this::terrainAtPlayer)
                    .then(argument("x", IntegerArgumentType.integer())
                        .then(argument("z", IntegerArgumentType.integer())
                            .executes(this::terrainAtCoords))))
                // /rtf locate <terrain> - 定位地形
                .then(literal("locate")
                    .then(argument("terrain", StringArgumentType.word())
                        .suggests(this::suggestTerrainTypes)
                        .executes(this::locateTerrain)))
                // /rtf export <radius> [filename] - 导出高度图
                .then(literal("export")
                    .then(argument("radius", IntegerArgumentType.integer(1, 64))
                        .executes(this::exportHeightmap)
                        .then(argument("filename", StringArgumentType.word())
                            .executes(this::exportHeightmapWithName))))
                // /rtf config <key> [value] - 查看/设置配置
                .then(literal("config")
                    .then(literal("list")
                        .executes(this::listConfig))
                    .then(argument("key", StringArgumentType.word())
                        .executes(this::getConfigValue)
                        .then(argument("value", StringArgumentType.greedyString())
                            .executes(this::setConfigValue))))
                // /rtf status - 查看状态
                .then(literal("status")
                    .executes(this::showStatus))
                // /rtf preset list - 列出预设
                .then(literal("preset")
                    .then(literal("list")
                        .executes(this::listPresets))
                    .then(literal("load")
                        .then(argument("name", StringArgumentType.word())
                            .suggests(this::suggestPresets)
                            .executes(this::loadPreset))));
            
            commands.register(rtfCommand.build(), "NekoTerraForged main command", java.util.List.of("nekoterraforged", "ntf"));
        });
    }
    
    // ==================== 命令处理方法 ====================
    
    private int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            bridge.reloadConfig();
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Configuration reloaded!", NamedTextColor.GREEN)
            );
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
        }
        return Command.SINGLE_SUCCESS;
    }
    
    private int debugAtPlayer(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getExecutor() instanceof Player player) {
            Location loc = player.getLocation();
            return showDebugInfo(ctx, loc.getBlockX(), loc.getBlockZ());
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] This command must be run by a player or specify coordinates!", NamedTextColor.RED)
            );
            return 0;
        }
    }
    
    private int debugAtCoords(CommandContext<CommandSourceStack> ctx) {
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        return showDebugInfo(ctx, x, z);
    }
    
    private int showDebugInfo(CommandContext<CommandSourceStack> ctx, int x, int z) {
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            String info = bridge.getDebugInfo(x, z);
            for (String line : info.split("\n")) {
                ctx.getSource().getSender().sendMessage(Component.text(line, NamedTextColor.AQUA));
            }
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
        }
        return Command.SINGLE_SUCCESS;
    }
    
    private int terrainAtPlayer(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getExecutor() instanceof Player player) {
            Location loc = player.getLocation();
            return showTerrainType(ctx, loc.getBlockX(), loc.getBlockZ());
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] This command must be run by a player or specify coordinates!", NamedTextColor.RED)
            );
            return 0;
        }
    }
    
    private int terrainAtCoords(CommandContext<CommandSourceStack> ctx) {
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        return showTerrainType(ctx, x, z);
    }
    
    private int showTerrainType(CommandContext<CommandSourceStack> ctx, int x, int z) {
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            String terrain = bridge.getTerrainType(x, z);
            if (terrain != null) {
                ctx.getSource().getSender().sendMessage(
                    Component.text("[RTF] Terrain at (" + x + ", " + z + "): ", NamedTextColor.GOLD)
                        .append(Component.text(terrain, NamedTextColor.GREEN))
                );
            } else {
                ctx.getSource().getSender().sendMessage(
                    Component.text("[RTF] No terrain data at (" + x + ", " + z + ")", NamedTextColor.YELLOW)
                );
            }
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
        }
        return Command.SINGLE_SUCCESS;
    }
    
    private int exportHeightmap(CommandContext<CommandSourceStack> ctx) {
        int radius = IntegerArgumentType.getInteger(ctx, "radius");
        String filename = "heightmap_" + System.currentTimeMillis() + ".png";
        return doExportHeightmap(ctx, radius, filename);
    }
    
    private int exportHeightmapWithName(CommandContext<CommandSourceStack> ctx) {
        int radius = IntegerArgumentType.getInteger(ctx, "radius");
        String filename = StringArgumentType.getString(ctx, "filename");
        if (!filename.endsWith(".png")) {
            filename += ".png";
        }
        return doExportHeightmap(ctx, radius, filename);
    }
    
    private int doExportHeightmap(CommandContext<CommandSourceStack> ctx, int radius, String filename) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] This command must be run by a player!", NamedTextColor.RED)
            );
            return 0;
        }
        
        PluginBridge bridge = getBridge();
        if (bridge == null) {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
            return 0;
        }
        
        Location loc = player.getLocation();
        String outputPath = new File(plugin.getDataFolder(), "exports/" + filename).getAbsolutePath();
        
        ctx.getSource().getSender().sendMessage(
            Component.text("[RTF] Exporting heightmap... This may take a moment.", NamedTextColor.YELLOW)
        );
        
        // 异步执行导出
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = bridge.exportHeightmap(loc.getBlockX(), loc.getBlockZ(), radius, outputPath);
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.sendMessage(
                        Component.text("[RTF] Heightmap exported to: " + outputPath, NamedTextColor.GREEN)
                    );
                } else {
                    player.sendMessage(
                        Component.text("[RTF] Failed to export heightmap!", NamedTextColor.RED)
                    );
                }
            });
        });
        
        return Command.SINGLE_SUCCESS;
    }
    
    private int listConfig(CommandContext<CommandSourceStack> ctx) {
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            Map<String, Object> config = bridge.getAllConfig();
            ctx.getSource().getSender().sendMessage(
                Component.text("=== RTF Configuration ===", NamedTextColor.GOLD)
            );
            config.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    ctx.getSource().getSender().sendMessage(
                        Component.text(entry.getKey() + ": ", NamedTextColor.AQUA)
                            .append(Component.text(String.valueOf(entry.getValue()), NamedTextColor.WHITE))
                    );
                });
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
        }
        return Command.SINGLE_SUCCESS;
    }
    
    private int getConfigValue(CommandContext<CommandSourceStack> ctx) {
        String key = StringArgumentType.getString(ctx, "key");
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            Object value = bridge.getConfig(key, null);
            if (value != null) {
                ctx.getSource().getSender().sendMessage(
                    Component.text("[RTF] " + key + " = ", NamedTextColor.AQUA)
                        .append(Component.text(String.valueOf(value), NamedTextColor.WHITE))
                );
            } else {
                ctx.getSource().getSender().sendMessage(
                    Component.text("[RTF] Config key not found: " + key, NamedTextColor.YELLOW)
                );
            }
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
        }
        return Command.SINGLE_SUCCESS;
    }
    
    private int setConfigValue(CommandContext<CommandSourceStack> ctx) {
        String key = StringArgumentType.getString(ctx, "key");
        String valueStr = StringArgumentType.getString(ctx, "value");
        
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            // 尝试解析值类型
            Object value;
            if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
                value = Boolean.parseBoolean(valueStr);
            } else {
                try {
                    if (valueStr.contains(".")) {
                        value = Double.parseDouble(valueStr);
                    } else {
                        value = Integer.parseInt(valueStr);
                    }
                } catch (NumberFormatException e) {
                    value = valueStr;
                }
            }
            
            bridge.setConfig(key, value);
            bridge.saveConfig();
            
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Set " + key + " = " + value, NamedTextColor.GREEN)
            );
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
        }
        return Command.SINGLE_SUCCESS;
    }
    
    private int showStatus(CommandContext<CommandSourceStack> ctx) {
        PluginBridge bridge = getBridge();
        
        ctx.getSource().getSender().sendMessage(
            Component.text("=== NekoTerraForged Status ===", NamedTextColor.GOLD)
        );
        
        if (bridge != null) {
            boolean initialized = bridge.isInitialized();
            ctx.getSource().getSender().sendMessage(
                Component.text("Bridge: ", NamedTextColor.AQUA)
                    .append(Component.text("Connected", NamedTextColor.GREEN))
            );
            ctx.getSource().getSender().sendMessage(
                Component.text("Initialized: ", NamedTextColor.AQUA)
                    .append(Component.text(String.valueOf(initialized), 
                        initialized ? NamedTextColor.GREEN : NamedTextColor.RED))
            );
            ctx.getSource().getSender().sendMessage(
                Component.text("Seed: ", NamedTextColor.AQUA)
                    .append(Component.text(String.valueOf(bridge.getSeed()), NamedTextColor.WHITE))
            );
            ctx.getSource().getSender().sendMessage(
                Component.text("Cull Noise Sections: ", NamedTextColor.AQUA)
                    .append(Component.text(String.valueOf(bridge.isCullNoiseSections()), NamedTextColor.WHITE))
            );
            ctx.getSource().getSender().sendMessage(
                Component.text("Fast Lookups: ", NamedTextColor.AQUA)
                    .append(Component.text(String.valueOf(bridge.isFastLookups()), NamedTextColor.WHITE))
            );
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("Bridge: ", NamedTextColor.AQUA)
                    .append(Component.text("Not Connected", NamedTextColor.RED))
            );
        }
        
        return Command.SINGLE_SUCCESS;
    }

    // ==================== Locate Terrain 命令 ====================

    private CompletableFuture<Suggestions> suggestTerrainTypes(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            List<String> terrainTypes = bridge.getTerrainTypes();
            String remaining = builder.getRemaining().toLowerCase();
            terrainTypes.stream()
                .filter(name -> name.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestPresets(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            List<String> presets = bridge.getPresetNames();
            String remaining = builder.getRemaining().toLowerCase();
            presets.stream()
                .filter(name -> name.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private int locateTerrain(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] This command must be run by a player!", NamedTextColor.RED)
            );
            return 0;
        }

        String terrainName = StringArgumentType.getString(ctx, "terrain");
        PluginBridge bridge = getBridge();
        
        if (bridge == null) {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
            return 0;
        }

        // 验证地形类型是否存在
        List<String> validTypes = bridge.getTerrainTypes();
        if (!validTypes.contains(terrainName)) {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Unknown terrain type: " + terrainName, NamedTextColor.RED)
            );
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Valid types: " + String.join(", ", validTypes), NamedTextColor.YELLOW)
            );
            return 0;
        }

        Location origin = player.getLocation();
        
        ctx.getSource().getSender().sendMessage(
            Component.text("[RTF] Searching for terrain: " + terrainName + "...", NamedTextColor.YELLOW)
        );

        // 异步搜索
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int[] result = bridge.locateTerrain(
                origin.getBlockX(), 
                origin.getBlockZ(), 
                terrainName,
                256,    // step
                256,    // minRadius
                24000,  // maxRadius
                30      // timeout seconds
            );

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (result != null) {
                    int x = result[0];
                    int z = result[1];
                    int distance = (int) Math.sqrt(
                        Math.pow(x - origin.getBlockX(), 2) + 
                        Math.pow(z - origin.getBlockZ(), 2)
                    );

                    Component teleportLink = Component.text("[" + x + ", ~, " + z + "]", NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.suggestCommand("/tp @s " + x + " ~ " + z))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to teleport")));

                    player.sendMessage(
                        Component.text("[RTF] Found " + terrainName + " at ", NamedTextColor.GOLD)
                            .append(teleportLink)
                            .append(Component.text(" (" + distance + " blocks away)", NamedTextColor.GRAY))
                    );
                } else {
                    player.sendMessage(
                        Component.text("[RTF] Could not find terrain: " + terrainName + " within search radius", NamedTextColor.RED)
                    );
                }
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    // ==================== 预设命令 ====================

    private int listPresets(CommandContext<CommandSourceStack> ctx) {
        PluginBridge bridge = getBridge();
        if (bridge != null) {
            List<String> presets = bridge.getPresetNames();
            ctx.getSource().getSender().sendMessage(
                Component.text("=== Available Presets ===", NamedTextColor.GOLD)
            );
            for (String preset : presets) {
                ctx.getSource().getSender().sendMessage(
                    Component.text("  - " + preset, NamedTextColor.AQUA)
                );
            }
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private int loadPreset(CommandContext<CommandSourceStack> ctx) {
        String presetName = StringArgumentType.getString(ctx, "name");
        PluginBridge bridge = getBridge();
        
        if (bridge == null) {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Bridge not initialized!", NamedTextColor.RED)
            );
            return 0;
        }

        boolean success = bridge.loadPreset(presetName);
        if (success) {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Loaded preset: " + presetName, NamedTextColor.GREEN)
            );
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Note: Changes will apply to newly generated chunks", NamedTextColor.YELLOW)
            );
        } else {
            ctx.getSource().getSender().sendMessage(
                Component.text("[RTF] Failed to load preset: " + presetName, NamedTextColor.RED)
            );
        }
        return Command.SINGLE_SUCCESS;
    }
}
