package com.derk.easyinventorycrafter.client;

import com.derk.easyinventorycrafter.mixin.RenderLayerMixin;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.OutputTarget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class EasyHighlightRenderLayer {
	private static final Identifier PIPELINE_ID = Identifier.of("easyinventorycrafter", "nearby_outline");
	private static final RenderPipeline OUTLINE_PIPELINE = RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
		.withLocation(PIPELINE_ID)
		.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.LINES)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withDepthWrite(false)
		.withCull(false)
		.withBlend(BlendFunction.TRANSLUCENT)
		.build();
	private static final RenderLayer OUTLINE = RenderLayerMixin.easyinventorycrafter$of(
		"easyinventorycrafter:nearby_outline",
		RenderSetup.builder(OUTLINE_PIPELINE)
			.translucent()
			.outputTarget(OutputTarget.MAIN_TARGET)
			.expectedBufferSize(256)
			.build()
	);

	private EasyHighlightRenderLayer() {
	}

	public static RenderLayer outline() {
		return OUTLINE;
	}
}