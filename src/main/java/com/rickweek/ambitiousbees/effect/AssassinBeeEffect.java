package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Kills the queen of every other bee housing inside the territory. The housing itself then finishes
 * the cycle on its next tick (offspring, drops), so cycles are completed immediately.
 */
public class AssassinBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<AssassinBeeEffect> CODEC = makeCodec(40, AssassinBeeEffect::new);

	public AssassinBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		int minCx = ((int) Math.floor(area.minX)) >> 4;
		int maxCx = ((int) Math.floor(area.maxX)) >> 4;
		int minCz = ((int) Math.floor(area.minZ)) >> 4;
		int maxCz = ((int) Math.floor(area.maxZ)) >> 4;
		for (int cx = minCx; cx <= maxCx; cx++) {
			for (int cz = minCz; cz <= maxCz; cz++) {
				if (!level.hasChunk(cx, cz)) {
					continue;
				}
				LevelChunk chunk = level.getChunk(cx, cz);
				List<BlockEntity> entities = new ArrayList<>(chunk.getBlockEntities().values());
				for (BlockEntity entity : entities) {
					BlockPos pos = entity.getBlockPos();
					if (entity == housing || !(entity instanceof IBeeHousing other) || !area.contains(pos.getCenter())) {
						continue;
					}
					killQueen(other);
					entity.setChanged();
				}
			}
		}
	}

	private static void killQueen(IBeeHousing target) {
		ItemStack queenStack = target.getBeeInventory().getQueen();
		if (queenStack.isEmpty()) {
			return;
		}
		if (IIndividualHandlerItem.getIndividual(queenStack) instanceof IBee queen && queen.isAlive()) {
			queen.setHealth(0);
			queen.saveToStack(queenStack);
			target.getBeeInventory().setQueen(queenStack);
		}
	}
}
