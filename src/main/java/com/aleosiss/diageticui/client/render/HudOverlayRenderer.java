package com.aleosiss.diageticui.client.render;

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
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

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


    public HudOverlayRenderer(Identifier rendererId) {
        this.rendererId = rendererId;
    }

    public void show(MatrixStack ms, float v) {
        HitResult clientCrosshairRaytrace = MinecraftClient.getInstance().crosshairTarget;
        if(clientCrosshairRaytrace == null || !HitResult.Type.BLOCK.equals(clientCrosshairRaytrace.getType())) {
            currentTarget = null;
            cachedRequestData = null;
            return;
        }

        BlockHitResult targetBlockResult = (BlockHitResult) clientCrosshairRaytrace;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;

        // TODO: Move this
        if(mc.currentScreen != null) {
            return;
        }

        currentTarget = targetBlockResult.getBlockPos();
        if(world == null) {
            logger.warn("World was null at show");
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(currentTarget);
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

        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(cachedRequestData.invMaxSize, ItemStack.EMPTY);
        Inventories.fromTag(cachedRequestData.inventoryData, inventory);
        ContainerType containerType = cachedRequestData.containerType;

        ms.push();
        Screen hudOverlayScreen = new BaseContainerScreen(null) {
        };
        hudOverlayScreen.init(mc,
                mc.getWindow().getScaledWidth(),
                mc.getWindow().getScaledHeight());

        int posX = hudOverlayScreen.width / 2 + getOverlayWidthOffset();
        int posY = hudOverlayScreen.height / 2 + getOverlayHeightOffset();
        int posZ = hudOverlayScreen.getZOffset();

        int invItemsPerRow = containerType.getInvItemsPerRow();
        if(ContainerType.OTHER.equals(containerType)) {
            invItemsPerRow = getInvItemsPerRow(blockEntity);
        }


        drawInventoryGUIImage(mc, containerType, invItemsPerRow, inventory, blockEntity, posX, posY, posZ);
        drawInventoryItems(mc, containerType, invItemsPerRow, inventory, blockEntity, posX, posY, posZ);

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
        if(cachedRequestData == null) {
            return false;
        }

        // handle double chests
        if (isPartOfCachedDoubleChest(currentBlock)) return true;

        return cachedRequestData.blockEntity.getPos().equals(currentTarget);
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

    private void drawInventoryGUIImage(MinecraftClient mc, ContainerType type, int invItemsPerRow, DefaultedList<ItemStack> inventory, LockableContainerBlockEntity blockEntity, int posX, int posY, int zOff) {
        float[] color = DEFAULT_COLOR;
        if(ContainerType.SHULKER_BOX.equals(type)) {
            color = getShulkerBoxColor(blockEntity);
        }

        setTexture(mc, color);

        RenderSystem.enableAlphaTest();
        DiffuseLighting.disable();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);

        final double zOffset = zOff + 100.0;
        int invSize = inventory.size();
        int rowSize = invItemsPerRow;
        int rowWidth = rowSize * 18;

        int xOffset = 7;
        int yOffset = 7;

        manipulateTexture(posX, posY, builder, zOffset, invSize, rowSize, rowWidth, xOffset, yOffset);

        tessellator.draw();
        RenderSystem.disableAlphaTest();
        DiffuseLighting.enable();
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

    // TODO: understand how this works
    private void manipulateTexture(int posX, int posY, BufferBuilder builder, double zOffset, int invSize, int rowSize, int rowWidth, int xOffset, int yOffset) {
        blitZOffset(builder, posX, posY, 0, 0, 7, 7, zOffset);
        for (int size = rowSize; size > 0; size -= 9) {
            int s = Math.min(size, 9);

            blitZOffset(builder, posX + xOffset, posY, 7, 0, s * 18, 7, zOffset);
            xOffset += s * 18;
        }
        blitZOffset(builder, posX + rowWidth + 7, posY, 169, 0, 7, 7, zOffset);

        int rowTexYPos = 7;
        while (invSize > 0) {
            xOffset = 7;
            blitZOffset(builder, posX, posY + yOffset, 0, rowTexYPos, 7, 18, zOffset);
            for (int rSize = rowSize; rSize > 0; rSize -= 9) {
                int s = Math.min(rSize, 9);

                blitZOffset(builder, posX + xOffset, posY + yOffset, 7, rowTexYPos, s * 18, 18, zOffset);
                xOffset += s * 18;
            }
            blitZOffset(builder, posX + xOffset, posY + yOffset, 169, rowTexYPos, 7, 18, zOffset);
            yOffset += 18;
            invSize -= rowSize;
            rowTexYPos = rowTexYPos >= 43 ? 7 : rowTexYPos + 18;
        }

        xOffset = 7;
        blitZOffset(builder, posX, posY + yOffset, 0, 61, 7, 7, zOffset);
        for (int size = rowSize; size > 0; size -= 9) {
            int s = Math.min(size, 9);

            blitZOffset(builder, posX + xOffset, posY + yOffset, 7, 61, s * 18, 7, zOffset);
            xOffset += s * 18;
        }
        blitZOffset(builder, posX + rowWidth + 7, posY + yOffset, 169, 61, 7, 7, zOffset);
    }

    private void setTexture(MinecraftClient mc, float[] color) {
        RenderSystem.color3f(color[0], color[1], color[2]);
        mc.getTextureManager().bindTexture(DEFAULT_TEXTURE_LIGHT);
    }

    public void blitZOffset(BufferBuilder builder, int x, int y, int u, int v, int w, int h, double zOffset) {
        builder.vertex(x, y + h, zOffset).texture(u * 0.00390625f, (v + h) * 0.00390625f).next();
        builder.vertex(x + w, y + h, zOffset).texture((u + w) * 0.00390625f, (v + h) * 0.00390625f).next();
        builder.vertex(x + w, y, zOffset).texture((u + w) * 0.00390625f, (v + 0) * 0.00390625f).next();
        builder.vertex(x, y, zOffset).texture(u * 0.00390625f, v * 0.00390625f).next();
    }

    private void requestInventoryData(BlockEntity blockEntity) {
        long now = System.currentTimeMillis();

        boolean sameBlock = false;
        if(cachedRequestData != null) {
            sameBlock = blockEntity.getPos().equals(cachedRequestData.blockEntity.getPos());
        }

        if (!sameBlock || now > cachedRequestData.timeReceived + packetPollerTimeMs) {
            PacketByteBuf request = networkService.buildInventoryRequestPacket(blockEntity);
            ClientPlayNetworking.send(NetworkConstants.DIAGETIC_INVENTORY_REQUEST_PACKET, request);
        }
    }

    public static class CachedRequestData {
        private CompoundTag inventoryData;
        private BlockEntity blockEntity;
        private long timeReceived;
        private ContainerType containerType;
        private int invMaxSize;

        public CachedRequestData(CompoundTag inventoryData, BlockEntity blockEntity, long timeReceived, ContainerType containerType, int invMaxSize) {
            this.inventoryData = inventoryData;
            this.blockEntity = blockEntity;
            this.timeReceived = timeReceived;
            this.containerType = containerType;
            this.invMaxSize = invMaxSize;
        }
    }
}
