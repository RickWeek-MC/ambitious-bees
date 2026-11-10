package com.rickweek.ambitiousbees.config;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** config/ambitious_bees-common.toml: switches for the effects that can grief, and a cap for Temporal bees. */
public final class AmbitiousBeesConfig {
	public static final ModConfigSpec SPEC;

	private static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_EFFECTS;
	private static final ModConfigSpec.BooleanValue AFFECT_PLAYERS;
	private static final ModConfigSpec.BooleanValue PROTECT_PETS_AND_VILLAGERS;
	private static final ModConfigSpec.BooleanValue RESPECT_CLAIMS;
	private static final ModConfigSpec.BooleanValue MAD_SCIENTIST_BREAKS_BLOCKS;
	private static final ModConfigSpec.IntValue TEMPORAL_EXTRA_TICKS;
	private static final ModConfigSpec.IntValue TEMPORAL_BUDGET_PER_TICK;

	static {
		ModConfigSpec.Builder b = new ModConfigSpec.Builder();

		b.push("effects");
		DISABLED_EFFECTS = b.comment(
				"Bee effects listed here do nothing (also when fired by the Bee-Bee Gun). Example: [\"thief\", \"devil\"]",
				"Names: artistic, assassin, bensen, business, butcher, chef, clockwinding, collecting, devil, electrician,",
				"enchant_buff (Armorer and Sharpening), energy, fortunate_miner, healing, honeyfication, husbandry, junk_seller,",
				"lumber, mad_scientist, mason, miner, nca, ore_crusher, plague, police, priest, quantum_charming,",
				"quantum_strange, repair, retraining, silky_miner, smelter, taxcollector, temporal, thief, yente")
				.defineListAllowEmpty("disabled_effects", List.of(), () -> "", o -> o instanceof String);
		b.pop();

		b.push("safety");
		AFFECT_PLAYERS = b.comment(
				"If false, Thief, Electrician, Devil and the Poison Frame ignore players.")
				.define("affect_players", true);
		PROTECT_PETS_AND_VILLAGERS = b.comment(
				"If true, Electrician, Devil and the Poison Frame skip villagers, tamed pets, named and leashed mobs.")
				.define("protect_pets_and_villagers", false);
		RESPECT_CLAIMS = b.comment(
				"If true, bees that change blocks (Miners, Lumber, Quantum Charming/Strange) first fire a block break event",
				"for a fake player (the shooter, for the Bee-Bee Gun), so claim and protection mods can say no.")
				.define("respect_claims", true);
		MAD_SCIENTIST_BREAKS_BLOCKS = b.comment(
				"If false, the Mad Scientist's explosions only make noise and particles and never destroy blocks.")
				.define("mad_scientist_breaks_blocks", true);
		b.pop();

		b.push("temporal");
		TEMPORAL_EXTRA_TICKS = b.comment(
				"Random block positions that one Temporal bee gives an extra random tick, every cycle.")
				.defineInRange("extra_ticks_per_cycle", 24, 0, 512);
		TEMPORAL_BUDGET_PER_TICK = b.comment(
				"Global cap (per dimension, all Temporal bees together) on those extra random ticks per game tick.",
				"Lower it if many Temporal bees hurt the TPS. 0 stops the effect.")
				.defineInRange("budget_per_game_tick", 256, 0, 8192);
		b.pop();

		SPEC = b.build();
	}

	private static final Map<Class<?>, String> NAMES = new ConcurrentHashMap<>();

	private AmbitiousBeesConfig() {}

	private static <T> T val(ModConfigSpec.ConfigValue<T> value) {
		return SPEC.isLoaded() ? value.get() : value.getDefault();
	}

	/** ThiefBeeEffect -> "thief", MadScientistBeeEffect -> "mad_scientist". */
	private static String nameOf(Class<?> type) {
		return NAMES.computeIfAbsent(type, c -> c.getSimpleName().replaceAll("BeeEffect$", "")
				.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT));
	}

	public static boolean isEffectEnabled(Object effect) {
		List<? extends String> disabled = val(DISABLED_EFFECTS);
		if (disabled.isEmpty()) {
			return true;
		}
		String name = nameOf(effect.getClass());
		for (String entry : disabled) {
			if (name.equalsIgnoreCase(entry.trim())) {
				return false;
			}
		}
		return true;
	}

	public static boolean affectPlayers() {
		return val(AFFECT_PLAYERS);
	}

	public static boolean respectClaims() {
		return val(RESPECT_CLAIMS);
	}

	public static boolean madScientistBreaksBlocks() {
		return val(MAD_SCIENTIST_BREAKS_BLOCKS);
	}

	public static int temporalExtraTicks() {
		return val(TEMPORAL_EXTRA_TICKS);
	}

	public static int temporalBudgetPerTick() {
		return val(TEMPORAL_BUDGET_PER_TICK);
	}

	/** True if a harmful effect may hit this entity under the current safety options. */
	public static boolean canTarget(LivingEntity entity) {
		if (entity instanceof Player && !affectPlayers()) {
			return false;
		}
		if (val(PROTECT_PETS_AND_VILLAGERS)) {
			if (entity instanceof AbstractVillager || entity.hasCustomName()) {
				return false;
			}
			if (entity instanceof TamableAnimal tame && tame.isTame()) {
				return false;
			}
			if (entity instanceof Mob mob && mob.isLeashed()) {
				return false;
			}
		}
		return true;
	}
}
