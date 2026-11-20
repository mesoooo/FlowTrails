package xyz.mesdev.flowtrails;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import xyz.mesdev.flowtrails.render.TrailRenderTypes;
import xyz.mesdev.flowtrails.render.TrailRenderer;

public final class FlowTrailsClient implements ClientModInitializer, ClientBridge {
	@Override
	public void onInitializeClient() {
		FlowTrails.init(this);

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(FlowTrails.MOD_ID, "main"));
		KeyMapping openSettings = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				FlowTrails.KEY_SETTINGS, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));
		KeyMapping toggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				FlowTrails.KEY_TOGGLE, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> FlowTrails.handleKeys(mc, openSettings, toggle));

		WorldRenderEvents.END_EXTRACTION.register(ctx -> {
			Minecraft mc = Minecraft.getInstance();
			boolean firstPerson = mc.options.getCameraType().isFirstPerson();
			float partialTick = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
			FlowTrails.RENDERER.build(mc, ctx.worldState().cameraRenderState.pos, firstPerson, partialTick);
		});

		WorldRenderEvents.END_MAIN.register(ctx -> draw(ctx.matrices(), ctx.consumers()));
	}

	private static void draw(PoseStack poseStack, MultiBufferSource consumers) {
		TrailRenderer renderer = FlowTrails.RENDERER;
		if (!renderer.hasGeometry()) return;
		PoseStack.Pose pose = poseStack.last();
		if (renderer.hasHalo()) {
			renderer.writeHalo(pose, consumers.getBuffer(TrailRenderTypes.GLOW));
			flush(consumers, TrailRenderTypes.GLOW);
		}
		RenderType core = renderer.glow() ? TrailRenderTypes.GLOW : TrailRenderTypes.NORMAL;
		renderer.writeCore(pose, consumers.getBuffer(core));
		flush(consumers, core);
	}

	private static void flush(MultiBufferSource consumers, RenderType type) {
		if (consumers instanceof MultiBufferSource.BufferSource source) source.endBatch(type);
	}

	@Override
	public Screen currentScreen(Minecraft mc) {
		return mc.screen;
	}

	@Override
	public void openScreen(Minecraft mc, Screen screen) {
		mc.setScreen(screen);
	}

	@Override
	public void showStatus(Minecraft mc, Component message) {
		if (mc.player != null) mc.player.displayClientMessage(message, true);
	}
}
