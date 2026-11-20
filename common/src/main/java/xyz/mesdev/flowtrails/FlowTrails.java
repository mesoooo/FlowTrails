package xyz.mesdev.flowtrails;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.mesdev.flowtrails.config.TrailConfig;
import xyz.mesdev.flowtrails.render.TrailRenderer;
import xyz.mesdev.flowtrails.screen.TrailConfigScreen;

public final class FlowTrails {
	public static final String MOD_ID = "flowtrails";
	public static final String AUTHOR = "mesdev";
	public static final String SOURCE = "https://github.com/mesoooo/FlowTrails";
	public static final Logger LOGGER = LoggerFactory.getLogger("FlowTrails");
	public static final TrailRenderer RENDERER = new TrailRenderer();

	public static final String KEY_CATEGORY = "key.category.flowtrails.main";
	public static final String KEY_SETTINGS = "key.flowtrails.settings";
	public static final String KEY_TOGGLE = "key.flowtrails.toggle";

	private FlowTrails() {
	}

	private static ClientBridge bridge;

	public static void init(ClientBridge clientBridge) {
		bridge = clientBridge;
		TrailConfig.load();
		LOGGER.info("FlowTrails by {} loaded ({})", AUTHOR, SOURCE);
	}

	public static void handleKeys(Minecraft mc, KeyMapping openSettings, KeyMapping toggle) {
		while (openSettings.consumeClick()) {
			bridge.openScreen(mc, new TrailConfigScreen(bridge.currentScreen(mc)));
		}
		while (toggle.consumeClick()) {
			TrailConfig cfg = TrailConfig.get();
			cfg.enabled = !cfg.enabled;
			TrailConfig.save();
			if (mc.player != null) {
				bridge.showStatus(mc, Component.translatable(
						cfg.enabled ? "flowtrails.toggle.on" : "flowtrails.toggle.off"));
			}
		}
	}
}
