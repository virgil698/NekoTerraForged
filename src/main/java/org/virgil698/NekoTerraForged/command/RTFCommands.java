package org.virgil698.NekoTerraForged.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.virgil698.NekoTerraForged.NekoTerraForgedPlugin;

import java.util.Collection;
import java.util.List;

/**
 * RTF 命令处理器 - 使用 Paper 的 BasicCommand API
 */
public class RTFCommands implements BasicCommand {
    private final NekoTerraForgedPlugin plugin;

    public RTFCommands(NekoTerraForgedPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 0) {
            stack.getSender().sendMessage(
                Component.text("[RTF] ", NamedTextColor.GOLD)
                    .append(Component.text("NekoTerraForged v1.0", NamedTextColor.YELLOW))
            );
            stack.getSender().sendMessage(
                Component.text("Use /rtf help for commands", NamedTextColor.GRAY)
            );
            return;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelp(stack);
                break;
                
            case "info":
                showInfo(stack);
                break;
                
            case "reload":
                handleReload(stack);
                break;
                
            default:
                stack.getSender().sendMessage(
                    Component.text("[RTF] ", NamedTextColor.RED)
                        .append(Component.text("Unknown command. Use /rtf help", NamedTextColor.WHITE))
                );
                break;
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1) {
            return List.of("help", "info", "reload");
        }
        return List.of();
    }

    private void showHelp(CommandSourceStack stack) {
        stack.getSender().sendMessage(Component.text("=== NekoTerraForged Commands ===", NamedTextColor.GOLD));
        stack.getSender().sendMessage(
            Component.text("/rtf info", NamedTextColor.YELLOW)
                .append(Component.text(" - Show generator info", NamedTextColor.GRAY))
        );
        stack.getSender().sendMessage(
            Component.text("/rtf reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload configuration", NamedTextColor.GRAY))
        );
        stack.getSender().sendMessage(
            Component.text("/rtf help", NamedTextColor.YELLOW)
                .append(Component.text(" - Show this help", NamedTextColor.GRAY))
        );
    }

    private void showInfo(CommandSourceStack stack) {
        stack.getSender().sendMessage(
            Component.text("[RTF] ", NamedTextColor.GOLD)
                .append(Component.text("NekoTerraForged - Valley-based terrain generation", NamedTextColor.YELLOW))
        );
        stack.getSender().sendMessage(
            Component.text("Seed: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(plugin.getBridge().getSeed()), NamedTextColor.WHITE))
        );
        
        Component status = plugin.getBridge().isInitialized() 
            ? Component.text("Active", NamedTextColor.GREEN)
            : Component.text("Inactive", NamedTextColor.RED);
        stack.getSender().sendMessage(
            Component.text("Status: ", NamedTextColor.GRAY).append(status)
        );
        
        if (plugin.getRTFConfig() != null) {
            stack.getSender().sendMessage(
                Component.text("Config: ", NamedTextColor.GRAY)
                    .append(Component.text("Loaded", NamedTextColor.GREEN))
            );
            
            Component enabled = plugin.getRTFConfig().isEnabled()
                ? Component.text("Yes", NamedTextColor.GREEN)
                : Component.text("No", NamedTextColor.RED);
            stack.getSender().sendMessage(
                Component.text("Enabled: ", NamedTextColor.GRAY).append(enabled)
            );
            
            stack.getSender().sendMessage(
                Component.text("Continent Scale: ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(plugin.getRTFConfig().getContinentConfig().scale), NamedTextColor.WHITE))
            );
            stack.getSender().sendMessage(
                Component.text("River Scale: ", NamedTextColor.GRAY)
                    .append(Component.text(String.valueOf(plugin.getRTFConfig().getRiverConfig().scale), NamedTextColor.WHITE))
            );
        }
    }

    private void handleReload(CommandSourceStack stack) {
        if (!stack.getSender().hasPermission("nekotf.reload")) {
            stack.getSender().sendMessage(
                Component.text("[RTF] ", NamedTextColor.RED)
                    .append(Component.text("You don't have permission to reload the config.", NamedTextColor.WHITE))
            );
            return;
        }
        
        try {
            plugin.getRTFConfig().reloadConfig();
            plugin.getBridge().setConfig(plugin.getRTFConfig());
            stack.getSender().sendMessage(
                Component.text("[RTF] ", NamedTextColor.GREEN)
                    .append(Component.text("Configuration reloaded successfully!", NamedTextColor.WHITE))
            );
            stack.getSender().sendMessage(
                Component.text("Note: Config changes only affect newly generated chunks.", NamedTextColor.GRAY)
            );
        } catch (Exception e) {
            stack.getSender().sendMessage(
                Component.text("[RTF] ", NamedTextColor.RED)
                    .append(Component.text("Failed to reload config: " + e.getMessage(), NamedTextColor.WHITE))
            );
            e.printStackTrace();
        }
    }
}
