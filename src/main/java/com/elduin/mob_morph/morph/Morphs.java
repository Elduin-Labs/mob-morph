package com.elduin.mob_morph.morph;

import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** What you are allowed to turn into, and what that lets you do. */
public final class Morphs {

	/** Morphing into one of these lets you fly. */
	private static final Set<EntityType<?>> CAN_FLY = Set.of(
			EntityType.BLAZE,
			EntityType.GHAST,
			EntityType.BEE
	);

	private Morphs() {
	}

	/** You unlock a mob by killing one, so anything alive that isn't a player counts. */
	public static boolean isMorphable(Entity entity) {
		return entity instanceof LivingEntity
				&& !(entity instanceof net.minecraft.world.entity.player.Player)
				&& isMorphable(entity.getType());
	}

	public static boolean isMorphable(EntityType<?> type) {
		return type != EntityType.PLAYER;
	}

	public static boolean canFly(EntityType<?> type) {
		return type != null && CAN_FLY.contains(type);
	}

	public static Identifier idOf(EntityType<?> type) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(type);
	}

	public static EntityType<?> byId(Identifier id) {
		if (id == null) {
			return null;
		}

		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
		return isMorphable(type) ? type : null;
	}
}
