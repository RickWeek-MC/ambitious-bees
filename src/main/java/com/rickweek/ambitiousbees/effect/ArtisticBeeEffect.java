package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Hangs paintings on free walls inside the territory, up to a limit per territory. */
public class ArtisticBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<ArtisticBeeEffect> CODEC = makeCodec(600, ArtisticBeeEffect::new);

	private static final int MAX_PAINTINGS = 4;
	private static final int ATTEMPTS = 24;

	public ArtisticBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		if (level.getEntitiesOfClass(Painting.class, area).size() >= MAX_PAINTINGS) {
			return;
		}
		int sx = Math.max(1, (int) area.getXsize());
		int sy = Math.max(1, (int) area.getYsize());
		int sz = Math.max(1, (int) area.getZsize());
		List<Direction> directions = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
		for (int i = 0; i < ATTEMPTS; i++) {
			BlockPos pos = BlockPos.containing(
					area.minX + level.random.nextInt(sx),
					area.minY + level.random.nextInt(sy),
					area.minZ + level.random.nextInt(sz));
			if (!level.isLoaded(pos) || !level.isEmptyBlock(pos)) {
				continue;
			}
			Collections.shuffle(directions, new java.util.Random(level.random.nextLong()));
			for (Direction facing : directions) {
				// The painting hangs in 'pos' facing 'facing'; the wall is behind it.
				if (!level.getBlockState(pos.relative(facing.getOpposite())).isFaceSturdy(level, pos.relative(facing.getOpposite()), facing)) {
					continue;
				}
				Optional<Painting> painting = Painting.create(level, pos, facing);
				if (painting.isPresent()) {
					level.addFreshEntity(painting.get());
					return;
				}
			}
		}
	}
}
