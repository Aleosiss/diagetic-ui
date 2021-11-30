package com.aleosiss.diageticui.network.handlers;

import com.aleosiss.diageticui.data.ContainerType;
import com.aleosiss.diageticui.network.NetworkConstants;
import com.aleosiss.diageticui.network.NetworkService;
import com.aleosiss.diageticui.network.packets.PacketGetInventory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReceiveInventoryRequestHandler implements ServerPlayNetworking.PlayChannelHandler {
    private static final Logger logger = LogManager.getLogger();

    public static final ServerPlayNetworking.PlayChannelHandler SERVER_RECEIVE_INVENTORY_REQUEST = new ReceiveInventoryRequestHandler();

    NetworkService networkService = NetworkService.getInstance();

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BlockPos blockPos = buf.readBlockPos();
        HitResult clientCrosshairRaytrace = player.raycast(5, 1, false);
        if(clientCrosshairRaytrace == null || !HitResult.Type.BLOCK.equals(clientCrosshairRaytrace.getType())) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult) clientCrosshairRaytrace;
        if(!blockHitResult.getBlockPos().equals(blockPos)) {
            return;
        }

        server.execute(() -> {
            BlockState blockState = player.world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            NbtCompound inventoryTagData = new NbtCompound();
            BlockEntity blockEntity = player.world.getBlockEntity(blockPos);
            int invMaxSize;
            ContainerType containerType;

            if(block instanceof ChestBlock chestBlock) {
                Inventory inventory = ChestBlock.getInventory(chestBlock, blockState, player.world, blockPos, true);
                if (inventory == null) { return; }
                invMaxSize = inventory.size();
                DefaultedList<ItemStack> items = DefaultedList.ofSize(invMaxSize, ItemStack.EMPTY);
                for(int i = 0; i < invMaxSize; ++i) {
                    items.set(i, inventory.getStack(i));
                }

                if (inventory instanceof DoubleInventory){
                    containerType = ContainerType.DOUBLE_CHEST;
                } else {
                    containerType = ContainerType.SINGLE_CHEST;
                }

                inventoryTagData = Inventories.writeNbt(inventoryTagData, items);
            }
            else if(blockEntity instanceof LockableContainerBlockEntity lbe) {
                containerType = ContainerType.from(lbe);

                invMaxSize = lbe.size();
                inventoryTagData = lbe.createNbt();
            } else {
                return;
            }
            responseSender.sendPacket(
                    NetworkConstants.DIAGETIC_INVENTORY_RESPONSE_PACKET,
                    PacketGetInventory.toPacketBuffer(inventoryTagData, blockEntity.getPos(), containerType, invMaxSize)
            );
        });
    }
}
