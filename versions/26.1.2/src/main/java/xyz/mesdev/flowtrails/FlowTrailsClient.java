package xyz.mesdev.flowtrails;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import xyz.mesdev.flowtrails.render.TrailRenderTypes;
import xyz.mesdev.flowtrails.render.TrailRenderer;

public final class FlowTrailsClient implements ClientModInitializer, ClientBridge {
	@Override
	public void onInitializeClient() {
		FlowTrails.init(this);

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(FlowTrails.MOD_ID, "main"));
		KeyMapping openSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				FlowTrails.KEY_SETTINGS, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));
		KeyMapping toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				FlowTrails.KEY_TOGGLE, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> FlowTrails.handleKeys(mc, openSettings, toggle));

		LevelRenderEvents.END_EXTRACTION.register(ctx -> {
			Minecraft mc = Minecraft.getInstance();
			boolean firstPerson = mc.options.getCameraType().isFirstPerson();
			float partialTick = ctx.deltaTracker().getGameTimeDeltaPartialTick(true);
			FlowTrails.RENDERER.build(mc, ctx.levelState().cameraRenderState.pos, firstPerson, partialTick);
		});

		LevelRenderEvents.COLLECT_SUBMITS.register(FlowTrailsClient::submit);
	}

	private static void submit(LevelRenderContext ctx) {
		TrailRenderer renderer = FlowTrails.RENDERER;
		if (!renderer.hasGeometry()) return;
		if (renderer.hasHalo()) {
			ctx.submitNodeCollector().submitCustomGeometry(ctx.poseStack(), TrailRenderTypes.GLOW, renderer::writeHalo);
		}
		ctx.submitNodeCollector().submitCustomGeometry(ctx.poseStack(),
				renderer.glow() ? TrailRenderTypes.GLOW : TrailRenderTypes.NORMAL, renderer::writeCore);
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
		if (mc.player != null) mc.player.sendOverlayMessage(message);
	}
}
