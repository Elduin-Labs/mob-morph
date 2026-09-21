package com.elduin.mob_morph.client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.elduin.mob_morph.morph.Morphs;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

/** What the client believes: who looks like what, and what I have unlocked. */
public final class ClientMorphs {

	private static final Map<UUID, EntityType<?>> MORPHS = new ConcurrentHashMap<>();
	private static final Set<Identifier> MY_UNLOCKS = new LinkedHashSet<>();

	private ClientMorphs() {
	}

	public static void setMorph(UUID player, EntityType<?> type) {
		if (type == null) {
			MORPHS.remove(player);
		} else {
			MORPHS.put(player, type);
		}
	}

	public static EntityType<?> morphOf(UUID player) {
		return MORPHS.get(player);
	}

	public static void setUnlocks(Set<Identifier> ids) {
		MY_UNLOCKS.clear();
		MY_UNLOCKS.addAll(ids);
	}

	/** Unlocked mobs, as entity types, in the order they were unlocked. */
	public static List<EntityType<?>> unlockedTypes() {
		List<EntityType<?>> types = new ArrayList<>();

		for (Identifier id : MY_UNLOCKS) {
			EntityType<?> type = Morphs.byId(id);

			if (type != null) {
				types.add(type);
			}
		}

		return types;
	}

	public static void clear() {
		MORPHS.clear();
		MY_UNLOCKS.clear();
	}
}
