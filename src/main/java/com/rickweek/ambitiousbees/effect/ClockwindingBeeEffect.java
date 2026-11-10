package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import forestry.core.content.energy.tiles.ClockworkEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

/** Clockwinding: keeps Forestry clockwork engines in the territory wound up. */
public class ClockwindingBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<ClockwindingBeeEffect> CODEC = makeCodec(40, ClockwindingBeeEffect::new);
	/** Tension added per cycle and the ceiling (the engine's own safe maximum is 8). */
	private static final float WIND_STEP = 1.0F;
	private static final float WIND_MAX = 8.0F;

	public ClockwindingBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		for (ClockworkEngineBlockEntity engine : BlockEntityScan.find(level, area, ClockworkEngineBlockEntity.class)) {
			wind(level, engine.getBlockPos(), engine);
		}
	}

	/** The tension field is private, so it goes through the engine's own save/load. */
	private static void wind(ServerLevel level, BlockPos pos, ClockworkEngineBlockEntity engine) {
		CompoundTag tag = engine.saveWithoutMetadata(level.registryAccess());
		float tension = tag.getFloat("tension");
		if (tension >= WIND_MAX) {
			return;
		}
		tag.putFloat("tension", Math.min(WIND_MAX, tension + WIND_STEP));
		engine.loadAdditional(tag, level.registryAccess());
		engine.setChanged();
		level.sendBlockUpdated(pos, engine.getBlockState(), engine.getBlockState(), 3);
	}
}
