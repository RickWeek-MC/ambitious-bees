package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;

/** Plays a little jazz (pentatonic notes on random note block instruments); a bigger territory means a bigger band. */
public class BensenBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<BensenBeeEffect> CODEC = makeCodec(12, BensenBeeEffect::new);

	public BensenBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		if (level.random.nextInt(3) != 0) {
			return;
		}
		// The band grows with the territory: one musician per few blocks of its side length, at most four.
		double side = Math.cbrt(Math.max(1.0, area.getXsize() * area.getYsize() * area.getZsize()));
		int musicians = Math.max(1, Math.min(MAX_BAND, (int) (side / 4.0)));
		for (BlockPos pos : randomPositions(level, area, musicians)) {
			SoundEvent instrument = INSTRUMENTS.get(level.random.nextInt(INSTRUMENTS.size()));
			int semitone = SCALE[level.random.nextInt(SCALE.length)];
			float pitch = (float) Math.pow(2.0, (semitone - 12) / 12.0);
			level.playSound(null, pos, instrument, SoundSource.BLOCKS, 1.0f, pitch);
			level.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 1, 0.3, 0.1, 0.3, semitone / 24.0);
		}
	}

	private static final int MAX_BAND = 4;
	private static final int[] SCALE = {0, 3, 5, 7, 10, 12, 15, 17, 19, 22, 24};
	private static final List<SoundEvent> INSTRUMENTS = List.of(
			SoundEvents.NOTE_BLOCK_HARP.value(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundEvents.NOTE_BLOCK_GUITAR.value(),
			SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundEvents.NOTE_BLOCK_BELL.value(),
			SoundEvents.NOTE_BLOCK_PLING.value());
}
