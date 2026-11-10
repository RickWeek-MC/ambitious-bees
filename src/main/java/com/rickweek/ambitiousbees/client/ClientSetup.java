package com.rickweek.ambitiousbees.client;

import com.rickweek.ambitiousbees.AmbitiousBees;
import com.rickweek.ambitiousbees.registry.ModRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AmbitiousBees.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModRegistries.FLOWER_PEDESTAL_BE.get(), FlowerPedestalRenderer::new);
		event.registerEntityRenderer(ModRegistries.FLYING_BEE.get(), context -> new ThrownItemRenderer<>(context, 0.9F, true));
	}

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModRegistries.GUN_MENU.get(), GunScreen::new);
	}

	private ClientSetup() {}
}
