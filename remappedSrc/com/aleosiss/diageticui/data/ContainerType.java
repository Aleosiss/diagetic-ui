package com.aleosiss.diageticui.data;

import net.minecraft.block.entity.*;

public enum ContainerType {
    SHULKER_BOX(ShulkerBoxBlockEntity.class, 27, 9),
    SINGLE_CHEST(ChestBlockEntity.class, 27, 9),
    DOUBLE_CHEST(ChestBlockEntity.class, 54, 9),
    BARREL(BarrelBlockEntity.class, 27, 9),
    HOPPER(HopperBlockEntity.class, 5, 5),
    DROPPER(DropperBlockEntity.class, 9, 3),
    DISPENSER(DispenserBlockEntity.class, 9, 3),
    FURNACE(FurnaceBlockEntity.class, 3, 3),
    BREWING_STAND(BrewingStandBlockEntity.class, 5, 5),
    OTHER(LockableContainerBlockEntity.class, 1, 1);


    private final Class<? extends LockableContainerBlockEntity> containerClass;
    private final int invMaxSize;

    public Class<? extends LockableContainerBlockEntity> getContainerClass() {
        return containerClass;
    }

    public int getInvMaxSize() {
        return invMaxSize;
    }

    public int getInvItemsPerRow() {
        return invItemsPerRow;
    }

    private final int invItemsPerRow;


    ContainerType(Class<? extends LockableContainerBlockEntity> containerClass, int invMaxSize, int invItemsPerRow) {
        this.containerClass = containerClass;
        this.invMaxSize = invMaxSize;
        this.invItemsPerRow = invItemsPerRow;
    }

    public static ContainerType from(LockableContainerBlockEntity lookupClass) {
        if(lookupClass.size() == DOUBLE_CHEST.invMaxSize && lookupClass instanceof ChestBlockEntity) return DOUBLE_CHEST;
        for(int i = 0; i < values().length; i++) {
            ContainerType value = ContainerType.values()[i];
            if(lookupClass.getClass().equals(value.containerClass)) {
                return value;
            }
        }

        return OTHER;
    }
}
