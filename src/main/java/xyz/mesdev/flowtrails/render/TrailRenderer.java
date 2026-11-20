package xyz.mesdev.flowtrails.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import xyz.mesdev.flowtrails.config.TrailConfig;
import xyz.mesdev.flowtrails.trail.TrailManager;
import xyz.mesdev.flowtrails.trail.TrailStrand;

import java.util.Arrays;
import java.util.UUID;

public final class TrailRenderer {
	private static final float HALO_WIDTH = 4.0f;
	private static final float HALO_ALPHA = 0.16f;

	private static final double SPLINE_STEP = 0.04;
	private static final int MAX_SUBDIVISIONS = 8;

	private final VertexList core = new VertexList();
	private final VertexList halo = new VertexList();

	private double[] cx = new double[64], cy = new double[64], cz = new double[64], ct = new double[64];
	private double[] sx = new double[256], sy = new double[256], sz = new double[256], st = new double[256];

	public void build(Minecraft mc, Vec3 cam, boolean firstPerson, float partialTick) {
		core.clear();
		halo.clear();
		TrailManager manager = TrailManager.get();
		manager.update(mc, partialTick);

		TrailConfig cfg = TrailConfig.get();
		if (!cfg.enabled || mc.player == null) return;

		double now = TrailManager.now();
		UUID self = mc.player.getUUID();
		for (var entry : manager.trails().entrySet()) {
			if (firstPerson && !cfg.showSelfFirstPerson && entry.getKey().equals(self)) continue;
			for (TrailStrand strand : entry.getValue()) {
				buildStrand(strand, cam, now, cfg);
			}
		}
	}

	public void draw(PoseStack poseStack, MultiBufferSource consumers) {
		if (core.size == 0) return;
		TrailConfig cfg = TrailConfig.get();
		PoseStack.Pose pose = poseStack.last();
		RenderType coreType = cfg.glow ? TrailRenderTypes.GLOW : TrailRenderTypes.NORMAL;

		if (halo.size > 0) emit(consumers, TrailRenderTypes.GLOW, pose, halo);
		emit(consumers, coreType, pose, core);
	}

	private static void emit(MultiBufferSource consumers, RenderType type, PoseStack.Pose pose, VertexList list) {
		VertexConsumer vc = consumers.getBuffer(type);
		float[] d = list.data;
		int[] c = list.colors;
		for (int v = 0; v < list.size; v++) {
			int i = v * 3;
			vc.addVertex(pose, d[i], d[i + 1], d[i + 2]).setColor(c[v]);
		}
		if (consumers instanceof MultiBufferSource.BufferSource source) source.endBatch(type);
	}

	private void buildStrand(TrailStrand strand, Vec3 cam, double now, TrailConfig cfg) {
		int stored = strand.size();
		int count = 0;
		ensureControl(stored + 1);
		double life = cfg.lifetime;

		if (strand.isEmpty()) return;
		if (strand.hasTip()) {
			cx[count] = strand.tipX() - cam.x;
			cy[count] = strand.tipY() - cam.y;
			cz[count] = strand.tipZ() - cam.z;
			ct[count] = 0;
			count++;
		}
		for (int i = 0; i < stored; i++) {
			double x = strand.x(i) - cam.x, y = strand.y(i) - cam.y, z = strand.z(i) - cam.z;
			if (count > 0 && near(x, y, z, count - 1)) continue;
			cx[count] = x;
			cy[count] = y;
			cz[count] = z;
			ct[count] = Math.min(1.0, (now - strand.time(i)) / life);
			count++;
		}
		if (count < 2) return;

		int smooth = spline(count);
		if (smooth < 2) return;
		ribbon(smooth, now, cfg);
	}

	private boolean near(double x, double y, double z, int j) {
		double dx = x - cx[j], dy = y - cy[j], dz = z - cz[j];
		return dx * dx + dy * dy + dz * dz < 1.0e-6;
	}

