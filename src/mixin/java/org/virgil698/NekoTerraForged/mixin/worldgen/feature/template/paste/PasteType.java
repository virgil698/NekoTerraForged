package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.paste;

import org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.template.FeatureTemplate;

/**
 * 粘贴类型接口
 * 移植自ReTerraForged
 */
public interface PasteType {
    Paste get(FeatureTemplate template);
}
