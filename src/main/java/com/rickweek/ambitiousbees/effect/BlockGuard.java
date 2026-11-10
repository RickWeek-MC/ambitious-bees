package com.rickweek.ambitiousbees.effect;

import com.mojang.authlib.GameProfile;
import com.rickweek.ambitiousbees.config.AmbitiousBeesConfig;
import com.rickweek.ambitiousbees.item.GunHousing;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Asks the rest of the game (claim and protection mods) whether a bee may change a block, by posting a
 * {@link BlockEvent.BreakEvent} for a fake player. For the Bee-Bee Gun the real shooter is used instead.
 * Does nothing (always allows) when the {@code respect_claims} option is off.
 */
final class BlockGuard {
	private static final GameProfile PROFILE = new GameProfile(
			UUID.nameUUIDFromBytes("ambitious_bees:block_guard".getBytes(StandardCharsets.UTF_8)), "[Ambitious Bees]");

	private BlockGuard() {}

	static boolean canModify(ServerLevel level, BlockPos pos, BlockState state, IBeeHousing housing) {
		if (!AmbitiousBeesConfig.respectClaims()) {
			return true;
		}
		Player actor = housing instanceof GunHousing gun ? gun.shooter() : FakePlayerFactory.get(level, PROFILE);
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, actor);
		NeoForge.EVENT_BUS.post(event);
		return !event.isCanceled();
	}
}
