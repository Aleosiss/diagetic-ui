package com.aleosiss.diageticui.data

import net.minecraft.block.entity.*

enum class ContainerType(
    val containerClass: Class<out LockableContainerBlockEntity>,
    val invMaxSize: Int,
    @JvmField val invItemsPerRow: Int
) {
    SHULKER_BOX(ShulkerBoxBlockEntity::class.java, 27, 9),
    SINGLE_CHEST(ChestBlockEntity::class.java, 27, 9),
    DOUBLE_CHEST(ChestBlockEntity::class.java, 54, 9),
    BARREL(BarrelBlockEntity::class.java, 27, 9),
    HOPPER(HopperBlockEntity::class.java, 5, 5),
    DROPPER(DropperBlockEntity::class.java, 9, 3),
    DISPENSER(DispenserBlockEntity::class.java, 9, 3),
    FURNACE(FurnaceBlockEntity::class.java, 3, 3),
    BREWING_STAND(BrewingStandBlockEntity::class.java, 5, 5),
    OTHER(LockableContainerBlockEntity::class.java, 1, 1);

    companion object {
        fun from(lookupClass: LockableContainerBlockEntity): ContainerType {
            if (lookupClass.size() == DOUBLE_CHEST.invMaxSize && lookupClass is ChestBlockEntity) return DOUBLE_CHEST
            for (i in values().indices) {
                val value = values()[i]
                if (lookupClass.javaClass == value.containerClass) {
                    return value
                }
            }
            return OTHER
        }
    }
}
