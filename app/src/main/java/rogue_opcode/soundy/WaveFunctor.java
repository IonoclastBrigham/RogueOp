// WaveFuctor.java
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


package rogue_opcode.soundy;

import java.util.Random;


public abstract class WaveFunctor extends WaveSource
{
	protected static Random sRand;

	/**
	 *
	 */
	public WaveFunctor()
	{
		if(sRand == null)
			sRand = new Random();
	}
}


class PulseWave extends WaveSource
{
	/**
	 * Dynamically generates a single audio sample.
	 * 
	 * @param pPhase phase of the sample to get.
	 * @return the requested sample.
	 */
	@Override
	protected float get_sample(float pPhase)
	{
		return (pPhase < sSampleRate * mDuty) ? 1.0f : -1.0f;
	}
}


class WhiteNoise extends WaveFunctor
{
	/**
	 *
	 */
	public WhiteNoise()
	{
		super();
	}

	/**
	 * Gets a sample of white noise.
	 *
	 * @param pPhase phase of the sample to get.
	 * @return the requested sample.
	 */
	@Override
	protected float get_sample(float pPhase)
	{
		return sRand.nextFloat() * 2.0f - 1.0f;
	}

}


class FMNoise extends WaveFunctor
{
	/**
	 *
	 */
	public FMNoise()
	{
		if(sRand == null)
			sRand = new Random();
	}

	/**
	 * Gets a sample of FM noise.
	 *
	 * @param pPhase phase of the sample to get.
	 * @return the requested sample.
	 */
	@Override
	protected float get_sample(float pPhase)
	{
		return sRand.nextLong() > sRand.nextLong() ? 1 : -1;
	}

}


class PinkNoise extends WaveFunctor
{
	// noise state
	protected int mDuration;
	protected float mNoiseFlip, mNoiseAmp;

	/**
	 *
	 */
	public PinkNoise()
	{
		if(sRand == null)
			sRand = new Random();
		mDuration = 0;
		mNoiseFlip = 1.0f;
	}

	/**
	 * Gets a sample of pink noise.
	 *
	 * @param pPhase phase of the sample to get.
	 * @return the requested sample.
	 */
	@Override
	protected float get_sample(float pPhase)
	{
		float tSample;

		// min frequency at 100Hz, max frequency at Nyquist
		if(mDuration == 0)
		{
			int tMax = sSampleRate / 400;
			mDuration = sRand.nextInt(tMax) + 1;
			mNoiseAmp = mNoiseFlip * (mDuration / tMax + 1) * 0.5f;
		}
		tSample = mNoiseAmp;
		if(--mDuration == 0)
			mNoiseFlip *= -1.0f;

		return tSample;
	}

}
