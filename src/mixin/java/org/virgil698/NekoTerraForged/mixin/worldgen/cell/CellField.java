package org.virgil698.NekoTerraForged.mixin.worldgen.cell;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * Cell 字段枚举，用于从 Cell 中读取特定字段
 * 移植自 ReTerraForged
 * 
 * 注意：MC 的密度函数值范围是 -1 到 1，而 RTF 的 Cell 值范围是 0 到 1
 * 需要进行适当的映射转换
 */
public enum CellField implements StringRepresentable {
    HEIGHT("height") {
        @Override
        public float read(Cell cell) {
            // height 保持 0-1 范围
            return cell.height;
        }
    },
    CONTINENT_NOISE("continent_noise") {
        @Override
        public float read(Cell cell) {
            return cell.continentNoise;
        }
    },
    CONTINENTALNESS("continentalness") {
        @Override
        public float read(Cell cell) {
            // MC 的 continentalness 范围是 -1.2 到 1.2
            // RTF 的 continentalness 范围是 0 到 1
            // 映射: 0 -> -1.2, 1 -> 1.2
            return cell.continentalness * 2.4f - 1.2f;
        }
    },
    EROSION("erosion") {
        @Override
        public float read(Cell cell) {
            // MC 的 erosion 范围是 -1 到 1
            // RTF 的 erosion 范围是 0 到 1
            // 映射: 0 -> -1, 1 -> 1
            return cell.erosion * 2.0f - 1.0f;
        }
    },
    WEIRDNESS("weirdness") {
        @Override
        public float read(Cell cell) {
            // MC 的 weirdness/ridges 范围是 -1 到 1
            // RTF 的 weirdness 范围是 -1 到 1 (已经是正确范围)
            return cell.weirdness;
        }
    },
    TERRAIN_REGION("terrain_region") {
        @Override
        public float read(Cell cell) {
            return cell.terrainRegionId;
        }
    },
    TERRAIN_REGION_EDGE("terrain_region_edge") {
        @Override
        public float read(Cell cell) {
            return cell.terrainRegionEdge;
        }
    },
    TERRAIN_MASK("terrain_mask") {
        @Override
        public float read(Cell cell) {
            return cell.terrainMask;
        }
    },
    RIVER_DISTANCE("river_distance") {
        @Override
        public float read(Cell cell) {
            return cell.riverDistance;
        }
    },
    BIOME_REGION("biome_region") {
        @Override
        public float read(Cell cell) {
            return cell.biomeRegionId;
        }
    },
    TEMPERATURE("temperature") {
        @Override
        public float read(Cell cell) {
            // MC 的 temperature 范围是 -1 到 1
            // RTF 的 temperature 已经是 -1 到 1 范围 (来自 BiomeType.getTemperature -> Temperature.midpoint)
            // 直接返回，不需要转换
            return cell.temperature;
        }
    },
    MOISTURE("moisture") {
        @Override
        public float read(Cell cell) {
            // MC 的 vegetation/humidity 范围是 -1 到 1
            // RTF 的 moisture 已经是 -1 到 1 范围 (来自 BiomeType.getMoisture -> Humidity.midpoint)
            // 直接返回，不需要转换
            return cell.moisture;
        }
    },
    GRADIENT("gradient") {
        @Override
        public float read(Cell cell) {
            return cell.gradient;
        }
    },
    LOCAL_EROSION("local_erosion") {
        @Override
        public float read(Cell cell) {
            return cell.localErosion;
        }
    },
    SEDIMENT("sediment") {
        @Override
        public float read(Cell cell) {
            return cell.sediment;
        }
    };

    public static final Codec<CellField> CODEC = StringRepresentable.fromEnum(CellField::values);

    private final String name;

    CellField(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public abstract float read(Cell cell);
}
