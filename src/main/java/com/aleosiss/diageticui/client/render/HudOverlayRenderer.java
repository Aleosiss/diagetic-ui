package com.aleosiss.diageticui.client.render;

import com.aleosiss.diageticui.client.render.util.DrawHelpers;
import com.aleosiss.diageticui.data.ContainerType;
import com.aleosiss.diageticui.network.NetworkConstants;
import com.aleosiss.diageticui.network.NetworkService;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.block.ShulkerBoxBlock.getColor;


@Environment(EnvType.CLIENT)
public class HudOverlayRenderer {

    private static final Identifier DEFAULT_TEXTURE_LIGHT = new Identifier("diageticui", "textures/gui/shulker_box_tooltip.png");
    static float[] DEFAULT_COLOR = new float[] { 1f, 1f, 1f };
    // Time between inventory requests of the same block
    private long packetPollerTimeMs = 150;
    private final Logger logger = LogManager.getLogger();
    private final Identifier rendererId;

    private final NetworkService networkService = NetworkService.getInstance();
    private BlockPos currentTarget;
    private long lastRequestTime;
    private CachedRequestData cachedRequestData;

    private int widthOffset = 20;
    private int heightOffset = 0;

    // does this reduce allocations?
    private HitResult clientCrosshairRaytrace;
    private MinecraftClient mc;
    private ClientWorld world;
    private BlockHitResult targetBlockResult;
    private BlockEntity blockEntity;
    private DefaultedList<ItemStack> inventory;
    private Screen hudOverlayScreen = new BaseContainerScreen(new TranslatableText("diageticui.container.screen")) {};


    public HudOverlayRenderer(Identifier rendererId) {
        this.rendererId = rendererId;
    }

    public void show(MatrixStack ms, float v) {
        mc = MinecraftClient.getInstance();
        world = mc.world;

        clientCrosshairRaytrace = mc.crosshairTarget;
        if(clientCrosshairRaytrace == null || !HitResult.Type.BLOCK.equals(clientCrosshairRaytrace.getType())) {
            currentTarget = null;
            cachedRequestData = null;
            return;
        }

        targetBlockResult = (BlockHitResult) clientCrosshairRaytrace;

        // TODO: Move this
        if(mc.currentScreen != null) {
            return;
        }

        currentTarget = targetBlockResult.getBlockPos();
        if(world == null) {
            logger.warn("World was null at show");
            return;
        }
        blockEntity = world.getBlockEntity(currentTarget);
        if(blockEntity instanceof LockableContainerBlockEntity) {
            draw(ms, mc, world, (LockableContainerBlockEntity) blockEntity);
        }
    }

    public void updateCachedRequestData(CachedRequestData cachedRequestData) {
        this.cachedRequestData = cachedRequestData;
    }

    private void draw(MatrixStack ms, MinecraftClient mc, ClientWorld world, LockableContainerBlockEntity blockEntity) {
        requestInventoryData(blockEntity);
        if(!validateCache(blockEntity)) {
            cachedRequestData = null;
            return;
        }

        inventory = DefaultedList.ofSize(cachedRequestData.invMaxSize, ItemStack.EMPTY);
        Inventories.readNbt(cachedRequestData.inventoryData, inventory);

        ms.push();
        hudOverlayScreen.init(mc,
                mc.getWindow().getScaledWidth(),
                mc.getWindow().getScaledHeight());

        int posX = hudOverlayScreen.width / 2 + getOverlayWidthOffset();
        int posY = hudOverlayScreen.height / 2 + getOverlayHeightOffset();
        int posZ = hudOverlayScreen.getZOffset();

        int invItemsPerRow = cachedRequestData.containerType.getInvItemsPerRow();
        if(ContainerType.OTHER.equals(cachedRequestData.containerType)) {
            invItemsPerRow = getInvItemsPerRow(blockEntity);
        }


        drawInventoryBackgroundImage(mc, cachedRequestData.containerType, invItemsPerRow, inventory, blockEntity, posX, posY, posZ);
        drawInventoryItems(mc, cachedRequestData.containerType, invItemsPerRow, inventory, blockEntity, posX, posY, posZ);

        ms.pop();
    }

    private int getOverlayHeightOffset() {
        return heightOffset;
    }

    private int getOverlayWidthOffset() {
        return widthOffset;
    }

    public void setWidthOffset(int widthOffset) {
        this.widthOffset = widthOffset;
    }

    public void setHeightOffset(int heightOffset) {
        this.heightOffset = heightOffset;
    }

    private int getInvItemsPerRow(LockableContainerBlockEntity blockEntity) {
        return 1;
    }

