package com.aleosiss.diageticui.client.render.util;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;

public class DrawHelpers {
    // TODO: understand how this works
    public static void manipulateTexture(int posX, int posY, BufferBuilder builder, double zOffset, int invSize, int rowSize, int rowWidth, int xOffset, int yOffset) {
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

    public static void blitZOffset(BufferBuilder builder, int x, int y, int u, int v, int w, int h, double zOffset) {
        builder.vertex(x, y + h, zOffset).texture(u * 0.00390625f, (v + h) * 0.00390625f).next();
        builder.vertex(x + w, y + h, zOffset).texture((u + w) * 0.00390625f, (v + h) * 0.00390625f).next();
        builder.vertex(x + w, y, zOffset).texture((u + w) * 0.00390625f, (v + 0) * 0.00390625f).next();
        builder.vertex(x, y, zOffset).texture(u * 0.00390625f, v * 0.00390625f).next();
    }

}
