package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Slaughters surplus adult animals, always keeping a breeding pair of each type. */
public class ButcherBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<ButcherBeeEffect> CODEC = makeCodec(100, ButcherBeeEffect::new);

	public ButcherBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		Map<EntityType<?>, List<Animal>> adults = new HashMap<>();
		for (Animal animal : level.getEntitiesOfClass(Animal.class, area, Animal::isAlive)) {
			boolean protectedAnimal = animal.isBaby() || animal.hasCustomName() || animal.isLeashed()
					|| (animal instanceof TamableAnimal tame && tame.isTame());
			if (!protectedAnimal) {
				adults.computeIfAbsent(animal.getType(), t -> new ArrayList<>()).add(animal);
			}
		}
		for (List<Animal> group : adults.values()) {
			for (int i = 2; i < group.size(); i++) {
				group.get(i).kill();
			}
		}
	}
}
