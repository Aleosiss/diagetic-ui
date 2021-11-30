package com.aleosiss.diageticui.data;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class ShulkerBoxContext {

    private ShulkerBoxBlock box;
    private DefaultedList<ItemStack> inventory;


    public ShulkerBoxContext(ShulkerBoxBlock box, DefaultedList<ItemStack> inventory) {
        this.box = box;
        this.inventory = inventory;
    }

    public ShulkerBoxBlock getBox() {
        return box;
    }

    public void setBox(ShulkerBoxBlock box) {
        this.box = box;
    }

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    public void setInventory(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }
}
