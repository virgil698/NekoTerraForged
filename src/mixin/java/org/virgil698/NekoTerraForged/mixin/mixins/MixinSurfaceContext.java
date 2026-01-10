package org.virgil698.NekoTerraForged.mixin.mixins;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.virgil698.NekoTerraForged.mixin.worldgen.surface.RTFSurfaceContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.surface.SurfaceRegion;

/**
 * Mixin 注入 SurfaceRules.Context
 * 添加周围生物群系信息用于表面生成
 * 移植自 ReTerraForged MixinContext
 */
@Implements(@Interface(iface = RTFSurfaceContext.class, prefix = "rtf$"))
@Mixin(SurfaceRules.Context.class)
public abstract class MixinSurfaceContext {
    
    @Shadow
    @Final
    public ChunkAccess chunk;

    @Unique
    @Nullable
    private Set<ResourceKey<Biome>> surroundingBiomes;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onInit(CallbackInfo callback) {
        WorldGenRegion region = SurfaceRegion.get();

        if (region != null) {
            ChunkPos centerPos = this.chunk.getPos();

            this.surroundingBiomes = new HashSet<>();

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    ChunkAccess chunk = region.getChunk(centerPos.x + x, centerPos.z + z);

                    for (LevelChunkSection section : chunk.getSections()) {
                        section.getBiomes().getAll((biome) -> {
                            biome.unwrapKey().ifPresent(this.surroundingBiomes::add);
                        });
                    }
                }
            }
        }
    }

    /**
     * 获取周围的生物群系
     */
    public Set<ResourceKey<Biome>> rtf$getSurroundingBiomes() {
        return this.surroundingBiomes;
    }
}
