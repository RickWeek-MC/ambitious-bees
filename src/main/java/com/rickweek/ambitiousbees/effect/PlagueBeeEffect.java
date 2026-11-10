package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/** Cures harmful potion effects and slightly heals living things in the territory. */
public class PlagueBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<PlagueBeeEffect> CODEC = makeCodec(40, PlagueBeeEffect::new);

	public PlagueBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
			List<Holder<MobEffect>> harmful = new ArrayList<>();
			for (MobEffectInstance instance : entity.getActiveEffects()) {
				if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
					harmful.add(instance.getEffect());
				}
			}
			harmful.forEach(entity::removeEffect);
			if (entity.getHealth() < entity.getMaxHealth()) {
				entity.heal(1.0f);
			}
		}
	}
}
