package com.elduin.mob_morph.platform.fabric;

//? fabric {

import com.elduin.mob_morph.MobMorph;
import com.elduin.mob_morph.client.MobMorphClient;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MobMorph.onInitializeClient();
		MobMorphClient.register();
	}

}
//?}
