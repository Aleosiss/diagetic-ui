package com.aleosiss.diageticui.client.render

import com.aleosiss.diageticui.client.render.util.DrawHelpers.manipulateTexture
import com.aleosiss.diageticui.data.ContainerType
import com.aleosiss.diageticui.network.NetworkConstants
import com.aleosiss.diageticui.network.NetworkService.Companion.instance
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.ChestBlock
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.block.enums.ChestType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.world.ClientWorld
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.apache.logging.log4j.LogManager

@Environment(EnvType.CLIENT)
class HudOverlayRenderer(private val rendererId: Identifier) {
    // Time between inventory requests of the same block
    private val packetPollerTimeMs: Long = 150
    private val logger = LogManager.getLogger()
    private val networkService = instance
    private var currentTarget: BlockPos? = null
    private val lastRequestTime: Long = 0
    private var cachedRequestData: CachedRequestData? = null
    private var overlayWidthOffset = 20
    private var overlayHeightOffset = 0

    // does this reduce allocations?
    private var clientCrosshairRaytrace: HitResult? = null
    private lateinit var mc: MinecraftClient
    private var world: ClientWorld? = null
    private var targetBlockResult: BlockHitResult? = null
    private var blockEntity: BlockEntity? = null
    private var inventory: DefaultedList<ItemStack>? = null
    private val hudOverlayScreen: Screen =
        object : BaseContainerScreen(Text.translatable("diageticui.container.screen")) {}

    fun show(draw: DrawContext) {
        mc = MinecraftClient.getInstance()!!
        world = mc.world
        clientCrosshairRaytrace = mc.crosshairTarget
        if (clientCrosshairRaytrace == null || HitResult.Type.BLOCK != clientCrosshairRaytrace!!.type) {
            currentTarget = null
            cachedRequestData = null
            return
        }
        targetBlockResult = clientCrosshairRaytrace as BlockHitResult?

        // TODO: Move this
        if (mc.currentScreen != null) {
            return
        }
        currentTarget = targetBlockResult!!.blockPos
        if (world == null) {
            logger.warn("World was null at show")
            return
        }
        blockEntity = world!!.getBlockEntity(currentTarget)
        if (blockEntity is LockableContainerBlockEntity) {
            draw(draw, mc, blockEntity as LockableContainerBlockEntity)
        }
    }

    fun updateCachedRequestData(cachedRequestData: CachedRequestData?) {
        this.cachedRequestData = cachedRequestData
    }

    private fun draw(
        draw: DrawContext,
        mc: MinecraftClient,
        blockEntity: LockableContainerBlockEntity
    ) {
        requestInventoryData(blockEntity)
        if (!validateCache(blockEntity)) {
            cachedRequestData = null
            return
        }
        inventory = DefaultedList.ofSize(cachedRequestData!!.invMaxSize, ItemStack.EMPTY)
        Inventories.readNbt(cachedRequestData!!.inventoryData, inventory, mc.world!!.registryManager)
        draw.matrices.push()
        hudOverlayScreen.init(
            mc,
            mc.window.scaledWidth,
            mc.window.scaledHeight
        )
        val posX = hudOverlayScreen.width / 2 + overlayWidthOffset
        val posY = hudOverlayScreen.height / 2 + overlayHeightOffset
        val posZ = hudOverlayScreen.navigationOrder
        var invItemsPerRow = cachedRequestData!!.containerType.invItemsPerRow
        if (ContainerType.OTHER == cachedRequestData!!.containerType) {
            invItemsPerRow = getInvItemsPerRow(blockEntity)
        }
        drawInventoryBackgroundImage(
            draw,
            cachedRequestData!!.containerType,
            invItemsPerRow,
            inventory,
            blockEntity,
            posX,
            posY,
            posZ
        )
        drawInventoryItems(
            draw,
            mc,
            invItemsPerRow,
            inventory,
            posX,
            posY
        )
        draw.matrices.pop()
    }

    fun setWidthOffset(widthOffset: Int) {
        overlayWidthOffset = widthOffset
    }

    fun setHeightOffset(heightOffset: Int) {
        overlayHeightOffset = heightOffset
    }

    private fun getInvItemsPerRow(blockEntity: LockableContainerBlockEntity): Int {
        return 1
    }

