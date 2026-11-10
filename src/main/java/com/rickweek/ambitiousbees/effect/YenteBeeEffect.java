package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Automated breeding: moves a princess and a drone from the output slots of its own housing
 * back into the breeding slots, so the next cycle starts without the player.
 */
public class YenteBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<YenteBeeEffect> CODEC = makeCodec(20, YenteBeeEffect::new);
	private static final int SLOT_QUEEN = 0;
	private static final int SLOT_DRONE = 1;
	private static final int FIRST_PRODUCT = 2;
	private static final int LAST_PRODUCT = 8;

	public YenteBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		if (!(housing.getBeeInventory() instanceof Container inventory)) {
			return;
		}
		if (inventory.getItem(SLOT_QUEEN).isEmpty()) {
			move(inventory, BeeLifeStage.PRINCESS, SLOT_QUEEN);
		}
		if (inventory.getItem(SLOT_DRONE).isEmpty()) {
			move(inventory, BeeLifeStage.DRONE, SLOT_DRONE);
		}
	}

	private static void move(Container inventory, BeeLifeStage stage, int target) {
		for (int slot = FIRST_PRODUCT; slot <= LAST_PRODUCT; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && IIndividualHandlerItem.getLifeStage(stack) == stage) {
				inventory.setItem(target, stack);
				inventory.setItem(slot, ItemStack.EMPTY);
				inventory.setChanged();
				return;
			}
		}
	}
}
