package xyz.mesdev.flowtrails.trail;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;
import xyz.mesdev.flowtrails.config.TrailConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class TrailManager {
	private static final TrailManager INSTANCE = new TrailManager();

	private final Map<UUID, TrailStrand[]> trails = new HashMap<>();
	private final LimbPoser poser = new LimbPoser();
	private final Vec3[] limbs = new Vec3[LimbPoser.COUNT];
	private ClientLevel lastLevel;

	public static TrailManager get() {
		return INSTANCE;
	}

	public Map<UUID, TrailStrand[]> trails() {
		return trails;
	}

	public static double now() {
		return System.nanoTime() / 1.0e9;
	}

	public void update(Minecraft mc, float partialTick) {
		TrailConfig cfg = TrailConfig.get();
		ClientLevel level = mc.level;
		if (level != lastLevel) {
			trails.clear();
			lastLevel = level;
		}
		if (level == null || mc.player == null || !cfg.enabled) {
			trails.clear();
			return;
		}

		double now = now();
		double maxDist2 = (double) cfg.maxDistance * cfg.maxDistance;

		for (AbstractClientPlayer player : level.players()) {
			boolean self = player == mc.player;
			boolean wanted = self ? cfg.showSelf : cfg.showOthers;
			if (!wanted || player.isSpectator() || player.isInvisibleTo(mc.player)
					|| (!self && player.distanceToSqr(mc.player) > maxDist2)) {
				trails.remove(player.getUUID());
				continue;
			}

			TrailStrand[] strands = trails.computeIfAbsent(player.getUUID(), id -> newStrands());
			boolean posed = poser.pose(player, partialTick, limbs);
			for (int i = 0; i < LimbPoser.COUNT; i++) {
				boolean enabled = i <= LimbPoser.LEFT_HAND ? cfg.hands : cfg.feet;
				TrailStrand strand = strands[i];
				if (!enabled) {
					strand.clear();
				} else if (posed) {
					strand.sample(limbs[i].x, limbs[i].y, limbs[i].z, now);
				} else {
					strand.detach();
				}
			}
		}

		Iterator<Map.Entry<UUID, TrailStrand[]>> it = trails.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, TrailStrand[]> entry = it.next();
			boolean present = level.getPlayerByUUID(entry.getKey()) != null;
			boolean anyAlive = false;
			for (TrailStrand strand : entry.getValue()) {
				if (!present) strand.detach();
				strand.expire(now, cfg.lifetime);
				anyAlive |= !strand.isEmpty();
			}
			if (!anyAlive && !present) it.remove();
		}
	}

	private static TrailStrand[] newStrands() {
		TrailStrand[] strands = new TrailStrand[LimbPoser.COUNT];
		for (int i = 0; i < strands.length; i++) strands[i] = new TrailStrand();
		return strands;
	}
}
