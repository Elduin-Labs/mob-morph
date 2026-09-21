package com.elduin.mob_morph.net;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.elduin.mob_morph.MobMorph;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * The three messages this mod sends.
 *
 * Everything is written as plain strings and ints on purpose — it is the part
 * of the network API least likely to be renamed out from under us between
 * Minecraft versions.
 */
public final class MorphNet {

	private MorphNet() {
	}

	/** Server -> one client: everything that client has unlocked. */
	public record SyncUnlocks(Set<Identifier> unlocked) implements CustomPacketPayload {

		public static final Type<SyncUnlocks> TYPE = new Type<>(MobMorph.id("sync_unlocks"));

		public static final StreamCodec<FriendlyByteBuf, SyncUnlocks> CODEC = StreamCodec.of(
				(buf, value) -> {
					buf.writeInt(value.unlocked().size());
					value.unlocked().forEach(id -> buf.writeUtf(id.toString()));
				},
				buf -> {
					int count = buf.readInt();
					Set<Identifier> ids = new LinkedHashSet<>();

					for (int i = 0; i < count; i++) {
						Identifier id = Identifier.tryParse(buf.readUtf());

						if (id != null) {
							ids.add(id);
						}
					}

					return new SyncUnlocks(ids);
				});

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Server -> every client: this player now looks like this (empty = themselves). */
	public record SyncMorph(UUID player, String entityId) implements CustomPacketPayload {

		public static final Type<SyncMorph> TYPE = new Type<>(MobMorph.id("sync_morph"));

		public static final StreamCodec<FriendlyByteBuf, SyncMorph> CODEC = StreamCodec.of(
				(buf, value) -> {
					buf.writeUUID(value.player());
					buf.writeUtf(value.entityId());
				},
				buf -> new SyncMorph(buf.readUUID(), buf.readUtf()));

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}

		public static SyncMorph clearedFor(UUID player) {
			return new SyncMorph(player, "");
		}
	}

	/** Client -> server: please turn me into this (empty = turn me back). */
	public record RequestMorph(String entityId) implements CustomPacketPayload {

		public static final Type<RequestMorph> TYPE = new Type<>(MobMorph.id("request_morph"));

		public static final StreamCodec<FriendlyByteBuf, RequestMorph> CODEC = StreamCodec.of(
				(buf, value) -> buf.writeUtf(value.entityId()),
				buf -> new RequestMorph(buf.readUtf()));

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
