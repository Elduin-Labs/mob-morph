package com.elduin.mob_morph.morph;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.elduin.mob_morph.MobMorph;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Who has unlocked what, and who is currently morphed into what.
 *
 * The server owns all of this. Unlocks are saved with the world; the morph a
 * player is wearing is deliberately not saved, so everybody starts a session
 * as themselves.
 */
public final class MorphState {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "mob_morph_unlocks.json";

	private static final Map<UUID, Set<Identifier>> UNLOCKED = new HashMap<>();
	private static final Map<UUID, EntityType<?>> MORPHED = new HashMap<>();

	private MorphState() {
	}

	// ------------------------------------------------------------- unlocks

	public static Set<Identifier> unlocked(UUID player) {
		return UNLOCKED.computeIfAbsent(player, key -> new LinkedHashSet<>());
	}

	/** @return true if this was a mob they had not killed before. */
	public static boolean unlock(UUID player, EntityType<?> type) {
		Identifier id = Morphs.idOf(type);
		return id != null && unlocked(player).add(id);
	}

	public static boolean hasUnlocked(UUID player, EntityType<?> type) {
		return unlocked(player).contains(Morphs.idOf(type));
	}

	// -------------------------------------------------------------- morphs

	/** @return the mob this player looks like, or null if they look like themselves. */
	public static EntityType<?> morph(UUID player) {
		return MORPHED.get(player);
	}

	public static void setMorph(UUID player, EntityType<?> type) {
		if (type == null) {
			MORPHED.remove(player);
		} else {
			MORPHED.put(player, type);
		}
	}

	public static Map<UUID, EntityType<?>> allMorphs() {
		return MORPHED;
	}

	public static void forget(UUID player) {
		MORPHED.remove(player);
	}

	// --------------------------------------------------------- persistence

	public static void load(MinecraftServer server) {
		UNLOCKED.clear();
		MORPHED.clear();

		Path path = savePath(server);

		if (!Files.exists(path)) {
			return;
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			Map<String, Set<String>> raw = GSON.fromJson(reader,
					new TypeToken<Map<String, Set<String>>>() {
					}.getType());

			if (raw == null) {
				return;
			}

			raw.forEach((uuid, ids) -> {
				Set<Identifier> parsed = new LinkedHashSet<>();

				for (String id : ids) {
					Identifier parsedId = Identifier.tryParse(id);

					if (parsedId != null) {
						parsed.add(parsedId);
					}
				}

				try {
					UNLOCKED.put(UUID.fromString(uuid), parsed);
				} catch (IllegalArgumentException ignored) {
					// a hand-edited file; skip the bad line rather than lose the rest
				}
			});
		} catch (IOException | RuntimeException e) {
			MobMorph.LOGGER.warn("Could not read {}, starting with no unlocks", FILE_NAME, e);
		}
	}

	public static void save(MinecraftServer server) {
		Map<String, Set<String>> raw = new HashMap<>();

		UNLOCKED.forEach((uuid, ids) -> {
			Set<String> asText = new TreeSet<>();
			ids.forEach(id -> asText.add(id.toString()));
			raw.put(uuid.toString(), asText);
		});

		try (Writer writer = Files.newBufferedWriter(savePath(server), StandardCharsets.UTF_8)) {
			GSON.toJson(raw, writer);
		} catch (IOException e) {
			MobMorph.LOGGER.warn("Could not write {}", FILE_NAME, e);
		}
	}

	private static Path savePath(MinecraftServer server) {
		return server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);
	}

	/** Used by the sync packet; a copy so callers can't mutate our state. */
	public static Set<Identifier> unlockedCopy(UUID player) {
		return new LinkedHashSet<>(unlocked(player));
	}

	public static void replaceUnlocked(UUID player, Set<Identifier> ids) {
		UNLOCKED.put(player, new LinkedHashSet<>(ids));
	}

	public static Set<UUID> knownPlayers() {
		return new HashSet<>(UNLOCKED.keySet());
	}
}
