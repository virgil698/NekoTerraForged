package org.virgil698.NekoTerraForged.mixin.mixins;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * 通用结构生成验证
 * 确保所有结构都在合适的地形上生成
 */
@Mixin(Structure.class)
public class MixinStructure {
    
    /**
     * 拦截所有结构的生成点查找
     * 根据结构类型验证地形适合性
     */
    @Inject(method = "findGenerationPoint", at = @At("RETURN"), cancellable = true)
    private void ntf$validateStructurePlacement(Structure.GenerationContext context, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        // 如果已经取消生成，不需要再检查
        if (cir.getReturnValue().isEmpty()) {
            return;
        }
        
        Structure structure = (Structure) (Object) this;
        StructureType<?> type = structure.type();
        
        int blockX = context.chunkPos().getBlockX(8);
        int blockZ = context.chunkPos().getBlockZ(8);
        int seaLevel = context.chunkGenerator().getSeaLevel();
        
        // 根据结构类型进行不同的验证
        if (isOceanStructure(type)) {
            if (!validateOceanStructure(context, blockX, blockZ, seaLevel, type)) {
                cir.setReturnValue(Optional.empty());
            }
        } else if (isLandStructure(type)) {
            if (!validateLandStructure(context, blockX, blockZ, seaLevel)) {
                cir.setReturnValue(Optional.empty());
            }
        } else if (isBeachStructure(type)) {
            if (!validateBeachStructure(context, blockX, blockZ, seaLevel)) {
                cir.setReturnValue(Optional.empty());
            }
        }
        // 地下结构（矿井、要塞）不需要额外验证，因为它们在地下生成
    }
    
    /**
     * 判断是否为海洋结构
     */
    private boolean isOceanStructure(StructureType<?> type) {
        return type == StructureType.OCEAN_MONUMENT 
            || type == StructureType.OCEAN_RUIN 
            || type == StructureType.SHIPWRECK && !isBeachedShipwreck();
    }
    
    /**
     * 判断是否为陆地结构
     */
    private boolean isLandStructure(StructureType<?> type) {
        return type == StructureType.JIGSAW  // 村庄、掠夺者哨塔等
            || type == StructureType.DESERT_PYRAMID
            || type == StructureType.JUNGLE_TEMPLE
            || type == StructureType.SWAMP_HUT
            || type == StructureType.IGLOO
            || type == StructureType.WOODLAND_MANSION;
    }
    
    /**
     * 判断是否为海滩结构
     */
    private boolean isBeachStructure(StructureType<?> type) {
        return type == StructureType.BURIED_TREASURE
            || (type == StructureType.SHIPWRECK && isBeachedShipwreck());
    }
    
    /**
     * 检查是否为搁浅沉船（需要在子类中重写）
     */
    private boolean isBeachedShipwreck() {
        // 默认返回 false，在 MixinShipwreckStructure 中会被正确处理
        return false;
    }
    
    /**
     * 验证海洋结构
     */
    private boolean validateOceanStructure(Structure.GenerationContext context, int blockX, int blockZ, int seaLevel, StructureType<?> type) {
        // 海底神殿需要特殊检查
        if (type == StructureType.OCEAN_MONUMENT) {
            // 检查周围生物群系
            boolean hasOceanBiomes = false;
            for (Holder<Biome> holder : context.biomeSource()
                .getBiomesWithin(blockX, seaLevel, blockZ, 29, context.randomState().sampler())) {
                if (holder.is(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING)) {
                    hasOceanBiomes = true;
                    break;
                }
            }
            
            if (!hasOceanBiomes) {
                return false;
            }
            
            // 检查地形深度 - 海底神殿需要深水
            int samples = 0;
            int underwaterSamples = 0;
            
            for (int dx = -16; dx <= 16; dx += 8) {
                for (int dz = -16; dz <= 16; dz += 8) {
                    int x = blockX + dx;
                    int z = blockZ + dz;
                    int surfaceHeight = context.chunkGenerator().getBaseHeight(x, z, 
                        Heightmap.Types.OCEAN_FLOOR_WG, 
                        context.heightAccessor(), context.randomState());
                    
                    samples++;
                    if (surfaceHeight < seaLevel - 10) {
                        underwaterSamples++;
                    }
                }
            }
            
            // 至少 80% 的采样点都在深水中
            return underwaterSamples >= samples * 0.8;
        }
        
        // 其他海洋结构（海底遗迹、沉船）
        int surfaceHeight = context.chunkGenerator().getBaseHeight(blockX, blockZ, 
            Heightmap.Types.OCEAN_FLOOR_WG, 
            context.heightAccessor(), context.randomState());
        
        // 应该在海平面以下，但不要太深
        return surfaceHeight < seaLevel && surfaceHeight >= seaLevel - 40;
    }
    
    /**
     * 验证陆地结构
     */
    private boolean validateLandStructure(Structure.GenerationContext context, int blockX, int blockZ, int seaLevel) {
        // 采样周围区域
        int samples = 0;
        int landSamples = 0;
        int totalHeight = 0;
        
        for (int dx = -8; dx <= 8; dx += 4) {
            for (int dz = -8; dz <= 8; dz += 4) {
                int x = blockX + dx;
                int z = blockZ + dz;
                int surfaceHeight = context.chunkGenerator().getBaseHeight(x, z, 
                    Heightmap.Types.WORLD_SURFACE_WG, 
                    context.heightAccessor(), context.randomState());
                
                samples++;
                totalHeight += surfaceHeight;
                
                if (surfaceHeight > seaLevel) {
                    landSamples++;
                }
            }
        }
        
        // 至少 70% 的采样点应该是陆地
        if (landSamples < samples * 0.7) {
            return false;
        }
        
        // 检查地形平坦度
        int avgHeight = totalHeight / samples;
        int maxHeightDiff = 0;
        
        for (int dx = -8; dx <= 8; dx += 4) {
            for (int dz = -8; dz <= 8; dz += 4) {
                int x = blockX + dx;
                int z = blockZ + dz;
                int surfaceHeight = context.chunkGenerator().getBaseHeight(x, z, 
                    Heightmap.Types.WORLD_SURFACE_WG, 
                    context.heightAccessor(), context.randomState());
                
                maxHeightDiff = Math.max(maxHeightDiff, Math.abs(surfaceHeight - avgHeight));
            }
        }
        
        // 如果地形太陡峭，有概率取消生成
        if (maxHeightDiff > 20) {
            return context.random().nextFloat() >= 0.5f;
        }
        
        return true;
    }
    
    /**
     * 验证海滩结构
     */
    private boolean validateBeachStructure(Structure.GenerationContext context, int blockX, int blockZ, int seaLevel) {
        int surfaceHeight = context.chunkGenerator().getBaseHeight(blockX, blockZ, 
            Heightmap.Types.WORLD_SURFACE_WG, 
            context.heightAccessor(), context.randomState());
        
        // 应该在海平面附近 ±5 格
        return Math.abs(surfaceHeight - seaLevel) <= 5;
    }
}
