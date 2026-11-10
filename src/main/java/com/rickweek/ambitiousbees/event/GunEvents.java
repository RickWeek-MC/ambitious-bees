package com.rickweek.ambitiousbees.event;

import com.rickweek.ambitiousbees.AmbitiousBees;
import com.rickweek.ambitiousbees.item.BeeBeeGunItem;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class GunEvents {
	private GunEvents() {}

	/** Aiming the gun at a block (an apiary, a chest...) shoots instead of opening its GUI. */
	@EventBusSubscriber(modid = AmbitiousBees.MOD_ID)
	public static final class Game {
		@SubscribeEvent
		public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
			if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().getItem() instanceof BeeBeeGunItem) {
				event.setUseBlock(TriState.FALSE);
			}
		}
	}
}
