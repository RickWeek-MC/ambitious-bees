package com.rickweek.ambitiousbees.genetics;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.AmbitiousBees;
import com.rickweek.ambitiousbees.registry.ModRegistries;
import forestry.api.core.climate.IClimateProvider;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.IMutation;
import forestry.api.core.genetics.IMutationCondition;
import forestry.api.core.genetics.MutationConditionType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The mutation only works if something blew up near the housing shortly before the roll
 * (the queen's last moments). Explosions are remembered for a short while per dimension.
 */
@EventBusSubscriber(modid = AmbitiousBees.MOD_ID)
public class ExplosionMutationCondition implements IMutationCondition {
	public static final ExplosionMutationCondition INSTANCE = new ExplosionMutationCondition();
	public static final MapCodec<ExplosionMutationCondition> CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ExplosionMutationCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	/** How long (ticks) an explosion counts: the last 10 seconds. */
	private static final long WINDOW_TICKS = 200;
	/** How close (blocks) the blast has to be to the housing. */
	private static final double RADIUS = 16.0;

	private record Blast(Vec3 center, long time) {
	}

	private static final Map<ResourceKey<Level>, List<Blast>> BLASTS = new HashMap<>();

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		Level level = event.getLevel();
		if (level.isClientSide()) {
			return;
		}
		List<Blast> list = BLASTS.computeIfAbsent(level.dimension(), k -> new ArrayList<>());
		long now = level.getGameTime();
		list.removeIf(b -> now - b.time() > WINDOW_TICKS * 2);
		list.add(new Blast(event.getExplosion().center(), now));
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		BLASTS.clear();
	}

	@Override
	public float modifyChance(Level level, BlockPos pos, IMutation<?> mutation, IGenome firstGenome,
			IGenome secondGenome, IClimateProvider climate, float chance) {
		List<Blast> list = BLASTS.get(level.dimension());
		if (list == null) {
			return 0f;
		}
		long now = level.getGameTime();
		Vec3 here = Vec3.atCenterOf(pos);
		for (Blast blast : list) {
			if (now - blast.time() <= WINDOW_TICKS && blast.center().distanceToSqr(here) <= RADIUS * RADIUS) {
				return chance;
			}
		}
		return 0f;
	}

	@Override
	public Component getDescription() {
		return Component.translatable("mutation.ambitious_bees.explosion");
	}

	@Override
	public MutationConditionType<?> type() {
		return ModRegistries.EXPLOSION_CONDITION.get();
	}
}
