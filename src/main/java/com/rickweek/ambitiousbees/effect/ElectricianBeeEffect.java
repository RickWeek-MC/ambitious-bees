package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;

import java.util.List;

/** Calls down lightning on a random living thing in the territory. */
public class ElectricianBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<ElectricianBeeEffect> CODEC = makeCodec(200, ElectricianBeeEffect::new);

	public ElectricianBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
				e -> e.isAlive() && AmbitiousBeesConfig.canTarget(e));
		if (targets.isEmpty()) {
			return;
		}
		LivingEntity target = targets.get(level.random.nextInt(targets.size()));
		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
		if (bolt != null) {
			bolt.moveTo(target.getX(), target.getY(), target.getZ());
			level.addFreshEntity(bolt);
		}
	}
}
