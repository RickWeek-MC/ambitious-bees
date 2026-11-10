package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IEffectData;
import forestry.api.core.genetics.IGenome;
import forestry.apiculture.bees.genetics.effects.ThrottledBeeEffect;
import forestry.core.engine.genetics.EffectData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.function.BiFunction;

/** Base for effects that act on the item held by flower pedestals inside the bee's territory. */
public abstract class PedestalBeeEffect implements IBeeEffect, AreaBeeEffect {
	protected final boolean dominant;
	protected final int throttle;

	protected PedestalBeeEffect(boolean dominant, int throttle) {
		this.dominant = dominant;
		this.throttle = throttle;
	}

	protected static <T extends PedestalBeeEffect> MapCodec<T> makeCodec(int defaultThrottle, BiFunction<Boolean, Integer, T> ctor) {
		return RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.BOOL.optionalFieldOf("dominant", false).forGetter(e -> e.dominant),
				Codec.INT.optionalFieldOf("throttle", defaultThrottle).forGetter(e -> e.throttle)
		).apply(i, ctor::apply));
	}

	/** Work on one pedestal. Called every {@code throttle} ticks per pedestal in range. */
	protected abstract void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal);

	@Override
	public boolean isDominant() {
		return dominant;
	}

	@Override
	public boolean isCombinable() {
		return true;
	}

	@Override
	public IEffectData validateStorage(IEffectData storedData) {
		return storedData instanceof EffectData ? storedData : new EffectData(1, 0);
	}

	@Override
	public IEffectData doEffect(IGenome genome, IEffectData storedData, IBeeHousing housing) {
		Level level = housing.getLevel();
		if (!(level instanceof ServerLevel serverLevel)) {
			return storedData;
		}
		if (!AmbitiousBeesConfig.isEffectEnabled(this)) {
			return storedData;
		}
		int ticks = storedData.getInteger(0) + 1;
		if (ticks < throttle) {
			storedData.setInteger(0, ticks);
			return storedData;
		}
		storedData.setInteger(0, 0);

		processArea(serverLevel, ThrottledBeeEffect.getBounding(housing, genome));
		return storedData;
	}

	@Override
	public void fire(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		if (AmbitiousBeesConfig.isEffectEnabled(this)) {
			processArea(level, area);
		}
	}

	/** Runs {@link #process} on every non-empty flower pedestal inside the box. */
	protected void processArea(ServerLevel level, AABB area) {
		for (FlowerPedestalBlockEntity pedestal : BlockEntityScan.find(level, area, FlowerPedestalBlockEntity.class)) {
			if (!pedestal.isRemoved() && !pedestal.getStack().isEmpty()) {
				process(level, pedestal.getBlockPos(), pedestal);
			}
		}
	}
}
