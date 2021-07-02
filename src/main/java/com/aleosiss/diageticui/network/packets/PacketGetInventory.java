package com.aleosiss.diageticui.network.packets;

import com.aleosiss.diageticui.data.ContainerType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class PacketGetInventory  {
    private NbtCompound inventory;
    private BlockPos pos;
    private ContainerType containerType;
    private int invMaxSize;

    public PacketGetInventory(NbtCompound inventory, BlockPos pos, ContainerType containerType, int invMaxSize) {
        this.inventory = inventory;
        this.pos = pos;
        this.containerType = containerType;
        this.invMaxSize = invMaxSize;
    }

    public NbtCompound getInventory() {
        return inventory;
    }

    public void setInventory(NbtCompound inventory) {
        this.inventory = inventory;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }

    public int getInvMaxSize() {
        return invMaxSize;
    }

    public void setInvMaxSize(int invMaxSize) {
        this.invMaxSize = invMaxSize;
    }

    public static PacketGetInventory fromPacketBuffer(PacketByteBuf buf) {
        BlockPos blockPos = buf.readBlockPos();
        NbtCompound compoundTag = buf.readNbt();
        ContainerType containerType = buf.readEnumConstant(ContainerType.class);
        int invMaxSize = buf.readInt();

        return new PacketGetInventory(compoundTag, blockPos, containerType, invMaxSize);
    }

    public static PacketByteBuf toPacketBuffer(PacketGetInventory packetData) {
        PacketByteBuf packetByteBuf = PacketByteBufs.create();
        packetByteBuf.writeBlockPos(packetData.getPos());
        packetByteBuf.writeNbt(packetData.getInventory());
        packetByteBuf.writeEnumConstant(packetData.getContainerType());
        packetByteBuf.writeInt(packetData.getInvMaxSize());

        return packetByteBuf;
    }

    public static PacketByteBuf toPacketBuffer(NbtCompound inventoryTagData, BlockPos pos, ContainerType containerType, int invMaxSize) {
        return toPacketBuffer(new PacketGetInventory(inventoryTagData, pos, containerType, invMaxSize));
    }
}
