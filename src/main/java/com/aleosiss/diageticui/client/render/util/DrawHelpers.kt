package com.aleosiss.diageticui.client.render.util

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier

object DrawHelpers {
    // TODO: understand how this works
    @JvmStatic
    fun manipulateTexture(
        draw: DrawContext,
        texture: Identifier,
        color: FloatArray,
        x: Int,
        y: Int,
        zOffset: Int,
        invSize: Int,
        rowSize: Int,
        rowWidth: Int,
        xOffset: Int,
        yOffset: Int
    ) {
        var invSize = invSize
        var xOffset = xOffset
        var yOffset = yOffset
        var rowTexYPos = 7
        draw.setShaderColor(color[0], color[1], color[2], 1.0f)

        // top side
        run {
            var size = rowSize
            while (size > 0) {
                val s = Math.min(size, 9)
                draw.drawTexture(texture, x + xOffset, y, zOffset, 7f, 0f, s * 18, 7, 256, 256)
                xOffset += s * 18
                size -= 9
            }
        }
        while (invSize > 0) {
            xOffset = 7
            // left side
            draw.drawTexture(texture, x, y + yOffset, zOffset, 0f, rowTexYPos.toFloat(), 7, 18, 256, 256)
            var rSize = rowSize
            while (rSize > 0) {
                val s = Math.min(rSize, 9)

                // center
                draw.drawTexture(
                    texture, x + xOffset, y + yOffset, zOffset, 7f, rowTexYPos.toFloat(),
                    s * 18, 18, 256, 256
                )
                xOffset += s * 18
                rSize -= 9
            }
            // right side
            draw.drawTexture(
                texture, x + xOffset, y + yOffset, zOffset, 169f, rowTexYPos.toFloat(), 7,
                18, 256, 256
            )
            yOffset += 18
            invSize -= rowSize
            rowTexYPos = if (rowTexYPos >= 43) 7 else rowTexYPos + 18
        }
        xOffset = 7
        var size = rowSize
        while (size > 0) {
            val s = Math.min(size, 9)

            // bottom side
            draw.drawTexture(
                texture, x + xOffset, y + yOffset, zOffset, 7f, 61f, s * 18, 7, 256,
                256
            )
            xOffset += s * 18
            size -= 9
        }

        // top-left corner
        draw.drawTexture(texture, x, y, zOffset, 0f, 0f, 7, 7, 256, 256)
        // top-right corner
        draw.drawTexture(texture, x + rowWidth + 7, y, zOffset, 169f, 0f, 7, 7, 256, 256)
        // bottom-right corner
        draw.drawTexture(
            texture, x + rowWidth + 7, y + yOffset, zOffset, 169f, 61f, 7, 7, 256,
            256
        )
        // bottom-left corner
        draw.drawTexture(texture, x, y + yOffset, zOffset, 0f, 61f, 7, 7, 256, 256)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    }
}
