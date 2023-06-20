package com.aleosiss.diageticui.client.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class DrawHelpers {
    // TODO: understand how this works
    public static void manipulateTexture(DrawContext draw, Identifier texture, float[] color, int x, int y, int zOffset, int invSize, int rowSize, int rowWidth, int xOffset, int yOffset) {
        int rowTexYPos = 7;
        var matrices = draw.getMatrices();

        draw.setShaderColor(color[0], color[1], color[2], 1.0f);

        // top side
        for (int size = rowSize; size > 0; size -= 9) {
            int s = Math.min(size, 9);

            draw.drawTexture(texture, x + xOffset, y, zOffset, 7, 0, s * 18, 7, 256, 256);
            xOffset += s * 18;
        }

        while (invSize > 0) {
            xOffset = 7;
            // left side
            draw.drawTexture(texture, x, y + yOffset, zOffset, 0, rowTexYPos, 7, 18, 256, 256);
            for (int rSize = rowSize; rSize > 0; rSize -= 9) {
                int s = Math.min(rSize, 9);

                // center
                draw.drawTexture(texture, x + xOffset, y + yOffset, zOffset, 7, rowTexYPos,
                        s * 18, 18, 256, 256);
                xOffset += s * 18;
            }
            // right side
            draw.drawTexture(texture, x + xOffset, y + yOffset, zOffset, 169, rowTexYPos, 7,
                    18, 256, 256);
            yOffset += 18;
            invSize -= rowSize;
            rowTexYPos = rowTexYPos >= 43 ? 7 : rowTexYPos + 18;
        }


        xOffset = 7;
        for (int size = rowSize; size > 0; size -= 9) {
            int s = Math.min(size, 9);

            // bottom side
            draw.drawTexture(texture, x + xOffset, y + yOffset, zOffset, 7, 61, s * 18, 7, 256,
                    256);
            xOffset += s * 18;
        }

        // top-left corner
        draw.drawTexture(texture, x, y, zOffset, 0, 0, 7, 7, 256, 256);
        // top-right corner
        draw.drawTexture(texture, x + rowWidth + 7, y, zOffset, 169, 0, 7, 7, 256, 256);
        // bottom-right corner
        draw.drawTexture(texture, x + rowWidth + 7, y + yOffset, zOffset, 169, 61, 7, 7, 256,
                256);
        // bottom-left corner
        draw.drawTexture(texture, x, y + yOffset, zOffset, 0, 61, 7, 7, 256, 256);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
