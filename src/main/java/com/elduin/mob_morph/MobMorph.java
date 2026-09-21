package com.elduin.mob_morph;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MobMorph {

	public static final String MOD_ID = /*$ mod_id*/ "modtemplate";
	public static final String MOD_VERSION = /*$ mod_version*/ "1.0.0";
	public static final String MOD_FRIENDLY_NAME = /*$ mod_name*/ "Mod Template";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private MobMorph() {
	}

	public static void onInitialize() {
		LOGGER.info("{} {} starting", MOD_FRIENDLY_NAME, MOD_VERSION);
	}

	public static void onInitializeClient() {
		LOGGER.info("{} {} client starting", MOD_FRIENDLY_NAME, MOD_VERSION);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
