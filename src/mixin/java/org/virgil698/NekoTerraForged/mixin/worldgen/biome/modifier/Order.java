package org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.StringRepresentable;

/**
 * 生物群系修改器顺序
 * 移植自 ReTerraForged
 */
public enum Order implements StringRepresentable {
    PREPEND("prepend") {
        @Override
        public <T> List<T> add(List<T> list1, List<T> list2) {
            List<T> newList = new ArrayList<>(list1.size() + list2.size());
            newList.addAll(list2);
            newList.addAll(list1);
            return newList;
        }
    },
    APPEND("append") {
        @Override
        public <T> List<T> add(List<T> list1, List<T> list2) {
            List<T> newList = new ArrayList<>(list1.size() + list2.size());
            newList.addAll(list1);
            newList.addAll(list2);
            return newList;
        }
    };

    private String name;
    
    private Order(String name) {
        this.name = name;
    }
    
    @Override
    public String getSerializedName() {
        return this.name;
    }
    
    public abstract <T> List<T> add(List<T> list1, List<T> list2);
}
