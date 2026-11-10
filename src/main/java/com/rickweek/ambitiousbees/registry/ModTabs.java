package com.rickweek.ambitiousbees.registry;

import com.rickweek.ambitiousbees.AmbitiousBees;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.core.platform.util.SpeciesUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The creative tab with everything the mod adds: items, blocks and all of its bees. */
public final class ModTabs {
	private ModTabs() {}

	public static final DeferredRegister<CreativeModeTab> TABS =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AmbitiousBees.MOD_ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.ambitious_bees"))
					.icon(() -> new ItemStack(ModRegistries.BEE_BEE_GUN.get()))
					.displayItems((parameters, output) -> {
						ModRegistries.ITEMS.getEntries().forEach(entry -> output.accept(entry.get()));
						addBees(output);
					})
					.build());

	/** Princess, drone and queen of every species of this mod (nothing if the species are not loaded yet). */
	private static void addBees(CreativeModeTab.Output output) {
		try {
			for (IBeeSpecies species : SpeciesUtil.getAllBeeSpecies()) {
				if (!AmbitiousBees.MOD_ID.equals(species.id().getNamespace())) {
					continue;
				}
				for (BeeLifeStage stage : new BeeLifeStage[]{BeeLifeStage.PRINCESS, BeeLifeStage.DRONE, BeeLifeStage.QUEEN}) {
					output.accept(SpeciesUtil.BEE_TYPE.get().createStack(species.id(), stage));
				}
			}
		} catch (RuntimeException e) {
			// species are data-driven and may not exist before a world is loaded: the tab simply has no bees then
		}
	}
}
