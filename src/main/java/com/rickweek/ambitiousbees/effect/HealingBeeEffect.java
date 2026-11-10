package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;


/** Gives regeneration to players and heals animals in the territory. */
public class HealingBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<HealingBeeEffect> CODEC = makeCodec(60, HealingBeeEffect::new);

	public HealingBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
			if (entity instanceof ServerPlayer || entity instanceof Animal || entity instanceof Villager) {
				entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, true, true));
				if (entity.getHealth() < entity.getMaxHealth()) {
					entity.heal(1.0f);
				}
			}
		}
	}
}
