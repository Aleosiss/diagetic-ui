package com.aleosiss.diageticui.network.packets

import com.aleosiss.diageticui.data.ContainerType
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos

class PacketGetInventory(
    var inventory: NbtCompound?,
    var pos: BlockPos,
    var containerType: ContainerType,
    var invMaxSize: Int
) {
    companion object {
        fun fromPacketBuffer(buf: PacketByteBuf): PacketGetInventory {
            val blockPos = buf.readBlockPos()
            val compoundTag = buf.readNbt()
            val containerType = buf.readEnumConstant(ContainerType::class.java)
            val invMaxSize = buf.readInt()
            return PacketGetInventory(compoundTag, blockPos, containerType, invMaxSize)
        }

        fun toPacketBuffer(packetData: PacketGetInventory): PacketByteBuf {
            val packetByteBuf = PacketByteBufs.create()
            packetByteBuf.writeBlockPos(packetData.pos)
            packetByteBuf.writeNbt(packetData.inventory)
            packetByteBuf.writeEnumConstant(packetData.containerType)
            packetByteBuf.writeInt(packetData.invMaxSize)
            return packetByteBuf
        }

        fun toPacketBuffer(
            inventoryTagData: NbtCompound?,
            pos: BlockPos,
            containerType: ContainerType,
            invMaxSize: Int
        ): PacketByteBuf {
            return toPacketBuffer(PacketGetInventory(inventoryTagData, pos, containerType, invMaxSize))
        }
    }
}
