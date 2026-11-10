package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IEffectData;
import forestry.api.core.genetics.IGenome;
import forestry.apiculture.bees.genetics.effects.ThrottledBeeEffect;
import forestry.core.engine.genetics.EffectData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/** Base for effects that act on the whole territory of the bee (entities, blocks...). Server side only, throttled. */
public abstract class TerritoryBeeEffect implements IBeeEffect, AreaBeeEffect {
	protected final boolean dominant;
	protected final int throttle;

	protected TerritoryBeeEffect(boolean dominant, int throttle) {
		this.dominant = dominant;
		this.throttle = throttle;
	}

	protected static <T extends TerritoryBeeEffect> MapCodec<T> makeCodec(int defaultThrottle, BiFunction<Boolean, Integer, T> ctor) {
		return RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.BOOL.optionalFieldOf("dominant", false).forGetter(e -> e.dominant),
				Codec.INT.optionalFieldOf("throttle", defaultThrottle).forGetter(e -> e.throttle)
		).apply(i, ctor::apply));
	}

	/** Called every {@code throttle} ticks with the bee's territory. */
	protected abstract void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area);

	@Override
	public void fire(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		if (AmbitiousBeesConfig.isEffectEnabled(this)) {
			applyEffect(level, genome, housing, area);
		}
	}

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
		applyEffect(serverLevel, genome, housing, ThrottledBeeEffect.getBounding(housing, genome));
		return storedData;
	}

	/** Random positions inside the territory (may repeat, may be unloaded: callers should check). */
	protected static List<BlockPos> randomPositions(ServerLevel level, AABB area, int count) {
		int sx = Math.max(1, (int) area.getXsize());
		int sy = Math.max(1, (int) area.getYsize());
		int sz = Math.max(1, (int) area.getZsize());
		List<BlockPos> result = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			BlockPos pos = BlockPos.containing(
					area.minX + level.random.nextInt(sx),
					area.minY + level.random.nextInt(sy),
					area.minZ + level.random.nextInt(sz));
			if (level.isLoaded(pos)) {
				result.add(pos);
			}
		}
		if (result.isEmpty()) {
			result.add(BlockPos.containing(area.getCenter()));
		}
		return result;
	}

	/** Puts a stack into the housing's output slots, or drops it at the given position if it does not fit. */
	protected static void give(ServerLevel level, IBeeHousing housing, ItemStack stack, double x, double y, double z) {
		if (stack.isEmpty()) {
			return;
		}
		if (!housing.getBeeInventory().addProduct(stack.copy(), true)) {
			level.addFreshEntity(new ItemEntity(level, x, y, z, stack.copy()));
		}
	}

	/** Moves item entities in the territory into the housing's output slots (stacks that do not fit stay). */
	protected static void collectItems(ServerLevel level, IBeeHousing housing, AABB area) {
		int handled = 0;
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area,
				e -> e.isAlive() && !e.hasPickUpDelay() && !e.getItem().isEmpty())) {
			if (handled++ >= 16) {
				break;
			}
			if (housing.getBeeInventory().addProduct(entity.getItem().copy(), true)) {
				entity.discard();
			}
		}
	}

	/** Position of the housing: its block entity if available, otherwise the center of the territory box. */
	protected static BlockPos housingPos(IBeeHousing housing, AABB area) {
		if (housing instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
			return blockEntity.getBlockPos();
		}
		return BlockPos.containing(area.getCenter());
	}
}
