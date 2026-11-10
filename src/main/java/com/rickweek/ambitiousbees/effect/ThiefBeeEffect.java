package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/** Steals one item from the inventory of players in the territory. */
public class ThiefBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<ThiefBeeEffect> CODEC = makeCodec(200, ThiefBeeEffect::new);

	public ThiefBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		if (!AmbitiousBeesConfig.affectPlayers()) {
			return;
		}
		for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area,
				p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
			Inventory inventory = player.getInventory();
			List<Integer> filled = new ArrayList<>();
			for (int i = 0; i < inventory.items.size(); i++) {
				if (!inventory.items.get(i).isEmpty()) {
					filled.add(i);
				}
			}
			if (filled.isEmpty()) {
				continue;
			}
			ItemStack slot = inventory.items.get(filled.get(level.random.nextInt(filled.size())));
			ItemStack stolen = slot.copyWithCount(1);
			slot.shrink(1);
			give(level, housing, stolen, player.getX(), player.getY(), player.getZ());
		}
	}
}
