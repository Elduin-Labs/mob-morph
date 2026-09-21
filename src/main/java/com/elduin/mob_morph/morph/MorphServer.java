package com.elduin.mob_morph.morph;

import java.util.Map;
import java.util.UUID;

import com.elduin.mob_morph.MobMorph;
import com.elduin.mob_morph.net.MorphNet;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** All the server-side wiring: unlocking, morphing, flying, saving. */
public final class MorphServer {

	private MorphServer() {
	}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(MorphNet.SyncUnlocks.TYPE, MorphNet.SyncUnlocks.CODEC);
		PayloadTypeRegistry.playS2C().register(MorphNet.SyncMorph.TYPE, MorphNet.SyncMorph.CODEC);
		PayloadTypeRegistry.playC2S().register(MorphNet.RequestMorph.TYPE, MorphNet.RequestMorph.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(MorphNet.RequestMorph.TYPE,
				(payload, context) -> context.server().execute(
						() -> onMorphRequested(context.player(), payload.entityId())));

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> onMobKilled(entity, source.getEntity()));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.player, server));

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			UUID id = handler.player.getUUID();
			MorphState.forget(id);
			broadcast(server, MorphNet.SyncMorph.clearedFor(id));
		});

		ServerLifecycleEvents.SERVER_STARTED.register(MorphState::load);
		ServerLifecycleEvents.SERVER_STOPPING.register(MorphState::save);
	}

	// ------------------------------------------------------------ unlocking

	private static void onMobKilled(LivingEntity dead, Entity killer) {
		if (!(killer instanceof ServerPlayer player) || !Morphs.isMorphable(dead)) {
			return;
		}

		EntityType<?> type = dead.getType();

		if (!MorphState.unlock(player.getUUID(), type)) {
			return; // already had it
		}

		player.sendSystemMessage(Component.translatable("message.mob_morph.unlocked", type.getDescription()));
		sendUnlocks(player);
	}

	// ------------------------------------------------------------- morphing

	private static void onMorphRequested(ServerPlayer player, String rawId) {
		UUID id = player.getUUID();

		if (rawId == null || rawId.isEmpty()) {
			applyMorph(player, null);
			return;
		}

		EntityType<?> type = Morphs.byId(Identifier.tryParse(rawId));

		if (type == null || !MorphState.hasUnlocked(id, type)) {
			player.sendSystemMessage(Component.translatable("message.mob_morph.not_unlocked"));
			return;
		}

		applyMorph(player, type);
	}

	private static void applyMorph(ServerPlayer player, EntityType<?> type) {
		MorphState.setMorph(player.getUUID(), type);
		applyFlight(player, type);

		Identifier id = type == null ? null : Morphs.idOf(type);
		broadcast(player.level().getServer(),
				new MorphNet.SyncMorph(player.getUUID(), id == null ? "" : id.toString()));

		player.sendSystemMessage(type == null
				? Component.translatable("message.mob_morph.unmorphed")
				: Component.translatable("message.mob_morph.morphed", type.getDescription()));
	}

	/** Blaze, ghast and bee can fly. Creative and spectator keep their own flight. */
	private static void applyFlight(ServerPlayer player, EntityType<?> type) {
		if (player.isCreative() || player.isSpectator()) {
			return;
		}

		boolean fly = Morphs.canFly(type);
		player.getAbilities().mayfly = fly;

		if (!fly) {
			player.getAbilities().flying = false;
		}

		player.onUpdateAbilities();
	}

	// -------------------------------------------------------------- joining

	private static void onJoin(ServerPlayer player, MinecraftServer server) {
		sendUnlocks(player);

		// tell the newcomer what everybody else currently looks like
		for (Map.Entry<UUID, EntityType<?>> entry : MorphState.allMorphs().entrySet()) {
			Identifier id = Morphs.idOf(entry.getValue());

			if (id != null) {
				ServerPlayNetworking.send(player, new MorphNet.SyncMorph(entry.getKey(), id.toString()));
			}
		}
	}

	private static void sendUnlocks(ServerPlayer player) {
		ServerPlayNetworking.send(player,
				new MorphNet.SyncUnlocks(MorphState.unlockedCopy(player.getUUID())));
	}

	private static void broadcast(MinecraftServer server, MorphNet.SyncMorph payload) {
		if (server == null) {
			return;
		}

		for (ServerPlayer online : server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(online, payload);
		}
	}

	static {
		MobMorph.LOGGER.debug("MorphServer loaded");
	}
}
