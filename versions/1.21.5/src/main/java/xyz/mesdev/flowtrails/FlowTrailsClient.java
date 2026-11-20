package xyz.mesdev.flowtrails;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import xyz.mesdev.flowtrails.render.TrailRenderTypes;
import xyz.mesdev.flowtrails.render.TrailRenderer;

public final class FlowTrailsClient implements ClientModInitializer, ClientBridge {
	@Override
	public void onInitializeClient() {
		FlowTrails.init(this);

		KeyMapping openSettings = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				FlowTrails.KEY_SETTINGS, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, FlowTrails.KEY_CATEGORY));
		KeyMapping toggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				FlowTrails.KEY_TOGGLE, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), FlowTrails.KEY_CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> FlowTrails.handleKeys(mc, openSettings, toggle));

		WorldRenderEvents.AFTER_TRANSLUCENT.register(FlowTrailsClient::render);
	}

	private static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		TrailRenderer renderer = FlowTrails.RENDERER;
		boolean firstPerson = mc.options.getCameraType().isFirstPerson();
		float partialTick = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
		renderer.build(mc, ctx.camera().getPosition(), firstPerson, partialTick);
		if (!renderer.hasGeometry()) return;

		MultiBufferSource consumers = ctx.consumers();
		PoseStack.Pose pose = new PoseStack().last();
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
