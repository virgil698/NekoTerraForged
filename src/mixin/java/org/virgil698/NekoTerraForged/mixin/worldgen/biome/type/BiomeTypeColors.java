package org.virgil698.NekoTerraForged.mixin.worldgen.biome.type;

import java.awt.Color;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 生物群系类型颜色管理
 * 移植自 ReTerraForged
 */
public class BiomeTypeColors {
    private static final BiomeTypeColors INSTANCE = new BiomeTypeColors();
    private Map<String, Color> colors;
    
    private BiomeTypeColors() {
        this.colors = new HashMap<>();
        try (InputStream inputStream = BiomeType.class.getResourceAsStream("/biomes.txt")) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    Color color = Color.decode("#" + entry.getValue().toString());
                    this.colors.put(entry.getKey().toString(), color);
                }
            }
        } catch (Exception e) {
            // 忽略加载错误，使用默认颜色
        }
    }
    
    public Color getColor(String name, Color defaultColor) {
        return this.colors.getOrDefault(name, defaultColor);
    }
    
    public static BiomeTypeColors getInstance() {
        return BiomeTypeColors.INSTANCE;
    }
}
