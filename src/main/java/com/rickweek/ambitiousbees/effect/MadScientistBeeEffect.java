package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

/** Sets off small explosions that wreck blocks but never hurt entities. */
public class MadScientistBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<MadScientistBeeEffect> CODEC = makeCodec(400, MadScientistBeeEffect::new);

	public MadScientistBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		BlockPos pos = randomPositions(level, area, 1).get(0);
		level.explode(null, null, NO_ENTITY_DAMAGE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				2.0f, false,
				AmbitiousBeesConfig.madScientistBreaksBlocks() ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
	}

	private static final ExplosionDamageCalculator NO_ENTITY_DAMAGE = new ExplosionDamageCalculator() {
		@Override
		public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
			return false;
		}
	};
}
