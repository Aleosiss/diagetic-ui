package com.aleosiss.diageticui.network.handlers

import com.aleosiss.diageticui.data.ContainerType
import com.aleosiss.diageticui.network.NetworkConstants
import com.aleosiss.diageticui.network.packets.PacketGetInventory.Companion.toPacketBuffer
import com.aleosiss.diageticui.toBlockPos
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.inventory.DoubleInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.LogManager

class ReceiveInventoryRequestHandler : ServerPlayNetworking.PlayPayloadHandler<NetworkConstants.InventoryRequestPayload> {
    override fun receive(payload: NetworkConstants.InventoryRequestPayload, context: ServerPlayNetworking.Context) {
        // read in the block the player says they're looking at and validate
        val blockPos = payload.blockPosLong.toBlockPos()
        val clientCrosshairRaytrace = context.player().raycast(5.0, 1f, false)
        if (clientCrosshairRaytrace == null || HitResult.Type.BLOCK != clientCrosshairRaytrace.type) {
            return
        }
        val blockHitResult = clientCrosshairRaytrace as BlockHitResult
        if (blockHitResult.blockPos != blockPos) {
            return
        }

        // okay we're definitely looking at a block
        context.server().execute { receive(context.player(), blockPos, context.responseSender()) }
    }

    private fun receive(
        player: ServerPlayerEntity,
        blockPos: BlockPos?,
        responseSender: PacketSender
    ) {
        val blockState = player.world.getBlockState(blockPos)
        val block = blockState.block
        var inventoryTagData: NbtCompound? = NbtCompound()
        val blockEntity = player.world.getBlockEntity(blockPos)
        val invMaxSize: Int
        val containerType: ContainerType

        when {
            block is ChestBlock -> {
                val inventory = ChestBlock.getInventory(block, blockState, player.world, blockPos, true)
                    ?: return
                invMaxSize = inventory.size()
                val items = DefaultedList.ofSize(invMaxSize, ItemStack.EMPTY)
                for (i in 0 until invMaxSize) {
                    items[i] = inventory.getStack(i)
                }
                containerType =
                    (if (inventory is DoubleInventory) ContainerType.DOUBLE_CHEST else ContainerType.SINGLE_CHEST)
                inventoryTagData = Inventories.writeNbt(inventoryTagData, items, player.world.registryManager)
            }
            blockEntity is LockableContainerBlockEntity -> {
                containerType = ContainerType.from(blockEntity)
                invMaxSize = blockEntity.size()
                inventoryTagData = blockEntity.createNbt(player.world.registryManager)
            }
            else -> return
        }
        blockPos?.let {
            responseSender.sendPacket(NetworkConstants.InventoryResponsePayload(inventoryTagData, it, containerType, invMaxSize))
        }
    }

    companion object {
        private val logger = LogManager.getLogger()
        val SERVER_RECEIVE_INVENTORY_REQUEST: ServerPlayNetworking.PlayPayloadHandler<NetworkConstants.InventoryRequestPayload> = ReceiveInventoryRequestHandler()
    }
}
