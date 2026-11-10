package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;


/** Marks its territory as creeper-safe: creeper explosions inside are cancelled (see NcaProtection). */
public class NcaBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<NcaBeeEffect> CODEC = makeCodec(10, NcaBeeEffect::new);

	public NcaBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		NcaProtection.mark(level, area);
	}
}
