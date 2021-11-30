package com.aleosiss.diageticui.client.render.util;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;

public class DrawHelpers {
    // TODO: understand how this works
    public static void manipulateTexture(MatrixStack matrices, int x, int y, int zOffset, int invSize, int rowSize, int rowWidth, int xOffset, int yOffset) {
        int rowTexYPos = 7;

        // top side
        for (int size = rowSize; size > 0; size -= 9) {
            int s = Math.min(size, 9);

            DrawableHelper.drawTexture(matrices, x + xOffset, y, zOffset, 7, 0, s * 18, 7, 256, 256);
            xOffset += s * 18;
        }

        while (invSize > 0) {
            xOffset = 7;
            // left side
            DrawableHelper.drawTexture(matrices, x, y + yOffset, zOffset, 0, rowTexYPos, 7, 18, 256, 256);
            for (int rSize = rowSize; rSize > 0; rSize -= 9) {
                int s = Math.min(rSize, 9);

                // center
                DrawableHelper.drawTexture(matrices, x + xOffset, y + yOffset, zOffset, 7, rowTexYPos,
                        s * 18, 18, 256, 256);
                xOffset += s * 18;
            }
            // right side
            DrawableHelper.drawTexture(matrices, x + xOffset, y + yOffset, zOffset, 169, rowTexYPos, 7,
                    18, 256, 256);
            yOffset += 18;
            invSize -= rowSize;
            rowTexYPos = rowTexYPos >= 43 ? 7 : rowTexYPos + 18;
        }


        xOffset = 7;
        for (int size = rowSize; size > 0; size -= 9) {
            int s = Math.min(size, 9);

            // bottom side
            DrawableHelper.drawTexture(matrices, x + xOffset, y + yOffset, zOffset, 7, 61, s * 18, 7, 256,
                    256);
            xOffset += s * 18;
        }

        // top-left corner
        DrawableHelper.drawTexture(matrices, x, y, zOffset, 0, 0, 7, 7, 256, 256);
        // top-right corner
        DrawableHelper.drawTexture(matrices, x + rowWidth + 7, y, zOffset, 169, 0, 7, 7, 256, 256);
        // bottom-right corner
        DrawableHelper.drawTexture(matrices, x + rowWidth + 7, y + yOffset, zOffset, 169, 61, 7, 7, 256,
                256);
        // bottom-left corner
        DrawableHelper.drawTexture(matrices, x, y + yOffset, zOffset, 0, 61, 7, 7, 256, 256);
    }
}
