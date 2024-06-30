package com.aleosiss.diageticui.network

import com.aleosiss.diageticui.DiageticUI
import com.aleosiss.diageticui.data.ContainerType
import com.mojang.authlib.properties.PropertyMap
import net.minecraft.data.DataGenerator.Pack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object NetworkConstants {
    @JvmField
    val DIAGETIC_INVENTORY_REQUEST_PACKET: Identifier = Identifier.of(DiageticUI.MOD_ID, "inventory_request_packet")
    val DIAGETIC_INVENTORY_REQUEST_PAYLOAD: CustomPayload.Id<InventoryRequestPayload> = CustomPayload.id(DIAGETIC_INVENTORY_REQUEST_PACKET.toTranslationKey())
    @JvmField
    val DIAGETIC_INVENTORY_RESPONSE_PACKET: Identifier = Identifier.of(DiageticUI.MOD_ID, "inventory_response_packet")
    val DIAGETIC_INVENTORY_RESPONSE_PAYLOAD: CustomPayload.Id<InventoryResponsePayload> = CustomPayload.id(DIAGETIC_INVENTORY_RESPONSE_PACKET.toTranslationKey())


    class InventoryRequestPayload(blockPos: BlockPos): CustomPayload {
        val blockPosLong: Long = blockPos.asLong()

        override fun getId(): CustomPayload.Id<out CustomPayload> {
            return DIAGETIC_INVENTORY_REQUEST_PAYLOAD
        }

        companion object {
            val CODEC: PacketCodec<RegistryByteBuf, InventoryRequestPayload> = PacketCodec.tuple(PacketCodecs.VAR_LONG, InventoryRequestPayload::blockPosLong) {
                InventoryRequestPayload(BlockPos.fromLong(it))
            }
        }
    }

    class InventoryResponsePayload(
        val inventoryTagData: NbtCompound?,
        pos: BlockPos,
        containerType: ContainerType,
        val invMaxSize: Int
    ): CustomPayload {
        val blockPosLong: Long = pos.asLong()
        val containerTypeInt: Int = containerType.ordinal

        override fun getId(): CustomPayload.Id<out CustomPayload> {
            return DIAGETIC_INVENTORY_RESPONSE_PAYLOAD
        }

        companion object {
            val CODEC: PacketCodec<RegistryByteBuf, InventoryResponsePayload> = PacketCodec.tuple(
                PacketCodecs.NBT_COMPOUND, InventoryResponsePayload::inventoryTagData,
                PacketCodecs.VAR_LONG, InventoryResponsePayload::blockPosLong,
                PacketCodecs.INTEGER, InventoryResponsePayload::containerTypeInt,
                PacketCodecs.INTEGER, InventoryResponsePayload::invMaxSize
            ) { inventoryTagData, blockPos, containerType, invMaxSive ->
                InventoryResponsePayload(inventoryTagData, BlockPos.fromLong(blockPos), ContainerType.entries[containerType], invMaxSive)
            }
        }
    }
}
