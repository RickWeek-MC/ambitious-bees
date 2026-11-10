package com.rickweek.ambitiousbees.effect;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.genetics.IGenome;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

/** An effect that can be fired on demand onto a box of the world (used by the Bee-Bee Gun), ignoring its throttle. */
public interface AreaBeeEffect {
	void fire(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area);
}
