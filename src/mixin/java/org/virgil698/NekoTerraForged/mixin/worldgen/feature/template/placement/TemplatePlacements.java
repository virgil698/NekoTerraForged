package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.placement;

/**
 * 模板放置工厂
 * 移植自ReTerraForged
 */
public class TemplatePlacements {

    public static void bootstrap() {
        // 注册放置类型
    }
    
    public static AnyPlacement any() {
        return new AnyPlacement();
    }    
    
    public static TreePlacement tree() {
        return new TreePlacement();
    }
}
