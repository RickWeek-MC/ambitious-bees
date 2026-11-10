package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;

/** Sets every living thing in the territory on fire. */
public class DevilBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<DevilBeeEffect> CODEC = makeCodec(40, DevilBeeEffect::new);

	public DevilBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
			if (!entity.fireImmune() && AmbitiousBeesConfig.canTarget(entity)) {
				entity.igniteForSeconds(4.0f);
			}
		}
	}
}
