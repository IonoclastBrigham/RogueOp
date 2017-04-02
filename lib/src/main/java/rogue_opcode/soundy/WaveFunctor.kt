// WaveFunctor.kt
// Wave function generator classes
//
// Copyright © 2010-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.soundy

import java.util.*


abstract class WaveFunctor : WaveSource() {
	companion object {
		var sRand = Random()
	}
}


internal class PulseWave : WaveSource() {
	/**
	 * Dynamically generates a single audio sample.
	 * @param pPhase phase of the sample to get.
	 * @return the requested sample.
	 */
	override fun get_sample(pPhase: Float): Float {
		return if(pPhase < WaveSource.sSampleRate * mDuty) 1.0f else -1.0f
	}
}


internal class WhiteNoise : WaveFunctor() {
	/**
	 * Gets a sample of white noise.
	 * @param pPhase phase of the sample to get.
	 * @return the requested sample.
	 */
	override fun get_sample(pPhase: Float) = sRand.nextFloat() * 2.0f - 1.0f
}


internal class FMNoise : WaveFunctor() {
	/**
	 * Gets a sample of FM noise.
	 * @param pPhase phase of the sample to get.
	 * @return the requested sample.
	 */
	override fun get_sample(pPhase: Float) = if(sRand.nextBoolean()) 1f else -1f
}


internal class PinkNoise : WaveFunctor() {
	// noise state
	private var mDuration = 0
	private var mNoiseFlip = 1f
	private var mNoiseAmp = 0f

	/**
	 * Gets a sample of pink noise.
	 * @param pPhase phase of the sample to get.
	 * @return the requested sample.
	 */
	override fun get_sample(pPhase: Float): Float {
		// min frequency at 100Hz, max frequency at Nyquist
		if(mDuration == 0) {
			val tMax = WaveSource.sSampleRate / 400
			mDuration = sRand.nextInt(tMax) + 1
			mNoiseAmp = mNoiseFlip * (mDuration / tMax + 1).toFloat() * 0.5f
		}
		if(--mDuration == 0) mNoiseFlip *= -1.0f

		return mNoiseAmp
	}
}
