package com.rickweek.ambitiousbees;

import forestry.api.client.plugin.IClientRegistration;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.plugin.IForestryPlugin;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

/** Forestry plugin entry point (loaded via ServiceLoader). */
public class AmbitiousBeesPlugin implements IForestryPlugin {
	private static final String MOD_ID = "ambitious_bees";

	/** Species that have their own item models (bee_<stage>_<species>.json). */
	private static final List<String> CUSTOM_MODEL_SPECIES = List.of("student", "graduate", "doctor", "armorer", "sharpening", "repair", "smelter", "mason", "ore_crusher", "collecting", "husbandry", "electrician", "artistic", "assassin", "bensen", "butcher", "chef", "clockwork", "devil", "energy", "engineer", "lumber", "mad_scientist", "nca", "phd", "plague", "police", "politician", "quantum_charming", "quantum_strange", "rainbow", "robot", "science", "taxcollector", "temporal", "thief", "miner", "silky_miner", "fortunate_miner", "yente", "priest", "business", "junk_seller", "honey_smelter");

	@Override
	public ResourceLocation id() {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, "main");
	}

	@Override
	public void registerClient(Consumer<Consumer<IClientRegistration>> registrar) {
		registrar.accept(client -> {
			for (String species : CUSTOM_MODEL_SPECIES) {
				ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, species);
				for (BeeLifeStage stage : BeeLifeStage.values()) {
					client.setCustomBeeModel(id, stage, ResourceLocation.fromNamespaceAndPath(MOD_ID,
							"item/bee_" + stage.name().toLowerCase(java.util.Locale.ROOT) + "_" + species));
				}
			}
		});
	}
}
