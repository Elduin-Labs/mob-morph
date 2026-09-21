package com.elduin.mob_morph.mixin;

import com.elduin.mob_morph.client.ClientMorphs;
import com.elduin.mob_morph.client.MorphDummies;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The whole trick.
 *
 * Minecraft draws an entity in two steps: pull a "render state" off it, then
 * hand that state to whichever renderer matches. If we hand back the state of
 * a mob instead of the state of the player, the game picks the mob's renderer
 * and draws the mob — model, texture, animations and all.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

	@Inject(method = "extractEntity", at = @At("HEAD"), cancellable = true)
	private <E extends Entity> void mobMorph$renderPlayerAsMob(
			E entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {

		if (!(entity instanceof AbstractClientPlayer player)) {
			return;
		}

		EntityType<?> type = ClientMorphs.morphOf(player.getUUID());

		if (type == null) {
			return;
		}

		Entity dummy = MorphDummies.posedLike(type, player);

		if (dummy == null) {
			return;
		}

		// The stand-in is not a player, so this call falls straight through
		// the check above instead of looping back into us.
		EntityRenderDispatcher self = (EntityRenderDispatcher) (Object) this;
		cir.setReturnValue(self.extractEntity(dummy, partialTick));
	}
}