    private boolean validateCache(BlockEntity currentBlock) {
        if(cachedRequestData == null || cachedRequestData.blockEntity == null) {
            return false;
        }

        // handle double chests
        if (isPartOfCachedDoubleChest(currentBlock)) return true;

        return currentTarget.equals(cachedRequestData.blockEntity.getPos());
    }

    private boolean isPartOfCachedDoubleChest(BlockEntity currentBlock) {
        if(currentBlock instanceof ChestBlockEntity && !cachedRequestData.blockEntity.getPos().equals(currentBlock.getPos()) && cachedRequestData.blockEntity instanceof ChestBlockEntity) {
            ChestType chestType1 = currentBlock.getCachedState().get(ChestBlock.CHEST_TYPE);
            ChestType chestType2 = cachedRequestData.blockEntity.getCachedState().get(ChestBlock.CHEST_TYPE);

            // not a perfect check but it works in my naive test cases
            return !ChestType.SINGLE.equals(chestType1) && chestType1.getOpposite().equals(chestType2);
        }
        return false;
    }

    private static void drawInventoryItems(MinecraftClient mc, ContainerType type, int invItemsPerRow, DefaultedList<ItemStack> inventory, LockableContainerBlockEntity blockEntity, int posX, int posY, int zOff) {
        ItemRenderer itemRenderer = mc.getItemRenderer();
        TextRenderer textRenderer = mc.textRenderer;

        for (int i = 0, size = inventory.size(); i < size; ++i) {
            ItemStack stack = inventory.get(i);
            int xOffset = 8 + posX + 18 * (i % invItemsPerRow);
            int yOffset = 8 + posY + 18 * (i / invItemsPerRow);

            itemRenderer.renderInGuiWithOverrides(stack, xOffset, yOffset);
            itemRenderer.renderGuiItemOverlay(textRenderer, stack, xOffset, yOffset);
        }
    }

    private void drawInventoryBackgroundImage(MinecraftClient mc, ContainerType type,
                                              int invItemsPerRow, DefaultedList<ItemStack> inventory,
                                              LockableContainerBlockEntity blockEntity,
                                              int x, int y, int z)
    {
        final int zOffset = z + 100;
        int invSize = inventory.size();
        int xOffset = 7;
        int yOffset = 7;
        int rowSize = invItemsPerRow;
        int rowWidth = rowSize * 18;

        float[] color = DEFAULT_COLOR;
        if(ContainerType.SHULKER_BOX.equals(type)) {
            color = getShulkerBoxColor(blockEntity);
        }

        setTexture(mc, color);

        DrawHelpers.manipulateTexture(new MatrixStack(), x, y, zOffset, invSize, rowSize, rowWidth, xOffset, yOffset);

    }

    private float[] getShulkerBoxColor(LockableContainerBlockEntity blockEntity) {
        float[] SHULKER_BOX_COLOR = new float[] { 0.592f, 0.403f, 0.592f };
        float[] color;
        DyeColor dyeColor = getColor(blockEntity.getCachedState().getBlock());
        if (dyeColor != null) {
            float[] components = dyeColor.getColorComponents();
            color = new float[] {
                    Math.max(0.15f, components[0]),
                    Math.max(0.15f, components[1]),
                    Math.max(0.15f, components[2])
            };
        } else {
            color = SHULKER_BOX_COLOR;
        }
        return color;
    }

    private void setTexture(MinecraftClient mc, float[] color) {
        RenderSystem.setShaderColor(color[0], color[1], color[2], 1.0f);
        RenderSystem.setShaderTexture(0, DEFAULT_TEXTURE_LIGHT);
    }

    private void requestInventoryData(BlockEntity blockEntity) {
        long now = System.currentTimeMillis();

        boolean sameBlock = false;
        if(cachedRequestData != null && cachedRequestData.blockEntity != null) {
            sameBlock = blockEntity.getPos().equals(cachedRequestData.blockEntity.getPos());
        }

        if (!sameBlock || now > cachedRequestData.timeReceived + packetPollerTimeMs) {
            PacketByteBuf request = networkService.buildInventoryRequestPacket(blockEntity);
            ClientPlayNetworking.send(NetworkConstants.DIAGETIC_INVENTORY_REQUEST_PACKET, request);
        }
    }

    public static class CachedRequestData {
        private NbtCompound inventoryData;
        private BlockEntity blockEntity;
        private long timeReceived;
        private ContainerType containerType;
        private int invMaxSize;

        public CachedRequestData(NbtCompound inventoryData, BlockEntity blockEntity, long timeReceived, ContainerType containerType, int invMaxSize) {
            this.inventoryData = inventoryData;
            this.blockEntity = blockEntity;
            this.timeReceived = timeReceived;
            this.containerType = containerType;
            this.invMaxSize = invMaxSize;
        }
    }
}
