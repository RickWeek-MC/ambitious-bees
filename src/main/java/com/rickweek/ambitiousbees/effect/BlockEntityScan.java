package com.rickweek.ambitiousbees.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the block entities of a given type inside a box by looking only at the block entities of the loaded chunks
 * that overlap it, instead of asking the world for every single block position of the box.
 */
final class BlockEntityScan {
	private BlockEntityScan() {}

	static <T extends BlockEntity> List<T> find(ServerLevel level, AABB area, Class<T> type) {
		BlockPos min = BlockPos.containing(area.minX, area.minY, area.minZ);
		BlockPos max = BlockPos.containing(area.maxX, area.maxY, area.maxZ);
		List<T> result = new ArrayList<>();
		for (int cx = min.getX() >> 4; cx <= max.getX() >> 4; cx++) {
			for (int cz = min.getZ() >> 4; cz <= max.getZ() >> 4; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (BlockEntity entity : new ArrayList<>(chunk.getBlockEntities().values())) {
					BlockPos pos = entity.getBlockPos();
					if (type.isInstance(entity)
							&& pos.getX() >= min.getX() && pos.getX() <= max.getX()
							&& pos.getY() >= min.getY() && pos.getY() <= max.getY()
							&& pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ()) {
						result.add(type.cast(entity));
					}
				}
			}
		}
		return result;
	}
}
