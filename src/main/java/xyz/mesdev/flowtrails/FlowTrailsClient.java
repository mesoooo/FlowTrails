package xyz.mesdev.flowtrails;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.mesdev.flowtrails.config.TrailConfig;
import xyz.mesdev.flowtrails.render.TrailRenderer;
import xyz.mesdev.flowtrails.screen.TrailConfigScreen;

public final class FlowTrailsClient implements ClientModInitializer {
	public static final String MOD_ID = "flowtrails";
	public static final String AUTHOR = "mesdev";
	public static final String SOURCE = "https://github.com/mesoooo/FlowTrails";
	public static final Logger LOGGER = LoggerFactory.getLogger("FlowTrails");

	private static final TrailRenderer RENDERER = new TrailRenderer();

	@Override
	public void onInitializeClient() {
		TrailConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		KeyMapping openSettings = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.flowtrails.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));
		KeyMapping toggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.flowtrails.toggle", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (openSettings.consumeClick()) {
				mc.setScreen(new TrailConfigScreen(mc.screen));
			}
			while (toggle.consumeClick()) {
				TrailConfig cfg = TrailConfig.get();
				cfg.enabled = !cfg.enabled;
				TrailConfig.save();
				if (mc.player != null) {
					mc.player.displayClientMessage(Component.translatable(
							cfg.enabled ? "flowtrails.toggle.on" : "flowtrails.toggle.off"), true);
				}
			}
		});

		WorldRenderEvents.END_EXTRACTION.register(ctx -> {
			Minecraft mc = Minecraft.getInstance();
			boolean firstPerson = mc.options.getCameraType().isFirstPerson();
			float partialTick = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
			RENDERER.build(mc, ctx.worldState().cameraRenderState.pos, firstPerson, partialTick);
		});

		WorldRenderEvents.END_MAIN.register(ctx -> RENDERER.draw(ctx.matrices(), ctx.consumers()));

		LOGGER.info("FlowTrails by {} loaded ({})", AUTHOR, SOURCE);
	}
}
