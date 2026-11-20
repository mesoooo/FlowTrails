package xyz.mesdev.flowtrails.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
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
				.withColorTargetState(new ColorTargetState(blend))
				.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
				.withCull(false)
				.build();
	}

	private static RenderType type(String name, RenderPipeline pipeline) {
		return RenderType.create(FlowTrails.MOD_ID + "_" + name,
				RenderSetup.builder(pipeline).createRenderSetup());
	}

	public static final RenderType NORMAL = type("trail", pipeline("trail", BlendFunction.TRANSLUCENT));
	public static final RenderType GLOW = type("trail_glow", pipeline("trail_glow", BlendFunction.LIGHTNING));

	private TrailRenderTypes() {
	}
}
