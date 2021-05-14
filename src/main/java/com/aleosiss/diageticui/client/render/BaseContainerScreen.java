package com.aleosiss.diageticui.client.render;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class BaseContainerScreen extends Screen {


    protected BaseContainerScreen(DefaultedList<ItemStack> inventory) {
        super(null);

    }
}
