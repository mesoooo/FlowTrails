package xyz.mesdev.flowtrails.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import xyz.mesdev.flowtrails.config.TrailConfig;

import java.util.function.Consumer;
import java.util.function.IntFunction;

public final class TrailConfigScreen extends OptionsSubScreen {
	public TrailConfigScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable("flowtrails.config.title"));
	}

	@Override
	protected void addOptions() {
		TrailConfig c = TrailConfig.get();

		list.addHeader(Component.translatable("flowtrails.config.section.general"));
		list.addSmall(
				bool("enabled", c.enabled, v -> c.enabled = v),
				bool("showOthers", c.showOthers, v -> c.showOthers = v),
				bool("showSelf", c.showSelf, v -> c.showSelf = v),
				bool("showSelfFirstPerson", c.showSelfFirstPerson, v -> c.showSelfFirstPerson = v),
				bool("hands", c.hands, v -> c.hands = v),
				bool("feet", c.feet, v -> c.feet = v),
				slider("maxDistance", 4, 256, c.maxDistance, v -> c.maxDistance = v, v -> v + " blocks"));

		list.addHeader(Component.translatable("flowtrails.config.section.look"));
		list.addSmall(
				slider("opacity", 5, 100, Math.round(c.opacity * 100), v -> c.opacity = v / 100f, v -> v + "%"),
				slider("width", 2, 50, Math.round(c.width / 0.005f), v -> c.width = v * 0.005f,
						v -> String.format("%.3f", v * 0.005f)),
				slider("lifetime", 3, 100, Math.round(c.lifetime / 0.05f), v -> c.lifetime = v * 0.05f,
						v -> String.format("%.2fs", v * 0.05f)),
				bool("glow", c.glow, v -> c.glow = v));

		list.addHeader(Component.translatable("flowtrails.config.section.color"));
		list.addSmall(
				slider("red", 0, 255, (c.color >> 16) & 0xFF, v -> c.color = withChannel(c.color, 16, v), String::valueOf),
				slider("green", 0, 255, (c.color >> 8) & 0xFF, v -> c.color = withChannel(c.color, 8, v), String::valueOf),
				slider("blue", 0, 255, c.color & 0xFF, v -> c.color = withChannel(c.color, 0, v), String::valueOf),
				bool("rainbow", c.rainbow, v -> c.rainbow = v),
				slider("rainbowSpeed", 1, 150, Math.round(c.rainbowSpeed / 0.02f), v -> c.rainbowSpeed = v * 0.02f,
						v -> String.format("%.2f/s", v * 0.02f)),
				bool("gradient", c.gradient, v -> c.gradient = v));

		list.addHeader(Component.translatable("flowtrails.config.section.endColor"));
		list.addSmall(
				slider("endRed", 0, 255, (c.endColor >> 16) & 0xFF, v -> c.endColor = withChannel(c.endColor, 16, v), String::valueOf),
				slider("endGreen", 0, 255, (c.endColor >> 8) & 0xFF, v -> c.endColor = withChannel(c.endColor, 8, v), String::valueOf),
				slider("endBlue", 0, 255, c.endColor & 0xFF, v -> c.endColor = withChannel(c.endColor, 0, v), String::valueOf));
	}

	@Override
	public void removed() {
		super.removed();
		TrailConfig.get().sanitize();
		TrailConfig.save();
	}

	private static int withChannel(int rgb, int shift, int value) {
		return (rgb & ~(0xFF << shift)) | ((value & 0xFF) << shift);
	}

	private static OptionInstance<Boolean> bool(String key, boolean value, Consumer<Boolean> setter) {
		return OptionInstance.createBoolean("flowtrails.config." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("flowtrails.config." + key + ".tooltip")),
				value, v -> {
					setter.accept(v);
					TrailConfig.get().sanitize();
				});
	}

	private static OptionInstance<Integer> slider(String key, int min, int max, int value,
												  Consumer<Integer> setter, IntFunction<String> format) {
		return new OptionInstance<>("flowtrails.config." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("flowtrails.config." + key + ".tooltip")),
				(caption, v) -> Options.genericValueLabel(caption, Component.literal(format.apply(v))),
				new OptionInstance.IntRange(min, max),
				Math.max(min, Math.min(max, value)),
				v -> {
					setter.accept(v);
					TrailConfig.get().sanitize();
				});
	}
}