	private int spline(int count) {
		int out = 0;
		for (int k = 0; k < count - 1; k++) {
			int k0 = Math.max(k - 1, 0), k2 = k + 1, k3 = Math.min(k + 2, count - 1);
			double dx = cx[k2] - cx[k], dy = cy[k2] - cy[k], dz = cz[k2] - cz[k];
			double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
			int sub = Mth.clamp((int) Math.ceil(len / SPLINE_STEP), 1, MAX_SUBDIVISIONS);
			ensureSmooth(out + sub + 1);
			for (int s = 0; s < sub; s++) {
				double u = (double) s / sub;
				sx[out] = catmull(cx[k0], cx[k], cx[k2], cx[k3], u);
				sy[out] = catmull(cy[k0], cy[k], cy[k2], cy[k3], u);
				sz[out] = catmull(cz[k0], cz[k], cz[k2], cz[k3], u);
				st[out] = ct[k] + (ct[k2] - ct[k]) * u;
				out++;
			}
		}
		ensureSmooth(out + 1);
		sx[out] = cx[count - 1];
		sy[out] = cy[count - 1];
		sz[out] = cz[count - 1];
		st[out] = ct[count - 1];
		return out + 1;
	}

	private static double catmull(double p0, double p1, double p2, double p3, double u) {
		double u2 = u * u, u3 = u2 * u;
		return 0.5 * ((2 * p1) + (-p0 + p2) * u + (2 * p0 - 5 * p1 + 4 * p2 - p3) * u2 + (-p0 + 3 * p1 - 3 * p2 + p3) * u3);
	}

	private void ribbon(int n, double now, TrailConfig cfg) {
		float prevLx = 0, prevLy = 0, prevLz = 0, prevRx = 0, prevRy = 0, prevRz = 0;
		float prevCx = 0, prevCy = 0, prevCz = 0;
		int prevCore = 0, prevEdge = 0, prevHaloCore = 0, prevHaloEdge = 0;
		float prevHw = 0;
		boolean havePrev = false;

		for (int i = 0; i < n; i++) {
			int a = Math.max(i - 1, 0), b = Math.min(i + 1, n - 1);
			double tx = sx[b] - sx[a], ty = sy[b] - sy[a], tz = sz[b] - sz[a];

			double px = sx[i], py = sy[i], pz = sz[i];
			double ox = ty * -pz - tz * -py;
			double oy = tz * -px - tx * -pz;
			double oz = tx * -py - ty * -px;
			double ol = Math.sqrt(ox * ox + oy * oy + oz * oz);
			if (ol < 1.0e-9) continue;

			double t = st[i];
			float fade = (float) Math.pow(1.0 - t, 1.6);
			float hw = (float) (cfg.width * 0.5 * (0.3 + 0.7 * (1.0 - t)));
			ox /= ol;
			oy /= ol;
			oz /= ol;

			int rgb = colorAt(t, now, cfg);
			float alpha = cfg.opacity * fade;
			int coreCol = argb(alpha, rgb);
			int edgeCol = rgb;
			int haloCore = argb(alpha * HALO_ALPHA, rgb);

			float cxf = (float) px, cyf = (float) py, czf = (float) pz;
			float lx = (float) (px + ox * hw), ly = (float) (py + oy * hw), lz = (float) (pz + oz * hw);
			float rx = (float) (px - ox * hw), ry = (float) (py - oy * hw), rz = (float) (pz - oz * hw);

			if (havePrev) {
				core.quad(prevLx, prevLy, prevLz, prevEdge, prevCx, prevCy, prevCz, prevCore,
						cxf, cyf, czf, coreCol, lx, ly, lz, edgeCol);
				core.quad(prevCx, prevCy, prevCz, prevCore, prevRx, prevRy, prevRz, prevEdge,
						rx, ry, rz, edgeCol, cxf, cyf, czf, coreCol);
				if (cfg.glow) {
					float h0 = prevHw * HALO_WIDTH, h1 = hw * HALO_WIDTH;

					float pdx = (prevLx - prevCx) / Math.max(prevHw, 1e-6f);
					float pdy = (prevLy - prevCy) / Math.max(prevHw, 1e-6f);
					float pdz = (prevLz - prevCz) / Math.max(prevHw, 1e-6f);
					halo.quad(prevCx + pdx * h0, prevCy + pdy * h0, prevCz + pdz * h0, prevHaloEdge,
							prevCx, prevCy, prevCz, prevHaloCore,
							cxf, cyf, czf, haloCore,
							(float) (px + ox * h1), (float) (py + oy * h1), (float) (pz + oz * h1), edgeCol);
					halo.quad(prevCx, prevCy, prevCz, prevHaloCore,
							prevCx - pdx * h0, prevCy - pdy * h0, prevCz - pdz * h0, prevHaloEdge,
							(float) (px - ox * h1), (float) (py - oy * h1), (float) (pz - oz * h1), edgeCol,
							cxf, cyf, czf, haloCore);
				}
			}

			prevLx = lx; prevLy = ly; prevLz = lz;
			prevRx = rx; prevRy = ry; prevRz = rz;
			prevCx = cxf; prevCy = cyf; prevCz = czf;
			prevCore = coreCol;
			prevEdge = edgeCol;
			prevHaloCore = haloCore;
			prevHaloEdge = edgeCol;
			prevHw = hw;
			havePrev = true;
		}
	}

