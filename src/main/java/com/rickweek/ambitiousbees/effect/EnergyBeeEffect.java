package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Pushes Forge Energy into the blocks next to the housing. */
public class EnergyBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<EnergyBeeEffect> CODEC = makeCodec(20, EnergyBeeEffect::new);

	public EnergyBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		BlockPos origin = housingPos(housing, area);
		for (Direction direction : Direction.values()) {
			IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, origin.relative(direction), direction.getOpposite());
			if (storage != null && storage.canReceive()) {
				storage.receiveEnergy(ENERGY_PER_RUN, false);
			}
		}
	}

	private static final int ENERGY_PER_RUN = 400;
}
