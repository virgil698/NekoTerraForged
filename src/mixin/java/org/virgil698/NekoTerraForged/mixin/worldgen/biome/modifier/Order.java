package org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * 生物群系修改器顺序
 * 控制特征添加到生物群系的位置
 * 移植自 ReTerraForged
 */
public enum Order implements StringRepresentable {
    /**
     * 前置 - 将新特征添加到列表开头
     */
    PREPEND("prepend") {
        @Override
        public <T> List<T> add(List<T> list1, List<T> list2) {
            List<T> newList = new ArrayList<>(list1.size() + list2.size());
            newList.addAll(list2);
            newList.addAll(list1);
            return newList;
        }
    },
    /**
     * 追加 - 将新特征添加到列表末尾
     */
    APPEND("append") {
        @Override
        public <T> List<T> add(List<T> list1, List<T> list2) {
            List<T> newList = new ArrayList<>(list1.size() + list2.size());
            newList.addAll(list1);
            newList.addAll(list2);
            return newList;
        }
    };

    public static final Codec<Order> CODEC = StringRepresentable.fromEnum(Order::values);
    
    private String name;
    
    private Order(String name) {
        this.name = name;
    }
    
    @Override
    public String getSerializedName() {
        return this.name;
    }
    
    /**
     * 合并两个列表
     * @param list1 原始列表
     * @param list2 要添加的列表
     * @return 合并后的新列表
     */
    public abstract <T> List<T> add(List<T> list1, List<T> list2);
}