	private static int colorAt(double t, double now, TrailConfig cfg) {
		if (cfg.rainbow) {
			float hue = (float) ((now * cfg.rainbowSpeed + t * 0.6) % 1.0);
			return Mth.hsvToRgb(hue, 0.7f, 1f) & 0xFFFFFF;
		}
		if (cfg.gradient) return lerpRgb(cfg.color, cfg.endColor, (float) t);
		return cfg.color;
	}

	private static int lerpRgb(int a, int b, float t) {
		int r = (int) Mth.lerp(t, (a >> 16) & 0xFF, (b >> 16) & 0xFF);
		int g = (int) Mth.lerp(t, (a >> 8) & 0xFF, (b >> 8) & 0xFF);
		int bl = (int) Mth.lerp(t, a & 0xFF, b & 0xFF);
		return (r << 16) | (g << 8) | bl;
	}

	private static int argb(float alpha, int rgb) {
		return (Mth.clamp((int) (alpha * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF);
	}

	private void ensureControl(int n) {
		if (cx.length >= n) return;
		int len = Math.max(n, cx.length * 2);
		cx = Arrays.copyOf(cx, len);
		cy = Arrays.copyOf(cy, len);
		cz = Arrays.copyOf(cz, len);
		ct = Arrays.copyOf(ct, len);
	}

	private void ensureSmooth(int n) {
		if (sx.length >= n) return;
		int len = Math.max(n, sx.length * 2);
		sx = Arrays.copyOf(sx, len);
		sy = Arrays.copyOf(sy, len);
		sz = Arrays.copyOf(sz, len);
		st = Arrays.copyOf(st, len);
	}

	private static final class VertexList {
		float[] data = new float[3 * 1024];
		int[] colors = new int[1024];
		int size;

		void clear() {
			size = 0;
		}

		void quad(float x0, float y0, float z0, int c0, float x1, float y1, float z1, int c1,
				  float x2, float y2, float z2, int c2, float x3, float y3, float z3, int c3) {
			vertex(x0, y0, z0, c0);
			vertex(x1, y1, z1, c1);
			vertex(x2, y2, z2, c2);
			vertex(x3, y3, z3, c3);
		}

		private void vertex(float x, float y, float z, int argb) {
			if (size == colors.length) {
				colors = Arrays.copyOf(colors, size * 2);
				data = Arrays.copyOf(data, size * 6);
			}
			int i = size * 3;
			data[i] = x;
			data[i + 1] = y;
			data[i + 2] = z;
			colors[size] = argb;
			size++;
		}
	}
}
