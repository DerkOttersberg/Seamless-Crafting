package com.derk.easyinventorycrafter.client;

import com.derk.easyinventorycrafter.EasyInventoryCrafterConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class EasyHighlightRenderer {
    private static final int FULL_BRIGHT = 15728880;

    private EasyHighlightRenderer() {
    }

    public static void renderBox(MatrixStack matrices, VertexConsumer consumer, Vec3d camPos, Box box, float alpha) {
        int highlightColor = EasyInventoryCrafterConfig.getHighlightColor();
        int color = ColorHelper.getArgb(
                (int)(alpha * EasyInventoryCrafterConfig.getHighlightOpacity() * 255),
                (highlightColor >> 16) & 0xFF,
                (highlightColor >> 8) & 0xFF,
                highlightColor & 0xFF
        );
        renderFilledBox(matrices, consumer, camPos, box, color);
    }

    public static void renderDistanceLabel(
        MinecraftClient client,
        MatrixStack matrices,
        VertexConsumerProvider consumers,
        Vec3d camPos,
        Box box,
        Vec3d playerPos,
        float alpha
    ) {
        TextRenderer textRenderer = client.textRenderer;
        Vec3d center = box.getCenter();
        Vec3d labelPos = new Vec3d(center.x, box.maxY + 0.1, center.z);
        double distance = playerPos.distanceTo(center);
        String label = String.format("%.1fm", distance);

        matrices.push();

        double cx = labelPos.x - camPos.x;
        double cy = labelPos.y - camPos.y;
        double cz = labelPos.z - camPos.z;
        matrices.translate(cx, cy, cz);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-client.gameRenderer.getCamera().getYaw()));
        matrices.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f textMatrix = matrices.peek().getPositionMatrix();
        float x = -textRenderer.getWidth(label) / 2.0f;
        int highlightColor = EasyInventoryCrafterConfig.getHighlightColor();
        int textColor = ColorHelper.getArgb((int) (alpha * 255), (highlightColor >> 16) & 0xFF, (highlightColor >> 8) & 0xFF, highlightColor & 0xFF);
        int backgroundColor = ColorHelper.getArgb((int) (alpha * 0.35f * 255), 0, 0, 0);
        textRenderer.draw(
            label,
            x,
            0.0f,
            textColor,
            false,
            textMatrix,
            consumers,
            TextRenderer.TextLayerType.SEE_THROUGH,
            backgroundColor,
            FULL_BRIGHT
        );

        matrices.pop();
    }

    private static void renderFilledBox(MatrixStack matrices, VertexConsumer consumer, Vec3d camPos, Box box, int color) {
        matrices.push();

        double cx = (box.minX + box.maxX) / 2.0 - camPos.x;
        double cy = (box.minY + box.maxY) / 2.0 - camPos.y;
        double cz = (box.minZ + box.maxZ) / 2.0 - camPos.z;
        matrices.translate(cx, cy, cz);

        float hw = (float) (box.maxX - box.minX) / 2.0f;
        float hh = (float) (box.maxY - box.minY) / 2.0f;
        float hd = (float) (box.maxZ - box.minZ) / 2.0f;

        MatrixStack.Entry matrixEntry = matrices.peek();

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
