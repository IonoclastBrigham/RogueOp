// WaveTable.java
// WaveTable class
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


/**
 * Generates and buffers 1 cycle of various waveforms at 1Hz in normalized
 * floating point format (amplitudes over the range [-1.0,1.0] inclusive).
 *
 * @author Brigham Toskin
 */
public class WaveTabler extends WaveSource
{
	protected static float[] sSawData;
	protected static float[] sTriData;
	protected static float[] sSinData;
	protected static float[] sTanData;
	protected static float[] sHemicycleData;

	public float[] mWaveData;

	/**
	 * Constructs an instance with an internal wavetable buffer of an
	 * appropriate size to hold one cycle at 1 Hz at the specified sample rate.
	 *
	 * @param pType the type of waveform to create a table for.
	 */
	public WaveTabler(WaveForm pType)
	{
		super();
		generate_table(pType);
	}

	/**
	 * Returns the next sample from the wavetable at the specified phase offset.
	 *
	 * @param pPhase the phase offset of the sample to retrieve.
	 * @return the next sample.
	 */
	@Override
	protected float get_sample(float pPhase)
	{
		float tSample = 0;
		tSample = mWaveData[(int)Math.floor(pPhase)];
		return tSample;
	}

	// table generation ////////////////////////////////////////////////////////

	/**
	 * Implement this method to generate the appropriate wave function values.
	 * This method should have an internal loop to fill the entire
	 * <code>mWaveData</code>array based on the current member parameters.
	 * 
	 * @param pType the type of waveform to create a table for.
	 */
	protected void generate_table(WaveForm pType)
	{
		switch(pType)
		{
		case SAW:
			if(sSawData == null)
			{
				sSawData = new float[sSampleRate];
				generate_saw();
			}
			mWaveData = sSawData;
			break;
		case TRIANGLE:
			if(sTriData == null)
			{
				sTriData = new float[sSampleRate];
				generate_triangle();
			}
			mWaveData = sTriData;
			break;
		case SIN:
			if(sSinData == null)
			{
				sSinData = new float[sSampleRate];
				generate_sin();
			}
			mWaveData = sSinData;
			break;
		case TAN:
			if(sTanData == null)
			{
				sTanData = new float[sSampleRate];
				generate_tan();
			}
			mWaveData = sTanData;
			break;
		case HEMICYCLE:
			if(sHemicycleData == null)
			{
				sHemicycleData = new float[sSampleRate];
				generate_hemicycle();
			}
			mWaveData = sHemicycleData;
			break;
		}
	}

	protected void generate_saw()
	{
		final float tPeriod = Period(sSampleRate, 1);
		for(int i = 0; i < sSampleRate; i++)
			sSawData[i] = (i / tPeriod) * 2 - 1;
	}

	protected void generate_triangle()
	{
		final float tPeriod = Period(sSampleRate, 1);
		final float tHalfPeriod = tPeriod / 2;
		for(int i = 0; i < sSampleRate; i++)
			sTriData[i] = (i <= tHalfPeriod ? i / tHalfPeriod :
				(tPeriod - i) / tHalfPeriod ) * 2 - 1;
	}

	protected void generate_sin()
	{
		final float tPeriod = Period(sSampleRate, 1);
		float tTheta;
		for(int i = 0; i < sSampleRate; i++)
		{
			tTheta = 2.0f * (float)Math.PI * (i / tPeriod);
			sSinData[i] = (float)Math.sin(tTheta);
		}
	}

	protected void generate_tan()
	{
		final float tPeriod = Period(sSampleRate, 1);
		float tTheta;
		for(int i = 0; i < sSampleRate; i++)
		{
			tTheta = 2.0f * (float)Math.PI * (i / tPeriod);
			sTanData[i] = clip((float)(Math.tan(tTheta) / (4 * Math.PI)));
		}
	}

	protected void generate_hemicycle()
	{
		final float tPeriod = Period(sSampleRate, 1);
		final float tHalfPeriod = tPeriod * 0.5f;
		final float tQuarterPeriod = tHalfPeriod * 0.5f;
		float tSample;
		int tPos;
		for(int i = 0; i < sSampleRate; i++)
		{
			tPos = i % Math.round(tHalfPeriod);
			tSample = (float)Math.sqrt(tQuarterPeriod * tQuarterPeriod
				- (tPos - tQuarterPeriod) * (tPos - tQuarterPeriod)); // about 0
			tSample *= (1.0f / tQuarterPeriod); // scale
			if(i > tHalfPeriod)
				tSample = -tSample;
			sHemicycleData[i] = tSample;
		}
	}
}
