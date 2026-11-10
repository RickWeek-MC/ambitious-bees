package com.rickweek.ambitiousbees.effect;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** Swaps pairs of "similar" blocks (members of the same tag but different blocks) inside the territory. */
public abstract class QuantumSwapBeeEffect extends TerritoryBeeEffect {
	private static final int SWAPS_PER_RUN = 3;

	protected QuantumSwapBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	/** Groups of similar blocks this bee can swap. */
	protected abstract List<TagKey<Block>> groups();

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		int done = 0;
		for (BlockPos a : randomPositions(level, area, 40)) {
			BlockState stateA = level.getBlockState(a);
			if (stateA.isAir() || level.getBlockEntity(a) != null) {
				continue;
			}
			TagKey<Block> group = groupOf(stateA);
			if (group == null) {
				continue;
			}
			for (BlockPos b : randomPositions(level, area, 40)) {
				BlockState stateB = level.getBlockState(b);
				if (stateB.is(stateA.getBlock()) || !stateB.is(group) || level.getBlockEntity(b) != null) {
					continue;
				}
				if (!BlockGuard.canModify(level, a, stateA, housing) || !BlockGuard.canModify(level, b, stateB, housing)) {
					continue;
				}
				level.setBlock(a, stateB, 3);
				level.setBlock(b, stateA, 3);
				level.sendParticles(ParticleTypes.PORTAL, a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.5);
				level.sendParticles(ParticleTypes.PORTAL, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.5);
				done++;
				break;
			}
			if (done >= SWAPS_PER_RUN) {
				break;
			}
		}
	}

	private TagKey<Block> groupOf(BlockState state) {
		for (TagKey<Block> tag : groups()) {
			if (state.is(tag)) {
				return tag;
			}
		}
		return null;
	}
}
