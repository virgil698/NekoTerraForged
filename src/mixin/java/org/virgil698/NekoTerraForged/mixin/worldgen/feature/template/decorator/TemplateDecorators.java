package org.virgil698.NekoTerraForged.mixin.worldgen.feature.template.decorator;

/**
 * 模板装饰器工厂
 * 移植自ReTerraForged
 */
public class TemplateDecorators {

    public static void bootstrap() {
        // 注册装饰器类型
    }
    
    public static TreeDecorator tree(net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator decorator) {
        return tree(decorator, decorator);
    }
    
    public static TreeDecorator tree(net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator decorator, net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator modifiedDecorator) {
        return new TreeDecorator(decorator, modifiedDecorator);
    }
}
