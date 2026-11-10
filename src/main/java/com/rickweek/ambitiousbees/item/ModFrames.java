package com.rickweek.ambitiousbees.item;

import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.IMutation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Modifiers and hooks of the three special frames. */
public final class ModFrames {
	private ModFrames() {}

	/** Mutation chances are multiplied by this. */
	public static final float MUTATION_MULTIPLIER = 5.0F;
	/** Eternal frame: aging is divided by this (lifespan x1000000). */
	public static final float ETERNAL_LIFESPAN = 1_000_000.0F;
	/** Poison frame: aging is multiplied by this (lifespan halved). */
	public static final float POISON_AGING = 2.0F;
	public static final double POISON_RADIUS = 8.0;
	public static final int POISON_TICKS = 120;

	public static final IBeeModifier MUTATION = new IBeeModifier() {
		@Override
		public float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation, float currentChance) {
			return currentChance * MUTATION_MULTIPLIER;
		}
	};

	public static final IBeeModifier ETERNAL = new IBeeModifier() {
		@Override
		public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
			return 0.0F;
		}

		@Override
		public float modifyAging(IGenome genome, IGenome mate, float currentAging) {
			return currentAging / ETERNAL_LIFESPAN;
		}
	};

	public static final IBeeModifier POISON = new IBeeModifier() {
		@Override
		public float modifyAging(IGenome genome, IGenome mate, float currentAging) {
			return currentAging * POISON_AGING;
		}
	};

	/** Poisons every living thing around the housing. */
	public static void poisonAround(IBeeHousing housing) {
		Level level = housing.getLevel();
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		BlockPos pos = housing.getBlockPos();
		AABB box = new AABB(pos).inflate(POISON_RADIUS);
		for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, box)) {
			if (AmbitiousBeesConfig.canTarget(entity)) {
				entity.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, 0));
			}
		}
	}
}
