package com.derk.easyinventorycrafter.client;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;

public final class EasyHighlightRenderer {

    private EasyHighlightRenderer() {
    }

    public static void renderOutlineBox(MatrixStack matrices, VertexConsumer consumer, Vec3d camPos, Box box, float alpha) {
        matrices.push();

        double cx = (box.minX + box.maxX) / 2.0 - camPos.x;
        double cy = (box.minY + box.maxY) / 2.0 - camPos.y;
        double cz = (box.minZ + box.maxZ) / 2.0 - camPos.z;
        matrices.translate(cx, cy, cz);

        float hw = (float) (box.maxX - box.minX) / 2.0f;
        float hh = (float) (box.maxY - box.minY) / 2.0f;
        float hd = (float) (box.maxZ - box.minZ) / 2.0f;

        MatrixStack.Entry matrixEntry = matrices.peek();

        int color = ColorHelper.getArgb((int) (alpha * 0.9f * 255), 255, 215, 0);

        addLine(consumer, matrixEntry, -hw, -hh, -hd, -hw,  hh, -hd, color);
        addLine(consumer, matrixEntry,  hw, -hh, -hd,  hw,  hh, -hd, color);
        addLine(consumer, matrixEntry, -hw, -hh,  hd, -hw,  hh,  hd, color);
        addLine(consumer, matrixEntry,  hw, -hh,  hd,  hw,  hh,  hd, color);

        addLine(consumer, matrixEntry, -hw, -hh, -hd,  hw, -hh, -hd, color);
        addLine(consumer, matrixEntry, -hw, -hh,  hd,  hw, -hh,  hd, color);
        addLine(consumer, matrixEntry, -hw,  hh, -hd,  hw,  hh, -hd, color);
        addLine(consumer, matrixEntry, -hw,  hh,  hd,  hw,  hh,  hd, color);

        addLine(consumer, matrixEntry, -hw, -hh, -hd, -hw, -hh,  hd, color);
        addLine(consumer, matrixEntry,  hw, -hh, -hd,  hw, -hh,  hd, color);
        addLine(consumer, matrixEntry, -hw,  hh, -hd, -hw,  hh,  hd, color);
        addLine(consumer, matrixEntry,  hw,  hh, -hd,  hw,  hh,  hd, color);

        matrices.pop();
    }

    private static void addLine(VertexConsumer consumer, MatrixStack.Entry matrixEntry,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                int color) {
        consumer.vertex(matrixEntry, x1, y1, z1).color(color);
        consumer.vertex(matrixEntry, x2, y2, z2).color(color);
    }
}
