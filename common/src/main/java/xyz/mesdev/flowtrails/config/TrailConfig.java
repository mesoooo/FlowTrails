package xyz.mesdev.flowtrails.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import xyz.mesdev.flowtrails.FlowTrails;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TrailConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("flowtrails.json");

	private static TrailConfig instance = new TrailConfig();

	public boolean enabled = true;
	public boolean showSelf = true;
	public boolean showSelfFirstPerson = false;
	public boolean showOthers = true;
	public int maxDistance = 64;

	public boolean hands = true;
	public boolean feet = true;

	public int color = 0xFFFFFF;
	public int endColor = 0x9F7BFF;
	public boolean gradient = false;
	public boolean rainbow = false;
	public float rainbowSpeed = 0.35f;
	public float opacity = 0.75f;
	public float width = 0.045f;
	public float lifetime = 0.9f;
	public boolean glow = true;

	public static TrailConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				TrailConfig loaded = GSON.fromJson(reader, TrailConfig.class);
				if (loaded != null) instance = loaded;
			} catch (Exception e) {
				FlowTrails.LOGGER.warn("Couldn't read {}, using defaults", PATH, e);
			}
		}
		instance.sanitize();
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			FlowTrails.LOGGER.warn("Couldn't save {}", PATH, e);
		}
	}

	public void sanitize() {
		color &= 0xFFFFFF;
		endColor &= 0xFFFFFF;
		opacity = clamp(opacity, 0.05f, 1f);
		width = clamp(width, 0.01f, 0.25f);
		lifetime = clamp(lifetime, 0.15f, 5f);
		rainbowSpeed = clamp(rainbowSpeed, 0.02f, 3f);
		maxDistance = Math.max(4, Math.min(256, maxDistance));
	}

	private static float clamp(float v, float min, float max) {
		return Float.isNaN(v) ? min : Math.max(min, Math.min(max, v));
	}
}
