package com.elduin.mob_morph.platform.fabric;

//? fabric {

import com.elduin.mob_morph.MobMorph;
import com.elduin.mob_morph.morph.MorphServer;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		MobMorph.onInitialize();
		MorphServer.register();
	}
}
//?}
