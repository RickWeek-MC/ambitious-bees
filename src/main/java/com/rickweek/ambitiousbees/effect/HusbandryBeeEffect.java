package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Puts adult animals in love so they breed, unless the territory already holds plenty of them. */
public class HusbandryBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<HusbandryBeeEffect> CODEC = makeCodec(200, HusbandryBeeEffect::new);

	public HusbandryBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		Map<EntityType<?>, List<Animal>> byType = new HashMap<>();
		for (Animal animal : level.getEntitiesOfClass(Animal.class, area, Animal::isAlive)) {
			byType.computeIfAbsent(animal.getType(), t -> new java.util.ArrayList<>()).add(animal);
		}
		for (List<Animal> group : byType.values()) {
			if (group.size() >= MAX_PER_TYPE) {
				continue;
			}
			int started = 0;
			for (Animal animal : group) {
				if (started < 2 && animal.getAge() == 0 && animal.canFallInLove()) {
					animal.setInLove(null);
					started++;
				}
			}
		}
	}

	private static final int MAX_PER_TYPE = 12;
}
