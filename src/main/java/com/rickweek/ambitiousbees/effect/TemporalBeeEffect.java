package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/** Accelerates time: gives extra random ticks to blocks in the territory. */
public class TemporalBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<TemporalBeeEffect> CODEC = makeCodec(20, TemporalBeeEffect::new);

	public TemporalBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		int allowed = takeBudget(level, AmbitiousBeesConfig.temporalExtraTicks());
		if (allowed <= 0) {
			return;
		}
		for (BlockPos pos : randomPositions(level, area, allowed)) {
			BlockState state = level.getBlockState(pos);
			if (state.isRandomlyTicking()) {
				state.randomTick(level, pos, level.random);
			}
		}
	}

	/** Per dimension: {game tick, random ticks already spent in that tick} by all Temporal bees together. */
	private static final Map<ResourceKey<Level>, long[]> BUDGET = new HashMap<>();

	/** How many extra random ticks this bee may still use in the current game tick (global cap, protects the TPS). */
	private static int takeBudget(ServerLevel level, int wanted) {
		long[] budget = BUDGET.computeIfAbsent(level.dimension(), k -> new long[2]);
		long now = level.getGameTime();
		if (budget[0] != now) {
			budget[0] = now;
			budget[1] = 0;
		}
		int allowed = (int) Math.max(0, Math.min(wanted, AmbitiousBeesConfig.temporalBudgetPerTick() - budget[1]));
		budget[1] += allowed;
		return allowed;
	}
}
