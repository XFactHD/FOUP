package io.github.xfacthd.foup.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public final class GhostVertexConsumer extends VertexConsumerWrapper {
    private final int alpha;

    public GhostVertexConsumer(VertexConsumer wrapped, int alpha) {
        super(wrapped);
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer setColor(int packedColor) {
        int alpha = (ARGB.alpha(packedColor) * this.alpha) / 0xFF;
        return parent.setColor(ARGB.color(alpha, ARGB.red(packedColor), ARGB.green(packedColor), ARGB.blue(packedColor)));
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return parent.setColor(red, green, blue, (alpha * this.alpha) / 0xFF);
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
        int alpha = (ARGB.alpha(color) * this.alpha) / 0xFF;
        color = ARGB.color(alpha, ARGB.red(color), ARGB.green(color), ARGB.blue(color));
        parent.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
    }
}
