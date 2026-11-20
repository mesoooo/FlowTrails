package xyz.mesdev.flowtrails.trail;

public final class TrailStrand {
	private static final int CAPACITY = 512;
	private static final double MIN_SPACING = 0.02;
	private static final double TELEPORT_DISTANCE = 6.0;

	private final double[] xs = new double[CAPACITY];
	private final double[] ys = new double[CAPACITY];
	private final double[] zs = new double[CAPACITY];
	private final double[] times = new double[CAPACITY];
	private int head = -1;
	private int size;

	private double tipX, tipY, tipZ;
	private boolean hasTip;

	public boolean hasTip() { return hasTip; }
	public double tipX() { return tipX; }
	public double tipY() { return tipY; }
	public double tipZ() { return tipZ; }

	public void sample(double x, double y, double z, double now) {
		if (size > 0) {
			double dx = x - xs[head], dy = y - ys[head], dz = z - zs[head];
			double d2 = dx * dx + dy * dy + dz * dz;
			if (d2 > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
				clear();
			} else if (d2 < MIN_SPACING * MIN_SPACING) {
				setTip(x, y, z);
				return;
			}
		}
		head = (head + 1) % CAPACITY;
		xs[head] = x;
		ys[head] = y;
		zs[head] = z;
		times[head] = now;
		if (size < CAPACITY) size++;
		setTip(x, y, z);
	}

	private void setTip(double x, double y, double z) {
		tipX = x;
		tipY = y;
		tipZ = z;
		hasTip = true;
	}

	public void detach() {
		hasTip = false;
	}

	public void expire(double now, double lifetime) {
		while (size > 0 && now - time(size - 1) > lifetime) size--;
		if (size == 0) hasTip = false;
	}

	public void clear() {
		size = 0;
		head = -1;
		hasTip = false;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	private int idx(int i) {
		return Math.floorMod(head - i, CAPACITY);
	}

	public double x(int i) { return xs[idx(i)]; }
	public double y(int i) { return ys[idx(i)]; }
	public double z(int i) { return zs[idx(i)]; }
	public double time(int i) { return times[idx(i)]; }
}
