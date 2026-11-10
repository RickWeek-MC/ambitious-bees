package com.rickweek.ambitiousbees.effect;

import com.rickweek.ambitiousbees.AmbitiousBees;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Tracks the areas protected by N.C.A. bees and cancels creeper explosions in them. */
@EventBusSubscriber(modid = AmbitiousBees.MOD_ID)
public final class NcaProtection {
	private static final long LIFETIME = 400L;
	private static final Map<ResourceKey<Level>, Map<AABB, Long>> AREAS = new HashMap<>();

	private NcaProtection() {}

	static void mark(ServerLevel level, AABB area) {
		Map<AABB, Long> map = AREAS.computeIfAbsent(level.dimension(), k -> new HashMap<>());
		long now = level.getGameTime();
		map.put(area, now + LIFETIME);
		Iterator<Map.Entry<AABB, Long>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			if (it.next().getValue() < now) {
				it.remove();
			}
		}
	}

	/** The areas belong to a running world: forget them when the server stops (singleplayer can load another world). */
	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		AREAS.clear();
	}

	@SubscribeEvent
	public static void onExplosionStart(ExplosionEvent.Start event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		if (!(event.getExplosion().getDirectSourceEntity() instanceof Creeper)
				&& !(event.getExplosion().getIndirectSourceEntity() instanceof Creeper)) {
			return;
		}
		Map<AABB, Long> map = AREAS.get(level.dimension());
		if (map == null) {
			return;
		}
		Vec3 center = event.getExplosion().center();
		long now = level.getGameTime();
		for (Map.Entry<AABB, Long> entry : map.entrySet()) {
			if (entry.getValue() >= now && entry.getKey().contains(center)) {
				event.setCanceled(true);
				return;
			}
		}
	}
}
