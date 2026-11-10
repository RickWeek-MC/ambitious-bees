package com.rickweek.ambitiousbees.registry;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.AmbitiousBees;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlock;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import com.rickweek.ambitiousbees.effect.ArtisticBeeEffect;
import com.rickweek.ambitiousbees.entity.FlyingBeeEntity;
import com.rickweek.ambitiousbees.item.BeeBeeGunItem;
import com.rickweek.ambitiousbees.item.ModFrameItem;
import com.rickweek.ambitiousbees.item.PoisonPotionIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import com.rickweek.ambitiousbees.item.ModFrames;
import com.rickweek.ambitiousbees.menu.GunMenu;
import com.rickweek.ambitiousbees.effect.ChefBeeEffect;
import com.rickweek.ambitiousbees.effect.YenteBeeEffect;
import com.rickweek.ambitiousbees.effect.MinerBeeEffect;
import com.rickweek.ambitiousbees.effect.SilkyMinerBeeEffect;
import com.rickweek.ambitiousbees.effect.FortunateMinerBeeEffect;
import com.rickweek.ambitiousbees.effect.PriestBeeEffect;
import com.rickweek.ambitiousbees.effect.BusinessBeeEffect;
import com.rickweek.ambitiousbees.effect.JunkSellerBeeEffect;
import com.rickweek.ambitiousbees.effect.ClockwindingBeeEffect;
import com.rickweek.ambitiousbees.effect.HealingBeeEffect;
import com.rickweek.ambitiousbees.effect.HoneyficationBeeEffect;
import com.rickweek.ambitiousbees.effect.AssassinBeeEffect;
import com.rickweek.ambitiousbees.effect.NcaBeeEffect;
import com.rickweek.ambitiousbees.effect.QuantumCharmingBeeEffect;
import com.rickweek.ambitiousbees.effect.QuantumStrangeBeeEffect;
import com.rickweek.ambitiousbees.effect.PoliceBeeEffect;
import com.rickweek.ambitiousbees.effect.LumberBeeEffect;
import com.rickweek.ambitiousbees.effect.HusbandryBeeEffect;
import com.rickweek.ambitiousbees.effect.ButcherBeeEffect;
import com.rickweek.ambitiousbees.effect.ThiefBeeEffect;
import com.rickweek.ambitiousbees.effect.TaxcollectorBeeEffect;
import com.rickweek.ambitiousbees.effect.PlagueBeeEffect;
import com.rickweek.ambitiousbees.effect.MadScientistBeeEffect;
import com.rickweek.ambitiousbees.effect.ElectricianBeeEffect;
import com.rickweek.ambitiousbees.effect.DevilBeeEffect;
import com.rickweek.ambitiousbees.effect.TemporalBeeEffect;
import com.rickweek.ambitiousbees.effect.EnergyBeeEffect;
import com.rickweek.ambitiousbees.effect.BensenBeeEffect;
import com.rickweek.ambitiousbees.effect.CollectingBeeEffect;
import com.rickweek.ambitiousbees.effect.EnchantBuffBeeEffect;
import com.rickweek.ambitiousbees.effect.MasonBeeEffect;
import com.rickweek.ambitiousbees.effect.OreCrusherBeeEffect;
import com.rickweek.ambitiousbees.effect.RepairBeeEffect;
import com.rickweek.ambitiousbees.effect.RetrainingBeeEffect;
import com.rickweek.ambitiousbees.effect.SmelterBeeEffect;
import forestry.api.ForestryRegistries;
import forestry.api.core.genetics.MutationConditionType;
import com.rickweek.ambitiousbees.genetics.ExplosionMutationCondition;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRegistries {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AmbitiousBees.MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AmbitiousBees.MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
			DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AmbitiousBees.MOD_ID);
	public static final DeferredRegister<MapCodec<? extends IBeeEffect>> BEE_EFFECT_TYPES =
			DeferredRegister.create(ForestryRegistries.Keys.BEE_EFFECT_TYPE, AmbitiousBees.MOD_ID);

	public static final DeferredRegister<MutationConditionType<?>> MUTATION_CONDITION_TYPES =
			DeferredRegister.create(ForestryRegistries.Keys.MUTATION_CONDITION_TYPE, AmbitiousBees.MOD_ID);
	public static final DeferredHolder<MutationConditionType<?>, MutationConditionType<ExplosionMutationCondition>> EXPLOSION_CONDITION =
			MUTATION_CONDITION_TYPES.register("explosion",
					() -> new MutationConditionType<>(ExplosionMutationCondition.CODEC, ExplosionMutationCondition.STREAM_CODEC));

	public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
			DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, AmbitiousBees.MOD_ID);
	public static final DeferredHolder<IngredientType<?>, IngredientType<PoisonPotionIngredient>> POISON_POTION_INGREDIENT =
			INGREDIENT_TYPES.register("poison_potion", () -> PoisonPotionIngredient.TYPE);

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
			DeferredRegister.create(Registries.ENTITY_TYPE, AmbitiousBees.MOD_ID);
	/** The queen shown flying out of the Bee-Bee Gun. */
	public static final Supplier<EntityType<FlyingBeeEntity>> FLYING_BEE = ENTITY_TYPES.register("flying_bee",
			() -> EntityType.Builder.<FlyingBeeEntity>of(FlyingBeeEntity::new, MobCategory.MISC)
					.sized(0.25F, 0.25F).noSave().noSummon().clientTrackingRange(8).updateInterval(1).build("flying_bee"));

	public static final DeferredRegister<MenuType<?>> MENU_TYPES =
			DeferredRegister.create(Registries.MENU, AmbitiousBees.MOD_ID);

	public static final DeferredItem<Item> HONEY_COMB_INGOT = ITEMS.registerSimpleItem("honey_comb_ingot");
	public static final DeferredItem<Item> YANG_ESSENCE = ITEMS.registerSimpleItem("yang_essence");
	public static final DeferredItem<Item> YIN_ESSENCE = ITEMS.registerSimpleItem("yin_essence");
	/** Has no use (yet), just like the original. */
	public static final DeferredItem<Item> TRIQUETRA = ITEMS.registerSimpleItem("triquetra");
	public static final DeferredItem<Item> MUTATION_FRAME = ITEMS.registerItem("mutation_frame",
			props -> new ModFrameItem(props.durability(240), ModFrames.MUTATION, false, null, "tooltip.ambitious_bees.frame_mutation"));
	public static final DeferredItem<Item> ETERNAL_FRAME = ITEMS.registerItem("eternal_frame",
			props -> new ModFrameItem(props.stacksTo(1), ModFrames.ETERNAL, true, null, "tooltip.ambitious_bees.frame_eternal"));
	public static final DeferredItem<Item> POISON_FRAME = ITEMS.registerItem("poison_frame",
			props -> new ModFrameItem(props.durability(240), ModFrames.POISON, false, ModFrames::poisonAround, "tooltip.ambitious_bees.frame_poison"));
	public static final DeferredItem<BeeBeeGunItem> BEE_BEE_GUN =
			ITEMS.registerItem("bee_bee_gun", BeeBeeGunItem::new, new Item.Properties().stacksTo(1));
	public static final DeferredHolder<MenuType<?>, MenuType<GunMenu>> GUN_MENU = MENU_TYPES.register("bee_bee_gun",
			() -> IMenuTypeExtension.create((id, inventory, buf) -> new GunMenu(id, inventory)));

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
			DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AmbitiousBees.MOD_ID);

	/** The Student drone attached to a bee housing that is being watched (see StudentAlert). */
	public static final Supplier<AttachmentType<ItemStack>> STUDENT_DRONE = ATTACHMENT_TYPES.register("student_drone",
			() -> AttachmentType.builder(() -> ItemStack.EMPTY).serialize(ItemStack.OPTIONAL_CODEC).build());

	public static final DeferredBlock<FlowerPedestalBlock> FLOWER_PEDESTAL = BLOCKS.register("flower_pedestal",
			() -> new FlowerPedestalBlock(BlockBehaviour.Properties.of().noCollission().instabreak().noOcclusion()
					.sound(net.minecraft.world.level.block.SoundType.GRASS)
					.pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
	public static final DeferredItem<BlockItem> FLOWER_PEDESTAL_ITEM =
			ITEMS.registerSimpleBlockItem("flower_pedestal", FLOWER_PEDESTAL);

	public static final Supplier<BlockEntityType<FlowerPedestalBlockEntity>> FLOWER_PEDESTAL_BE =
			BLOCK_ENTITIES.register("flower_pedestal", () ->
					BlockEntityType.Builder.of(FlowerPedestalBlockEntity::new, FLOWER_PEDESTAL.get()).build(null));

	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<SmelterBeeEffect>> SMELTING_EFFECT =
			BEE_EFFECT_TYPES.register("smelting", () -> SmelterBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<HoneyficationBeeEffect>> HONEYFICATION_EFFECT =
			BEE_EFFECT_TYPES.register("honeyfication", () -> HoneyficationBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<MasonBeeEffect>> MASONRY_EFFECT =
			BEE_EFFECT_TYPES.register("masonry", () -> MasonBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<OreCrusherBeeEffect>> CRUSHING_EFFECT =
			BEE_EFFECT_TYPES.register("crushing", () -> OreCrusherBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<CollectingBeeEffect>> COLLECTING_EFFECT =
			BEE_EFFECT_TYPES.register("collecting", () -> CollectingBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<ArtisticBeeEffect>> PAINTING_EFFECT =
			BEE_EFFECT_TYPES.register("painting", () -> ArtisticBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<ChefBeeEffect>> GLAZING_EFFECT =
			BEE_EFFECT_TYPES.register("glazing", () -> ChefBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<PoliceBeeEffect>> POLICING_EFFECT =
			BEE_EFFECT_TYPES.register("policing", () -> PoliceBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<LumberBeeEffect>> BARKING_EFFECT =
			BEE_EFFECT_TYPES.register("barking", () -> LumberBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<HusbandryBeeEffect>> HUSBANDRY_EFFECT =
			BEE_EFFECT_TYPES.register("husbandry", () -> HusbandryBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<ButcherBeeEffect>> BUTCHERY_EFFECT =
			BEE_EFFECT_TYPES.register("butchery", () -> ButcherBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<ThiefBeeEffect>> STEALING_EFFECT =
			BEE_EFFECT_TYPES.register("stealing", () -> ThiefBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<TaxcollectorBeeEffect>> TAXING_EFFECT =
			BEE_EFFECT_TYPES.register("taxing", () -> TaxcollectorBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<PlagueBeeEffect>> PURGING_EFFECT =
			BEE_EFFECT_TYPES.register("purging", () -> PlagueBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<ClockwindingBeeEffect>> CLOCKWINDING_EFFECT =
			BEE_EFFECT_TYPES.register("clockwinding", () -> ClockwindingBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<MadScientistBeeEffect>> TINKERING_EFFECT =
			BEE_EFFECT_TYPES.register("tinkering", () -> MadScientistBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<ElectricianBeeEffect>> SHOCKING_EFFECT =
			BEE_EFFECT_TYPES.register("shocking", () -> ElectricianBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<DevilBeeEffect>> BURNING_EFFECT =
			BEE_EFFECT_TYPES.register("burning", () -> DevilBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<TemporalBeeEffect>> ACCELERATING_EFFECT =
			BEE_EFFECT_TYPES.register("accelerating", () -> TemporalBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<EnergyBeeEffect>> ENERGIZING_EFFECT =
			BEE_EFFECT_TYPES.register("energizing", () -> EnergyBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<BensenBeeEffect>> JAZZ_EFFECT =
			BEE_EFFECT_TYPES.register("jazz", () -> BensenBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<MinerBeeEffect>> MINING_EFFECT =
			BEE_EFFECT_TYPES.register("mining", () -> MinerBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<SilkyMinerBeeEffect>> SILK_MINING_EFFECT =
			BEE_EFFECT_TYPES.register("silk_mining", () -> SilkyMinerBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<FortunateMinerBeeEffect>> FORTUNE_MINING_EFFECT =
			BEE_EFFECT_TYPES.register("fortune_mining", () -> FortunateMinerBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<PriestBeeEffect>> BLESSING_EFFECT =
			BEE_EFFECT_TYPES.register("blessing", () -> PriestBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<BusinessBeeEffect>> TRADING_EFFECT =
			BEE_EFFECT_TYPES.register("trading", () -> BusinessBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<JunkSellerBeeEffect>> SELLING_EFFECT =
			BEE_EFFECT_TYPES.register("selling", () -> JunkSellerBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<HealingBeeEffect>> HEALING_EFFECT =
			BEE_EFFECT_TYPES.register("healing", () -> HealingBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<AssassinBeeEffect>> ASSASSINATING_EFFECT =
			BEE_EFFECT_TYPES.register("assassinating", () -> AssassinBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<NcaBeeEffect>> PROTECTING_EFFECT =
			BEE_EFFECT_TYPES.register("protecting", () -> NcaBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<QuantumCharmingBeeEffect>> CHARMING_SWAP_EFFECT =
			BEE_EFFECT_TYPES.register("charming_swap", () -> QuantumCharmingBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<QuantumStrangeBeeEffect>> STRANGE_SWAP_EFFECT =
			BEE_EFFECT_TYPES.register("strange_swap", () -> QuantumStrangeBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<YenteBeeEffect>> BREEDING_EFFECT =
			BEE_EFFECT_TYPES.register("breeding", () -> YenteBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<RepairBeeEffect>> REPAIR_EFFECT =
			BEE_EFFECT_TYPES.register("repair", () -> RepairBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<RetrainingBeeEffect>> RETRAINING_EFFECT =
			BEE_EFFECT_TYPES.register("retraining", () -> RetrainingBeeEffect.CODEC);
	public static final DeferredHolder<MapCodec<? extends IBeeEffect>, MapCodec<EnchantBuffBeeEffect>> ENCHANT_BUFF_EFFECT =
			BEE_EFFECT_TYPES.register("enchant_buff", () -> EnchantBuffBeeEffect.CODEC);

	public static void register(IEventBus bus) {
		BLOCKS.register(bus);
		ITEMS.register(bus);
		BLOCK_ENTITIES.register(bus);
		BEE_EFFECT_TYPES.register(bus);
		ATTACHMENT_TYPES.register(bus);
		MENU_TYPES.register(bus);
		ENTITY_TYPES.register(bus);
		ModTabs.TABS.register(bus);
		INGREDIENT_TYPES.register(bus);
		MUTATION_CONDITION_TYPES.register(bus);
	}

	private ModRegistries() {}
}
