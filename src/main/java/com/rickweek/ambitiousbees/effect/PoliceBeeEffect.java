package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.monster.Monster;

/** Hurts hostile mobs in the territory and picks up the resulting drops. */
public class PoliceBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<PoliceBeeEffect> CODEC = makeCodec(20, PoliceBeeEffect::new);

	public PoliceBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		for (Monster monster : level.getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
			monster.hurt(level.damageSources().magic(), 4.0f);
		}
		collectItems(level, housing, area);
	}
}
