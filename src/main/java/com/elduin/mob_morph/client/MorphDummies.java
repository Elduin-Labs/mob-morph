package com.elduin.mob_morph.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * One spare mob per type, kept off to the side and never added to the world.
 *
 * When a morphed player needs drawing we move this stand-in onto them, copy
 * their pose, and let Minecraft render it exactly as it would render the real
 * mob. That way we get the right model, texture and animations for free.
 */
public final class MorphDummies {

	private static final Map<EntityType<?>, Entity> CACHE = new HashMap<>();
	private static ClientLevel cachedLevel;

	private MorphDummies() {
	}

	public static Entity posedLike(EntityType<?> type, Player player) {
		ClientLevel level = Minecraft.getInstance().level;

		if (level == null || type == null) {
			return null;
		}

		if (level != cachedLevel) {
			CACHE.clear();
			cachedLevel = level;
		}

		Entity dummy = CACHE.computeIfAbsent(type, t -> t.create(level, EntitySpawnReason.LOAD));

		if (dummy == null) {
			return null;
		}

		copyPose(dummy, player);
		return dummy;
	}

	private static void copyPose(Entity dummy, Player player) {
		dummy.setPos(player.getX(), player.getY(), player.getZ());
		dummy.xo = player.xo;
		dummy.yo = player.yo;
		dummy.zo = player.zo;

		dummy.setYRot(player.getYRot());
		dummy.setXRot(player.getXRot());
		dummy.yRotO = player.yRotO;
		dummy.xRotO = player.xRotO;

		dummy.tickCount = player.tickCount;
		dummy.setOnGround(player.onGround());
		dummy.setDeltaMovement(player.getDeltaMovement());
		dummy.setInvisible(player.isInvisible());
		dummy.setPose(player.getPose());
		dummy.setSprinting(player.isSprinting());
		dummy.setSwimming(player.isSwimming());
		dummy.setShiftKeyDown(player.isShiftKeyDown());

		if (dummy instanceof LivingEntity living) {
			living.yHeadRot = player.yHeadRot;
			living.yHeadRotO = player.yHeadRotO;
			living.yBodyRot = player.yBodyRot;
			living.yBodyRotO = player.yBodyRotO;

			// so legs and wings actually move when the player moves
			living.walkAnimation.setSpeed(player.walkAnimation.speed());
			living.hurtTime = player.hurtTime;
			living.deathTime = player.deathTime;
			living.setSwimming(player.isSwimming());
		}
	}

	public static void clear() {
		CACHE.clear();
		cachedLevel = null;
	}
}
