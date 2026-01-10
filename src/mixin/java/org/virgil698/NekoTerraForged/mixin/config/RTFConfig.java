package org.virgil698.NekoTerraForged.mixin.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RTF 配置管理器
 * 使用 JSON 格式存储配置
 */
public class RTFConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Path configPath;
    private final Map<String, Object> config = new ConcurrentHashMap<>();
    
    // 默认配置值
    private static final Map<String, Object> DEFAULTS = new HashMap<>();
    
    static {
        // 世界生成设置
        DEFAULTS.put("worldgen.enabled", true);
        DEFAULTS.put("worldgen.seed_offset", 0);
        DEFAULTS.put("worldgen.sea_level", 63);
        
        // 性能设置
        DEFAULTS.put("performance.cull_noise_sections", true);
        DEFAULTS.put("performance.fast_lookups", true);
        DEFAULTS.put("performance.fast_cell_lookups", true);
        DEFAULTS.put("performance.thread_count", 4);
        
        // 地形设置
        DEFAULTS.put("terrain.continent_scale", 3000);
        DEFAULTS.put("terrain.region_scale", 1000);
        DEFAULTS.put("terrain.erosion_strength", 0.5);
        DEFAULTS.put("terrain.river_width", 1.0);
        
        // 气候设置
        DEFAULTS.put("climate.temperature_scale", 1.0);
        DEFAULTS.put("climate.moisture_scale", 1.0);
        DEFAULTS.put("climate.biome_size", 4);
        
        // 表面设置
        DEFAULTS.put("surface.strata_enabled", true);
        DEFAULTS.put("surface.erosion_enabled", true);
        
        // 调试设置
        DEFAULTS.put("debug.enabled", false);
        DEFAULTS.put("debug.log_generation", false);
    }
    
    public RTFConfig(Path configPath) {
        this.configPath = configPath;
        loadDefaults();
    }
    
    private void loadDefaults() {
        config.putAll(DEFAULTS);
    }
    
    /**
     * 加载配置文件
     */
    public void load() {
        if (!Files.exists(configPath)) {
            save(); // 创建默认配置
            return;
        }
        
        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            loadFromJson(json, "");
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadFromJson(JsonObject json, String prefix) {
        for (String key : json.keySet()) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            var element = json.get(key);
            
            if (element.isJsonObject()) {
                loadFromJson(element.getAsJsonObject(), fullKey);
            } else if (element.isJsonPrimitive()) {
                var primitive = element.getAsJsonPrimitive();
                if (primitive.isBoolean()) {
                    config.put(fullKey, primitive.getAsBoolean());
                } else if (primitive.isNumber()) {
                    Number num = primitive.getAsNumber();
                    // 尝试保持原始类型
                    if (num.doubleValue() == num.intValue()) {
                        config.put(fullKey, num.intValue());
                    } else {
                        config.put(fullKey, num.doubleValue());
                    }
                } else if (primitive.isString()) {
                    config.put(fullKey, primitive.getAsString());
                }
            }
        }
    }
    
    /**
     * 保存配置文件
     */
    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            
            JsonObject root = new JsonObject();
            
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                String[] parts = entry.getKey().split("\\.");
                JsonObject current = root;
                
                for (int i = 0; i < parts.length - 1; i++) {
                    if (!current.has(parts[i])) {
                        current.add(parts[i], new JsonObject());
                    }
                    current = current.getAsJsonObject(parts[i]);
                }
                
                String lastKey = parts[parts.length - 1];
                Object value = entry.getValue();
                
                if (value instanceof Boolean b) {
                    current.addProperty(lastKey, b);
                } else if (value instanceof Number n) {
                    current.addProperty(lastKey, n);
                } else if (value instanceof String s) {
                    current.addProperty(lastKey, s);
                }
            }
            
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取配置值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            // 类型转换
            if (defaultValue instanceof Integer && value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            } else if (defaultValue instanceof Double && value instanceof Number) {
                return (T) Double.valueOf(((Number) value).doubleValue());
            } else if (defaultValue instanceof Float && value instanceof Number) {
                return (T) Float.valueOf(((Number) value).floatValue());
            } else if (defaultValue instanceof Long && value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
    
    /**
     * 设置配置值
     */
    public void set(String key, Object value) {
        config.put(key, value);
    }
    
    /**
     * 获取所有配置
     */
    public Map<String, Object> getAll() {
        return new HashMap<>(config);
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        config.clear();
        loadDefaults();
        load();
    }
    
    // ==================== 便捷方法 ====================
    
    public boolean isWorldGenEnabled() {
        return get("worldgen.enabled", true);
    }
    
    public int getSeaLevel() {
        return get("worldgen.sea_level", 63);
    }
    
    public boolean isCullNoiseSections() {
        return get("performance.cull_noise_sections", true);
    }
    
    public boolean isFastLookups() {
        return get("performance.fast_lookups", true);
    }
    
    public boolean isFastCellLookups() {
        return get("performance.fast_cell_lookups", true);
    }
    
    public int getThreadCount() {
        return get("performance.thread_count", 4);
    }
    
    public int getContinentScale() {
        return get("terrain.continent_scale", 3000);
    }
    
    public int getRegionScale() {
        return get("terrain.region_scale", 1000);
    }
    
    public double getErosionStrength() {
        return get("terrain.erosion_strength", 0.5);
    }
    
    public boolean isDebugEnabled() {
        return get("debug.enabled", false);
    }
}
