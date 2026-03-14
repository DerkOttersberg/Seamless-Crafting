package com.derk.easyinventorycrafter.client;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;

public final class EasyHighlightRenderer {

    private EasyHighlightRenderer() {
    }

    public static void renderBox(MatrixStack matrices, VertexConsumer consumer, Vec3d camPos, Box box, float alpha) {
        matrices.push();

        double cx = (box.minX + box.maxX) / 2.0 - camPos.x;
        double cy = (box.minY + box.maxY) / 2.0 - camPos.y;
        double cz = (box.minZ + box.maxZ) / 2.0 - camPos.z;
        matrices.translate(cx, cy, cz);

        float hw = (float) (box.maxX - box.minX) / 2.0f;
        float hh = (float) (box.maxY - box.minY) / 2.0f;
        float hd = (float) (box.maxZ - box.minZ) / 2.0f;

        MatrixStack.Entry matrixEntry = matrices.peek();

        // Gold color at 40% opacity.
        int color = ColorHelper.getArgb((int) (alpha * 0.4f * 255), 255, 215, 0);

        // -Z face
        consumer.vertex(matrixEntry, -hw, -hh, -hd).color(color);
        consumer.vertex(matrixEntry, -hw,  hh, -hd).color(color);
        consumer.vertex(matrixEntry,  hw,  hh, -hd).color(color);
        consumer.vertex(matrixEntry,  hw, -hh, -hd).color(color);
        // +Z face
        consumer.vertex(matrixEntry, -hw, -hh,  hd).color(color);
        consumer.vertex(matrixEntry,  hw, -hh,  hd).color(color);
        consumer.vertex(matrixEntry,  hw,  hh,  hd).color(color);
        consumer.vertex(matrixEntry, -hw,  hh,  hd).color(color);
        // -Y face
        consumer.vertex(matrixEntry, -hw, -hh, -hd).color(color);
        consumer.vertex(matrixEntry,  hw, -hh, -hd).color(color);
        consumer.vertex(matrixEntry,  hw, -hh,  hd).color(color);
        consumer.vertex(matrixEntry, -hw, -hh,  hd).color(color);
        // +Y face
        consumer.vertex(matrixEntry, -hw,  hh, -hd).color(color);
        consumer.vertex(matrixEntry, -hw,  hh,  hd).color(color);
        consumer.vertex(matrixEntry,  hw,  hh,  hd).color(color);
        consumer.vertex(matrixEntry,  hw,  hh, -hd).color(color);
        // -X face
        consumer.vertex(matrixEntry, -hw, -hh, -hd).color(color);
        consumer.vertex(matrixEntry, -hw, -hh,  hd).color(color);
        consumer.vertex(matrixEntry, -hw,  hh,  hd).color(color);
        consumer.vertex(matrixEntry, -hw,  hh, -hd).color(color);
        // +X face
        consumer.vertex(matrixEntry,  hw, -hh, -hd).color(color);
        consumer.vertex(matrixEntry,  hw,  hh, -hd).color(color);
        consumer.vertex(matrixEntry,  hw,  hh,  hd).color(color);
        consumer.vertex(matrixEntry,  hw, -hh,  hd).color(color);

        matrices.pop();
    }
}
