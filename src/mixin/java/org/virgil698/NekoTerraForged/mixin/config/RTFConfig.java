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
 * 配置文件由插件层负责生成，此类只负责读取和管理
 */
public class RTFConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final Path configPath;
    private final Map<String, Object> config = new ConcurrentHashMap<>();
    
    public RTFConfig(Path configPath) {
        this.configPath = configPath;
    }
    
    /**
     * 加载配置文件
     * 配置文件应该由插件层预先生成
     */
    public void load() {
        // 加载默认值
        loadDefaults();
        
        // 如果配置文件存在，加载用户配置（覆盖默认值）
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                loadFromJson(json, "");
                System.out.println("[NekoTerraForged] Loaded config from: " + configPath);
            } catch (Exception e) {
                System.err.println("[NekoTerraForged] Failed to load config: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[NekoTerraForged] Config file not found, using defaults: " + configPath);
        }
    }
    
    /**
     * 加载内置默认值
     */
    private void loadDefaults() {
        // worldgen
        config.put("worldgen.enabled", true);
        config.put("worldgen.seed_offset", 0);
        config.put("worldgen.sea_level", 63);
        config.put("worldgen.world_height", 384);
        config.put("worldgen.min_y", -64);
        
        // performance
        config.put("performance.cull_noise_sections", true);
        config.put("performance.fast_lookups", true);
        config.put("performance.fast_cell_lookups", true);
        config.put("performance.thread_count", 4);
        config.put("performance.tile_size", 3);
        config.put("performance.batch_count", 6);
        
        // terrain
        config.put("terrain.continent_scale", 3000);
        config.put("terrain.continent_shape", 1);
        config.put("terrain.region_scale", 1000);
        config.put("terrain.erosion_strength", 0.5);
        config.put("terrain.river_width", 1.0);
        config.put("terrain.mountain_height", 1.0);
        config.put("terrain.volcano_chance", 0.7);
        
        // climate
        config.put("climate.temperature_scale", 1.0);
        config.put("climate.moisture_scale", 1.0);
        config.put("climate.biome_size", 4);
        config.put("climate.biome_warp_scale", 150);
        config.put("climate.biome_warp_strength", 80);
        
        // surface
        config.put("surface.strata_enabled", true);
        config.put("surface.erosion_enabled", true);
        config.put("surface.natural_snow_enabled", true);
        config.put("surface.smooth_layer_decorator", true);
        
        // structure
        config.put("structure.terrain_match_enabled", true);
        config.put("structure.smooth_enabled", true);
        
        // feature
        config.put("feature.erosion_decorator_enabled", true);
        config.put("feature.custom_biome_features", true);
        
        // noise
        config.put("noise.continent_noise_octaves", 5);
        config.put("noise.continent_noise_gain", 0.5);
        config.put("noise.continent_noise_lacunarity", 2.5);
        config.put("noise.terrain_noise_octaves", 5);
        config.put("noise.terrain_noise_gain", 0.5);
        config.put("noise.terrain_noise_lacunarity", 2.5);
        
        // debug
        config.put("debug.enabled", false);
        config.put("debug.log_generation", false);
        config.put("debug.log_surface", false);
        config.put("debug.log_biomes", false);
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
        load();
    }
    
    // ==================== 便捷方法 ====================
    
    // --- worldgen ---
    public boolean isWorldGenEnabled() {
        return get("worldgen.enabled", true);
    }
    
    public int getSeaLevel() {
        return get("worldgen.sea_level", 63);
    }
    
    public int getWorldHeight() {
        return get("worldgen.world_height", 384);
    }
    
    public int getMinY() {
        return get("worldgen.min_y", -64);
    }
    
    // --- performance ---
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
    
    public int getTileSize() {
        return get("performance.tile_size", 3);
    }
    
    public int getBatchCount() {
        return get("performance.batch_count", 6);
    }
    
    // --- terrain ---
    public int getContinentScale() {
        return get("terrain.continent_scale", 3000);
    }
    
    public int getContinentShape() {
        return get("terrain.continent_shape", 1);
    }
    
    public int getRegionScale() {
        return get("terrain.region_scale", 1000);
    }
    
    public double getErosionStrength() {
        return get("terrain.erosion_strength", 0.5);
    }
    
    public double getRiverWidth() {
        return get("terrain.river_width", 1.0);
    }
    
    public double getMountainHeight() {
        return get("terrain.mountain_height", 1.0);
    }
    
    public double getVolcanoChance() {
        return get("terrain.volcano_chance", 0.7);
    }
    
    // --- climate ---
    public double getTemperatureScale() {
        return get("climate.temperature_scale", 1.0);
    }
    
    public double getMoistureScale() {
        return get("climate.moisture_scale", 1.0);
    }
    
    public int getBiomeSize() {
        return get("climate.biome_size", 4);
    }
    
    public int getBiomeWarpScale() {
        return get("climate.biome_warp_scale", 150);
    }
    
    public int getBiomeWarpStrength() {
        return get("climate.biome_warp_strength", 80);
    }
    
    // --- surface ---
    public boolean isStrataEnabled() {
        return get("surface.strata_enabled", true);
    }
    
    public boolean isErosionEnabled() {
        return get("surface.erosion_enabled", true);
    }
    
    public boolean isNaturalSnowEnabled() {
        return get("surface.natural_snow_enabled", true);
    }
    
    // --- debug ---
    public boolean isDebugEnabled() {
        return get("debug.enabled", false);
    }
    
    public boolean isLogGeneration() {
        return get("debug.log_generation", false);
    }
}
