package xyz.mesdev.flowtrails.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import xyz.mesdev.flowtrails.FlowTrails;

public final class TrailRenderTypes {
	private static RenderType type(String name, RenderStateShard.TransparencyStateShard transparency) {
		return RenderType.create(FlowTrails.MOD_ID + "_" + name, DefaultVertexFormat.POSITION_COLOR,
				VertexFormat.Mode.QUADS, 262144, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
						.setTransparencyState(transparency)
						.setCullState(RenderStateShard.NO_CULL)
						.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderStateShard.COLOR_WRITE)
						.createCompositeState(false));
	}

	public static final RenderType NORMAL = type("trail", RenderStateShard.TRANSLUCENT_TRANSPARENCY);
	public static final RenderType GLOW = type("trail_glow", RenderStateShard.LIGHTNING_TRANSPARENCY);

	private TrailRenderTypes() {
	}
}
