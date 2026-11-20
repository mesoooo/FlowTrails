package xyz.mesdev.flowtrails.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import xyz.mesdev.flowtrails.FlowTrails;

public final class TrailRenderTypes {
	private static RenderPipeline pipeline(String name, BlendFunction blend) {
		return RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
				.withLocation(Identifier.fromNamespaceAndPath(FlowTrails.MOD_ID, "pipeline/" + name))
				.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
				.withBlend(blend)
				.withCull(false)
				.withDepthWrite(false)
				.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
				.build();
	}

	private static RenderType type(String name, RenderPipeline pipeline) {
		return RenderType.create(FlowTrails.MOD_ID + "_" + name,
				RenderSetup.builder(pipeline).bufferSize(RenderType.TRANSIENT_BUFFER_SIZE).createRenderSetup());
	}

	public static final RenderType NORMAL = type("trail", pipeline("trail", BlendFunction.TRANSLUCENT));
	public static final RenderType GLOW = type("trail_glow", pipeline("trail_glow", BlendFunction.LIGHTNING));

	private TrailRenderTypes() {
	}
}
