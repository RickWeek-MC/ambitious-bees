package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.AmbitiousBees;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.ISpecies;
import forestry.api.core.genetics.alleles.AllelePair;
import forestry.api.core.genetics.alleles.BeeChromosomes;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Restores the effect chromosome of a purebred career bee that has lost it.
 * The bee to retrain is placed on a flower pedestal inside the territory.
 */
public class RetrainingBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<RetrainingBeeEffect> CODEC = makeCodec(100, RetrainingBeeEffect::new);

	public RetrainingBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		ItemStack stack = pedestal.getStack();
		if (!IIndividualHandlerItem.hasIndividual(stack)) {
			return;
		}
		IIndividual individual = IIndividualHandlerItem.getIndividual(stack);
		if (!(individual instanceof IBee)) {
			return;
		}
		IGenome genome = individual.getGenome();
		// Only purebred bees (same species on both chromosomes) of this mod are retrained.
		AllelePair<ResourceLocation> speciesPair = genome.getAllelePair(BeeChromosomes.SPECIES);
		if (!speciesPair.isSameAlleles()) {
			return;
		}
		ISpecies<?> species = genome.getActiveSpecies();
		if (!AmbitiousBees.MOD_ID.equals(species.id().getNamespace())) {
			return;
		}
		AllelePair<ResourceLocation> defaultEffect = species.getDefaultGenome().getAllelePair(BeeChromosomes.EFFECT);
		IGenome restored = genome.copyWithPairs(Map.<IChromosome<?>, AllelePair<?>>of(BeeChromosomes.EFFECT, defaultEffect));
		if (restored.isSameAlleles(genome)) {
			return;
		}
		IIndividual retrained = individual.copyWithGenome(restored);
		ItemStack result = stack.copy();
		retrained.saveToStack(result);
		pedestal.setStack(result);
		level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 12, 0.25, 0.2, 0.25, 0.1);
	}
}
