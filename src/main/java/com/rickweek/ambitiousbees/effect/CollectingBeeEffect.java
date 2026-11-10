package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** Picks up item entities lying in the territory and stores them in the housing's output slots. */
public class CollectingBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<CollectingBeeEffect> CODEC = makeCodec(20, CollectingBeeEffect::new);

	private static final int MAX_ITEMS_PER_RUN = 16;

	public CollectingBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area,
				e -> e.isAlive() && !e.hasPickUpDelay() && !e.getItem().isEmpty());
		int handled = 0;
		for (ItemEntity entity : items) {
			if (handled++ >= MAX_ITEMS_PER_RUN) {
				break;
			}
			ItemStack stack = entity.getItem().copy();
			// all = true: the whole stack must fit, otherwise nothing is taken.
			if (housing.getBeeInventory().addProduct(stack, true)) {
				entity.discard();
			}
		}
	}
}
