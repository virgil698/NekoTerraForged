package org.virgil698.NekoTerraForged.mixin.world;

import org.virgil698.NekoTerraForged.mixin.math.Node;

/**
 * 气候系统
 * 管理温度、湿度、降水等气候参数
 */
public class ClimateSystem {
    private final Node temperatureNode;
    private final Node moistureNode;
    private final Node windNode;

    public ClimateSystem(int seed) {
        // 温度噪声 - 大尺度变化
        this.temperatureNode = Node.Fbm(Node.Scale(Node.Simplex(), 3000), 3, 2.0, 0.5);
        
        // 湿度噪声 - 中等尺度
        this.moistureNode = Node.Fbm(Node.Scale(Node.Simplex(), 2000), 3, 2.0, 0.5);
        
        // 风向噪声 - 影响天气模式
        this.windNode = Node.Fbm(Node.Scale(Node.Simplex(), 5000), 2, 2.0, 0.5);
    }

    /**
     * 获取气候数据
     */
    public ClimateData sample(int seed, int x, int z, int y) {
        ClimateData data = new ClimateData();
        
        // 基础温度（受纬度影响）
        double baseTemp = temperatureNode.eval(seed, x, z);
        
        // 高度影响温度（每升高100格降低约0.3度）
        double heightEffect = -((y - 63) / 100.0) * 0.3;
        
        data.temperature = baseTemp + heightEffect;
        
        // 湿度
        data.moisture = moistureNode.eval(seed + 1000, x, z);
        
        // 风向和风速
        double windValue = windNode.eval(seed + 2000, x, z);
        data.windDirection = windValue * Math.PI; // -π 到 π
        data.windSpeed = Math.abs(windValue);
        
        // 降水概率（基于温度和湿度）
        data.precipitation = calculatePrecipitation(data.temperature, data.moisture);
        
        // 气候类型
        data.climateType = determineClimateType(data.temperature, data.moisture);
        
        return data;
    }

    private double calculatePrecipitation(double temperature, double moisture) {
        // 温暖湿润 = 高降水
        // 寒冷干燥 = 低降水
        double tempFactor = 1.0 - Math.abs(temperature); // 极端温度降低降水
        double moistureFactor = (moisture + 1.0) * 0.5; // 归一化到 0-1
        
        return tempFactor * moistureFactor;
    }

    private ClimateType determineClimateType(double temperature, double moisture) {
        if (temperature < -0.5) {
            return ClimateType.POLAR;
        } else if (temperature < -0.2) {
            return moisture > 0.0 ? ClimateType.BOREAL : ClimateType.TUNDRA;
        } else if (temperature < 0.2) {
            if (moisture < -0.3) {
                return ClimateType.ARID;
            } else if (moisture > 0.3) {
                return ClimateType.TEMPERATE_HUMID;
            } else {
                return ClimateType.TEMPERATE;
            }
        } else if (temperature < 0.6) {
            if (moisture < -0.3) {
                return ClimateType.SEMI_ARID;
            } else if (moisture > 0.3) {
                return ClimateType.SUBTROPICAL_HUMID;
            } else {
                return ClimateType.SUBTROPICAL;
            }
        } else {
            if (moisture < 0.0) {
                return ClimateType.DESERT;
            } else {
                return ClimateType.TROPICAL;
            }
        }
    }

    /**
     * 气候数据
     */
    public static class ClimateData {
        public double temperature;      // -1 到 1
        public double moisture;         // -1 到 1
        public double windDirection;    // -π 到 π
        public double windSpeed;        // 0 到 1
        public double precipitation;    // 0 到 1
        public ClimateType climateType;
    }

    /**
     * 气候类型
     */
    public enum ClimateType {
        POLAR,              // 极地
        TUNDRA,             // 苔原
        BOREAL,             // 北方针叶林
        TEMPERATE,          // 温带
        TEMPERATE_HUMID,    // 温带湿润
        SUBTROPICAL,        // 亚热带
        SUBTROPICAL_HUMID,  // 亚热带湿润
        TROPICAL,           // 热带
        ARID,               // 干旱
        SEMI_ARID,          // 半干旱
        DESERT              // 沙漠
    }
}