    private fun validateCache(currentBlock: BlockEntity): Boolean {
        if (cachedRequestData == null || cachedRequestData!!.blockEntity == null) {
            return false
        }

        // handle double chests
        return if (isPartOfCachedDoubleChest(currentBlock)) true else currentTarget == cachedRequestData!!.blockEntity!!.pos
    }

    private fun isPartOfCachedDoubleChest(currentBlock: BlockEntity): Boolean {
        if (currentBlock is ChestBlockEntity && cachedRequestData!!.blockEntity!!.pos != currentBlock.getPos() && cachedRequestData!!.blockEntity is ChestBlockEntity) {
            val chestType1 = currentBlock.getCachedState().get(ChestBlock.CHEST_TYPE)
            val chestType2 = cachedRequestData!!.blockEntity?.cachedState?.get(ChestBlock.CHEST_TYPE)

            // not a perfect check, but it works in my naive test cases
            return ChestType.SINGLE != chestType1 && chestType1.opposite == chestType2
        }
        return false
    }

    private fun drawInventoryItems(
        draw: DrawContext,
        mc: MinecraftClient?,
        invItemsPerRow: Int,
        inventory: DefaultedList<ItemStack>?,
        posX: Int,
        posY: Int
    ) {
        val textRenderer = mc!!.textRenderer
        var i = 0
        val size = inventory!!.size
        while (i < size) {
            val stack = inventory[i]
            val xOffset = 8 + posX + 18 * (i % invItemsPerRow)
            val yOffset = 8 + posY + 18 * (i / invItemsPerRow)
            draw.drawItem(stack, xOffset, yOffset)
            draw.drawItemInSlot(textRenderer, stack, xOffset, yOffset)
            ++i
        }
    }

    private fun drawInventoryBackgroundImage(
        draw: DrawContext, type: ContainerType, invItemsPerRow: Int,
        inventory: DefaultedList<ItemStack>?, blockEntity: LockableContainerBlockEntity,
        x: Int,
        y: Int, z: Int
    ) {
        val zOffset = z - 100
        val invSize = inventory!!.size
        val xOffset = 7
        val yOffset = 7
        val rowWidth = invItemsPerRow * 18
        var color = DEFAULT_COLOR
        if (ContainerType.SHULKER_BOX == type) {
            color = getShulkerBoxColor(blockEntity)
        }
        manipulateTexture(
            draw,
            DEFAULT_TEXTURE_LIGHT,
            color,
            x,
            y,
            zOffset,
            invSize,
            invItemsPerRow,
            rowWidth,
            xOffset,
            yOffset
        )
    }

    private fun getShulkerBoxColor(blockEntity: LockableContainerBlockEntity): FloatArray {
        val shulkerBoxColor = floatArrayOf(0.592f, 0.403f, 0.592f)
        val color: FloatArray
        val dyeColor: DyeColor? = ShulkerBoxBlock.getColor(blockEntity.cachedState.block)
        color = if (dyeColor != null) {
            val components = Vec3d.unpackRgb(dyeColor.entityColor)
            floatArrayOf(
                0.15f.coerceAtLeast(components.x.toFloat()), // r
                0.15f.coerceAtLeast(components.y.toFloat()), // g
                0.15f.coerceAtLeast(components.z.toFloat())  // b
            )
        } else {
            shulkerBoxColor
        }
        return color
    }

    private fun requestInventoryData(blockEntity: BlockEntity) {
        val now = System.currentTimeMillis()
        var sameBlock = false
        if (cachedRequestData != null && cachedRequestData!!.blockEntity != null) {
            sameBlock = blockEntity.pos == cachedRequestData!!.blockEntity!!.pos
        }
        if (!sameBlock || now > cachedRequestData!!.timeReceived + packetPollerTimeMs) {
            ClientPlayNetworking.send(NetworkConstants.InventoryRequestPayload(blockEntity.pos))
        }
    }

    class CachedRequestData(
        val inventoryData: NbtCompound,
        val blockEntity: BlockEntity?,
        val timeReceived: Long,
        val containerType: ContainerType,
        val invMaxSize: Int
    )

    companion object {
        private val DEFAULT_TEXTURE_LIGHT = Identifier.of("diageticui", "textures/gui/shulker_box_tooltip.png")
        var DEFAULT_COLOR = floatArrayOf(1f, 1f, 1f)
    }
}
