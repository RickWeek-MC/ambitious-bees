package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;

/** Strips the bark off logs in the territory. */
public class LumberBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<LumberBeeEffect> CODEC = makeCodec(60, LumberBeeEffect::new);

	public LumberBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		// Random samples instead of a scan of the whole territory: cheap, and every log gets its turn over time.
		int done = 0;
		for (BlockPos pos : randomPositions(level, area, SAMPLES)) {
			BlockState state = level.getBlockState(pos);
			BlockState stripped = AxeItem.getAxeStrippingState(state);
			if (stripped == null || !BlockGuard.canModify(level, pos, state, housing)) {
				continue;
			}
			level.setBlock(pos, stripped, 3);
			level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.02);
			if (++done >= MAX_PER_RUN) {
				break;
			}
		}
	}

	private static final int MAX_PER_RUN = 4;
	/** Random positions looked at per run. */
	private static final int SAMPLES = 128;
}
