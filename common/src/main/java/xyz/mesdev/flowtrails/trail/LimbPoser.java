package xyz.mesdev.flowtrails.trail;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class LimbPoser {
	public static final int RIGHT_HAND = 0;
	public static final int LEFT_HAND = 1;
	public static final int RIGHT_FOOT = 2;
	public static final int LEFT_FOOT = 3;
	public static final int COUNT = 4;

	private static final float PI = (float) Math.PI;

	private final Quaternionf rot = new Quaternionf();
	private final Vector3f tmp = new Vector3f();

	public boolean pose(AbstractClientPlayer player, float partialTick, Vec3[] out) {
		if (player.isFallFlying() || player.isVisuallySwimming() || player.isAutoSpinAttack()
				|| player.hasPose(Pose.SLEEPING) || player.isPassenger()) {
			return false;
		}

		float walkPos = player.walkAnimation.position(partialTick);
		float walkSpeed = Math.min(player.walkAnimation.speed(partialTick), 1f);
		float age = player.tickCount + partialTick;
		float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
		float headPitch = player.getViewXRot(partialTick) * Mth.DEG_TO_RAD;
		float attack = player.getAttackAnim(partialTick);
		boolean crouch = player.isCrouching();

		float rArmX = Mth.cos(walkPos * 0.6662f + PI) * walkSpeed;
		float lArmX = Mth.cos(walkPos * 0.6662f) * walkSpeed;
		float rArmY = 0, lArmY = 0;
		float rArmZ = 0, lArmZ = 0;
		float rLegX = Mth.cos(walkPos * 0.6662f) * 1.4f * walkSpeed;
		float lLegX = Mth.cos(walkPos * 0.6662f + PI) * 1.4f * walkSpeed;

		boolean rightIsMain = player.getMainArm() == HumanoidArm.RIGHT;
		boolean mainHasItem = !player.getMainHandItem().isEmpty();
		boolean offHasItem = !player.getOffhandItem().isEmpty();
		boolean rightHasItem = rightIsMain ? mainHasItem : offHasItem;
		boolean leftHasItem = rightIsMain ? offHasItem : mainHasItem;
		if (rightHasItem) rArmX = rArmX * 0.5f - PI / 10f;
		if (leftHasItem) lArmX = lArmX * 0.5f - PI / 10f;

		float bodyY = 0;
		if (attack > 0) {
			boolean swingRight = (player.swingingArm == InteractionHand.MAIN_HAND) == rightIsMain;
			bodyY = Mth.sin(Mth.sqrt(attack) * (PI * 2f)) * 0.2f * (swingRight ? 1 : -1);
			float f = 1f - attack;
			f = f * f * f * f;
			float lift = Mth.sin((1f - f) * PI);
			float pitchTerm = Mth.sin(attack * PI) * -(headPitch - 0.7f) * 0.75f;
			if (swingRight) {
				rArmX -= lift * 1.2f + pitchTerm;
				rArmY += bodyY * 2f;
				rArmZ += Mth.sin(attack * PI) * -0.4f;
			} else {
				lArmX -= lift * 1.2f + pitchTerm;
				lArmY += bodyY * 2f;
				lArmZ -= Mth.sin(attack * PI) * -0.4f;
			}
		}
		rArmY += bodyY;
		lArmY += bodyY;

		float sway = Mth.cos(age * 0.09f) * 0.05f + 0.05f;
		float bob = Mth.sin(age * 0.067f) * 0.05f;
		rArmZ += sway;
		lArmZ -= sway;
		rArmX += bob;
		lArmX -= bob;

		float armY = 2f, legY = 12f, legZ = 0.1f;
		if (crouch) {
			rArmX += 0.4f;
			lArmX += 0.4f;
			armY = 5.2f;
			legY = 12.2f;
			legZ = 4f;
		}
		float rArmPx = -Mth.cos(bodyY) * 5f, rArmPz = Mth.sin(bodyY) * 5f;
		float lArmPx = Mth.cos(bodyY) * 5f, lArmPz = -Mth.sin(bodyY) * 5f;

		Vec3 base = player.getPosition(partialTick);
		if (crouch) base = base.add(0, -0.125, 0);
		float scale = player.getScale();
		float yawRad = (180f - bodyYaw) * Mth.DEG_TO_RAD;

		out[RIGHT_HAND] = toWorld(base, yawRad, scale, rArmPx, armY, rArmPz, rArmX, rArmY, rArmZ, -1f, 10f, 0f);
		out[LEFT_HAND] = toWorld(base, yawRad, scale, lArmPx, armY, lArmPz, lArmX, lArmY, lArmZ, 1f, 10f, 0f);
		out[RIGHT_FOOT] = toWorld(base, yawRad, scale, -1.9f, legY, legZ, rLegX, 0, 0, 0f, 11.5f, 0f);
		out[LEFT_FOOT] = toWorld(base, yawRad, scale, 1.9f, legY, legZ, lLegX, 0, 0, 0f, 11.5f, 0f);
		return true;
	}

	private Vec3 toWorld(Vec3 base, float yawRad, float scale,
						 float px, float py, float pz, float xRot, float yRot, float zRot,
						 float lx, float ly, float lz) {
		rot.rotationZYX(zRot, yRot, xRot);
		tmp.set(lx, ly, lz).rotate(rot).add(px, py, pz);

		float x = -tmp.x / 16f;
		float y = 1.501f - tmp.y / 16f;
		float z = tmp.z / 16f;
		float sin = Mth.sin(yawRad), cos = Mth.cos(yawRad);
		float wx = x * cos + z * sin;
		float wz = -x * sin + z * cos;
		return base.add(wx * scale, y * scale, wz * scale);
	}
}
