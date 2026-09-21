package com.elduin.mob_morph.client;

import com.elduin.mob_morph.morph.Morphs;
import com.elduin.mob_morph.net.MorphNet;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.lwjgl.glfw.GLFW;

/** The B key, the menu, and keeping the client's picture of everyone up to date. */
public final class MobMorphClient {

	public static final KeyMapping OPEN_MENU = new KeyMapping(
			"key.mob_morph.open_menu",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_B,
			KeyMapping.Category.MISC);

	private MobMorphClient() {
	}

	public static void register() {
		KeyBindingHelper.registerKeyBinding(OPEN_MENU);

		ClientPlayNetworking.registerGlobalReceiver(MorphNet.SyncUnlocks.TYPE,
				(payload, context) -> context.client().execute(
						() -> ClientMorphs.setUnlocks(payload.unlocked())));

		ClientPlayNetworking.registerGlobalReceiver(MorphNet.SyncMorph.TYPE,
				(payload, context) -> context.client().execute(() -> {
					EntityType<?> type = payload.entityId().isEmpty()
							? null
							: Morphs.byId(Identifier.tryParse(payload.entityId()));
					ClientMorphs.setMorph(payload.player(), type);
				}));

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientMorphs.clear());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_MENU.consumeClick()) {
				if (client.player != null && client.screen == null) {
					client.setScreen(new MorphScreen());
				}
			}
		});
	}

	/** Ask the server to turn us into this. Null means turn back into ourselves. */
	public static void requestMorph(EntityType<?> type) {
		Identifier id = type == null ? null : Morphs.idOf(type);
		ClientPlayNetworking.send(new MorphNet.RequestMorph(id == null ? "" : id.toString()));
	}
}
