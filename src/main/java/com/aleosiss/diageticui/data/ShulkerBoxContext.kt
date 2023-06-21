package com.aleosiss.diageticui.data

import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

class ShulkerBoxContext(var box: ShulkerBoxBlock, var inventory: DefaultedList<ItemStack>)
