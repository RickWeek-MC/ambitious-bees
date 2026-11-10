package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

/** Extracts the loot of mobs in the territory without harming them (once per mob every 5 minutes). */
public class TaxcollectorBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<TaxcollectorBeeEffect> CODEC = makeCodec(200, TaxcollectorBeeEffect::new);

	public TaxcollectorBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		long now = level.getGameTime();
		for (Mob mob : level.getEntitiesOfClass(Mob.class, area, Mob::isAlive)) {
			long last = mob.getPersistentData().getLong(LAST_TAX_KEY);
			if (last != 0 && now - last < COOLDOWN_TICKS) {
				continue;
			}
			ResourceKey<LootTable> key = mob.getLootTable();
			LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
			LootParams params = new LootParams.Builder(level)
					.withParameter(LootContextParams.THIS_ENTITY, mob)
					.withParameter(LootContextParams.ORIGIN, mob.position())
					.withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
					.create(LootContextParamSets.ENTITY);
			List<ItemStack> drops = table.getRandomItems(params);
			mob.getPersistentData().putLong(LAST_TAX_KEY, now);
			for (ItemStack drop : drops) {
				give(level, housing, drop, mob.getX(), mob.getY(), mob.getZ());
			}
		}
	}

	private static final String LAST_TAX_KEY = "ambitious_bees_last_tax";
	private static final long COOLDOWN_TICKS = 6000;
}
