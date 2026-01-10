package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.TemplateFeature.Config;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.decorator.DecoratorConfig;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.decorator.TemplateDecorator;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.paste.Paste;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.paste.PasteConfig;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.paste.PasteType;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.placement.TemplatePlacement;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.Dimensions;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.FeatureTemplate;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.FeatureTemplateManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.TemplateContext;

/**
 * 模板特征
 * 移植自ReTerraForged
 */
public class TemplateFeature extends Feature<Config<?>> {
    
    private FeatureTemplateManager templateManager;

    public TemplateFeature(Codec<Config<?>> codec) {
        super(codec);
    }
    
    public void setTemplateManager(FeatureTemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public boolean place(FeaturePlaceContext<Config<?>> ctx) {
        RandomSource random = ctx.random();
        Config<?> config = ctx.config();
        
        Mirror mirror = nextMirror(random);
        Rotation rotation = nextRotation(random);
        return paste(ctx.level(), random, ctx.origin(), mirror, rotation, config, FeatureTemplate.WORLD_GEN, templateManager);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TemplateContext> boolean paste(WorldGenLevel world, RandomSource rand, BlockPos pos, Mirror mirror, Rotation rotation, Config<T> config, PasteType pasteType, FeatureTemplateManager templateManager) {
        return paste(world, rand, pos, mirror, rotation, config, pasteType, false, templateManager);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TemplateContext> boolean paste(WorldGenLevel world, RandomSource rand, BlockPos pos, Mirror mirror, Rotation rotation, Config<T> config, PasteType pasteType, boolean modified, FeatureTemplateManager templateManager) {
        if (config.templates().isEmpty()) {
            return false;
        }
        
        if (templateManager == null) {
            return false;
        }
        
        DecoratorConfig<T> decoratorConfig = config.decorator();
        
        ResourceLocation templateName = nextTemplate(config.templates, rand);
        FeatureTemplate template = templateManager.load(templateName);
        
        Dimensions dimensions = template.getDimensions(mirror, rotation);
        TemplatePlacement<T> placement = config.placement();
        if (!placement.canPlaceAt(world, pos, dimensions)) {
            return false;
        }

        Paste paste = pasteType.get(template);
        T buffer = placement.createContext();
        if (paste.apply(world, buffer, pos, mirror, rotation, placement, config.paste())) {
            ResourceKey<Biome> biome = world.getBiome(pos).unwrapKey().orElse(null);
            for (TemplateDecorator<T> decorator : decoratorConfig.getDecorators(biome)) {
                decorator.apply(world, buffer, rand, modified);
            }
            return true;
        }

        return false;
    }

    private static ResourceLocation nextTemplate(List<ResourceLocation> templates, RandomSource random) {
        return templates.get(random.nextInt(templates.size()));
    }

    private static Mirror nextMirror(RandomSource random) {
        return Mirror.values()[random.nextInt(Mirror.values().length)];
    }

    private static Rotation nextRotation(RandomSource random) {
        return Rotation.values()[random.nextInt(Rotation.values().length)];
    }
    
    public record Config<T extends TemplateContext>(List<ResourceLocation> templates, TemplatePlacement<T> placement, PasteConfig paste, DecoratorConfig<T> decorator) implements FeatureConfiguration {
    }
}
