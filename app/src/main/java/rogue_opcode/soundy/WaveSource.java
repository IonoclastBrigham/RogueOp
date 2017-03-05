// WaveSource.java
// WaveSource class
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

import java.util.Arrays;



/**
 *
 * @author Brigham Toskin
 */
public abstract class WaveSource
{
	public static int sFormat = 8;
	public static int sSampleRate = 11025;

	public float mPhase;
	public float mDuty;
	public ADSHR mEnvelope;

	// c'tor, etc. /////////////////////////////////////////////////////////////

	/**
	 * Specifies the type of waveform to generate. <br /> {@link WaveForm#PULSE
	 * PULSE}, {@link WaveForm#NOISE_AM NOISE_AM}, {@link WaveForm#NOISE_FM
	 * NOISE_FM}, and {@link WaveForm#NOISE_PINK NOISE_PINK} are available for
	 * dynamic generation.<br />
	 * Wavetables are available for {@link WaveForm#SAW SAW},
	 * {@link WaveForm#TRIANGLE TRIANGLE}, {@link WaveForm#SIN SIN},
	 * {@link WaveForm#TAN TAN}, and {@link WaveForm#HEMICYCLE HEMICYCLE}.
	 */
	public enum WaveForm
	{
		/**
		 * Generates a pulse wave; a dutycycle of {@code .5} will result in a
		 * square wave, and a random dutycycle will result in FM white noise.
		 */
		PULSE,
		/** Generates amplitude modulated white noise. */
		NOISE_AM,
		/** Generates frequency modulated noise. */
		NOISE_FM,
		/** Generates FM pink noise. */
		NOISE_PINK,
		/** Generates a sawtooth signal. */
		SAW,
		/**
		 * Generates a triangle wave; a dutycycle of {@code 1.0} will result in
		 * a sawtooth, and {@code 0.0} will result in a reverse sawtooth.
		 */
		TRIANGLE,
		/** Generates a sine wave. */
		SIN,
		/** Generates a clipped tangent signal. */
		TAN,
		/** Generates a hemicycle signal. */
		HEMICYCLE
	}

	public static WaveSource ICanHas(WaveForm pWF)
	{
		switch(pWF)
		{
		case PULSE:
			return new PulseWave();
		case NOISE_AM:
			return new WhiteNoise();
		case NOISE_FM:
			return new FMNoise();
		case NOISE_PINK:
			return new PinkNoise();
		case SAW:
		case TRIANGLE:
		case SIN:
		case TAN:
		case HEMICYCLE:
			return new WaveTabler(pWF);
		default:
			throw new RuntimeException("Invalid enumeration value");
		}
	}

	protected WaveSource()
	{
		mPhase = 0;
		mDuty = 0.5f;
	}

	// signal generation ///////////////////////////////////////////////////////

	/**
	 * Implement this method to return the appropriate raw, full amplitude
	 * sample at the given phase offset. This will be called in a loop, so make
	 * it as efficient as possible.
	 * <br /><br />
	 * <code>get_sample()</code> returns only a single sample, rather than
	 * filling the entire array with samples and then applying transformations
	 * to those. This may seem inefficient, but it is necessary because the
	 * frequency and duty cycle may be modulated dynamically. The tradeoff here
	 * is flexibility and power at the expense of some efficiency.
	 *
	 * @param pPhase the phase offset of the sample to retrieve.
	 * @return the next sample.
	 */
	protected abstract float get_sample(float pPhase);

	/**
	 * Master sample generator for functional and wavetable synthesis. This is a
	 * very complicated method to use; normally you won't want to call this
	 * directly, instead using one of the interfaces with a simpler call
	 * signature which in turn calls <code>SynthMaster()</code>. However, it is
	 * being made public for advanced users and for interfacing external classes
	 * and adaptors that in turn make synthesis programming simpler.
	 *
	 * @param oStream reference to output audio stream buffer. This buffer can
	 *        be any size, and does not need to comply to any limitations
	 *        imposed by the underlying audio output system. It is assumed that
	 *        this buffer will be mixed down later before being pushed to sound
	 *        hardware, so buffers may be as large or small as is convenient.
	 * @param pFreq the output tone frequency, in Hz.
	 * @param pPhaseOffset starting phase offset in the range [0 , 1.0].
	 * @param pVol output volume in the range [0 , 1.0].
	 * @param pWaveMod waveform modulation mode. This parameter affects how the
	 *        generated waveform is affected by the signal <code>pLFO</code>.
	 * @param pLFO low-frequency oscillator signal, used to modulate the
	 *        generated waveform.
	 * @param pModArg prescales effect of lfo.
	 * @param pSlideTo slide end value for linear parameter attenuation.
	 * @param pSlideTime time in samples it takes for the modulated parameter
	 *        to slide from its original specified value to pSlideArg.
	 * @param pNoteOn flag to turn the output on or off.
	 */
	public void SynthMaster(float[] oStream, float pFreq, float pPhaseOffset,
		float pVol, WaveModulation pWaveMod, float pLFO[], float pModArg,
		float pSlideTo, float pSlideTime, boolean pNoteOn)
	{
		// precalc envelope to see if we need to turn pFreq back on
		float tAmp = 1.0f;
		if(mEnvelope != null)
		{
			pFreq = mEnvelope.Calculate(pFreq, pNoteOn);
			tAmp = mEnvelope.mAmp;
		}
		// ensure a reasonable "no signal" value and bail
		else if(pFreq == 0.0 || !pNoteOn)
		{
			Arrays.fill(oStream, 0.0f);
			return;
		}

		// precalculate slide step
		float tSlideStep = 0;
		switch(pWaveMod)
		{
		case AMPSLIDE:
			tSlideStep = (pSlideTo - 1.0f) / pSlideTime;
			break;
		case FREQSLIDE:
			tSlideStep = (pSlideTo - pFreq) / pSlideTime;
			break;
		case DUTYSLIDE:
			tSlideStep = (pSlideTo - mDuty) / pSlideTime;
			break;
		case PHASESLIDE:
			tSlideStep = (pSlideTo - mPhase) / pSlideTime;
			break;
		}

		// generate each sample
		float tPeriod = Period(sSampleRate, pFreq);
		float tPortion, tPhase;
		float tDuty = mDuty;
		for(int i = 0; i < oStream.length; i++)
		{
			// predomulate dutycycle
			if(pWaveMod == WaveModulation.DUTYMOD)
			{
				tDuty = mix_mean(tDuty, amp_mod(1.0f, pLFO[i] * pModArg));
				tDuty = wrap_around(tDuty);
			}
			else if(pWaveMod == WaveModulation.DUTYSLIDE)
				tDuty += tSlideStep;

			// premodulate phaseshift
			tPhase = wrap_around(mPhase);
			if(pWaveMod == WaveModulation.PHASEMOD)
				tPhase = tPhase + (sSampleRate * pLFO[i] * pModArg);
			else if(pWaveMod == WaveModulation.PHASESLIDE)
				tPhase += tSlideStep;
			else
				tPhase = tPhase + (sSampleRate * pPhaseOffset);
			tPhase = wrap_around(tPhase);

			// handle envelope calculations
			if(mEnvelope != null)
			{
				mEnvelope.Calculate(pFreq, pNoteOn);
				tAmp = mEnvelope.mAmp;
			}

			// grab and attenuate sample
			oStream[i] = get_sample(tPhase) * pVol * tAmp;


			// step to next sample, based on dutycycle
			tPhase = mPhase;
			if(tPhase < sSampleRate / 2.0) // in first halfperiod
			{
				if(tDuty == 0.0) //we should never be here; wrap
				{
					tPhase = wrap_second(tPhase, tDuty);
					tPortion = 1.0f - tDuty;
				}
				else
					tPortion = tDuty;
			}
			else // in second halfperiod
			{
				if(tDuty == 1.0) // we should never be here; wrap
				{
					tPhase = wrap_first(tPhase, tDuty);
					tPortion = tDuty;
				}
				else
					tPortion = 1.0f - tDuty;
			}
			// equivalent to pFreq, when duty == .5
			mPhase += sSampleRate / (2 * tPortion * tPeriod);

			// handle modulation
			switch(pWaveMod)
			{
			case AMPMOD:
				oStream[i] = amp_mod(oStream[i], pLFO[i] * pModArg);
				break;
			case FREQMOD:
				tPhase += pLFO[i] * pModArg;
				break;
			case AMPSLIDE:
				oStream[i] = amp_mod(oStream[i], pLFO[i] * pModArg);
				break;
			case FREQSLIDE:
				tPhase += pLFO[i] * pModArg;
				break;
			default: // PM, DM handled above
				break;
			}
		}
	}


	/**
	 * If you just want to play a simple tone of a particular wave type, this is
	 * the method you probably want. It doesn't offer any parameter modulation
	 * or slide features. It will, however, respect your envelope if you have
	 * defined one for this oscillator.
	 * 
	 * @param oStream out param for stream buffer to write to.
	 * @param pFreq frequency of wave to synthesize.
	 * @param pPhase phase offset of wave to synthesize.
	 * @param pVol output volume of wave to synthesize.
	 * @param pNoteOn analogous to whether keyboard key is being held down. This
	 *        is used when calculating the envelope.
	 */
	public void SynthBasic(float[] oStream, float pFreq, float pPhase,
			float pVol, boolean pNoteOn)
	{
		SynthMaster(oStream, pFreq, pPhase, pVol,
			WaveModulation.NONE, null, 0, 0, 0, pNoteOn);
	}

	// LFO modulated synthesis //

	public void SynthPhaseMod(float[] oStream, float pFreq, float pPhase,
		float pVol, float pLFO[], float pModArg, boolean pNoteOn)
	{
		SynthMaster(oStream, pFreq, pPhase, pVol,
			WaveModulation.PHASEMOD, pLFO, pModArg, 0, 0, pNoteOn);
	}

	public void SynthDutyMod(float[] oStream, float pFreq, float phase,
		float pVol, float[] pLFO, float pModArg, boolean pNoteOn)
	{
		SynthMaster(oStream, pFreq, phase, pVol,
			 WaveModulation.DUTYMOD, pLFO, pModArg, 0, 0, pNoteOn);
	}

	void SynthAmpMod(float[] oStream, float pFreq, float phase,
		float pVol, float[] pLFO, float pModArg, boolean pNoteOn)
	{
		SynthMaster(oStream, pFreq, phase, pVol,
			WaveModulation.AMPMOD, pLFO, pModArg, 0, 0, pNoteOn);
	}

	void SynthFreqMod(float[] oStream, float pFreq, float phase,
		float pVol, float[] pLFO, float pModArg, boolean pNoteOn)
	{
		SynthMaster(oStream, pFreq, phase, pVol,
			WaveModulation.FREQMOD, pLFO, pModArg, 0, 0, pNoteOn);
	}

	// mix/mod /////////////////////////////////////////////////////////////////

	/**
	 * Specifies the type of modulation to apply to a synthesized waveform.
	 */
	public enum WaveModulation
	{
		NONE, AMPMOD, FREQMOD, PHASEMOD, DUTYMOD,
		AMPSLIDE, FREQSLIDE, PHASESLIDE, DUTYSLIDE;
	}

	/**
	 * Applies amplitude modulation to one sample.
	 *
	 * @param pW the wave sample to modulate.
	 * @param pLFO the modulating lfo sample.
	 * @return the modulated sample.
	 */
	protected static float amp_mod(float pW, float pLFO)
	{
		return pW * ((pLFO + 1.0f) * 0.5f); // times normalized lfo amp
	}

	/**
	 * Mixes two input samples by taking their mean.
	 *
	 * @param pW1 first sample to mix.
	 * @param pW2 second sample to mix.
	 * @return the mixed sample.
	 */
	protected static float mix_mean(float pW1, float pW2)
	{
		return (pW1 + pW2) / 2.0f;
	}

	/**
	 * Clips the signal to the range [-1.0, 1.0] inclusive.
	 *
	 * @param pWavAmp the wave sample to clip.
	 * @return The clipped sample.
	 */
	protected static float clip(float pWavAmp)
	{
		return pWavAmp > 1.0f ? 1.0f : (pWavAmp < -1.0f ? -1.0f : pWavAmp);
	}

	// utility methods /////////////////////////////////////////////////////////

	/**
	 * Utility method to calculate the wavelength period in samples, based on
	 * output sample rate and requested tone frequency.
	 *
	 * @param pSampleRate current signal output sample rate.
	 * @param pFreq frequency of tone context.
	 * @return the wave period, in samples.
	 */
	public static float Period(int pSampleRate, float pFreq)
	{
		return pSampleRate / pFreq;
	}

	/**
	 * Wraps a phase value to make sure it maps to a wavetable index and is
	 * contiguous with the previous chunk.
	 *
	 * @param pPhase current phase value to wrap.
	 * @return the wrapped phase.
	 */
	protected static float wrap_around(float pPhase)
	{
		float tChunk = sSampleRate;
		while(pPhase >= tChunk)
			pPhase -= tChunk;
		while(pPhase < 0.0000f)
			pPhase += tChunk;
		return pPhase;
	}

	/**
	 * Wraps a phase value to make sure it maps to a wavetable index in the
	 * first half of the dutycycle and is contiguous with the previous chunk.
	 *
	 * @param pPhase current phase value to wrap.
	 * @param pDuty dutycycle of this waveform.
	 * @return the wrapped phase.
	 */
	protected static float wrap_first(float pPhase, float pDuty)
	{
		float tChunk = sSampleRate * pDuty;
		while(pPhase >= tChunk)
			pPhase -= tChunk;
		while(pPhase < 0.0000f)
			pPhase += tChunk;
		return pPhase;
	}

	/**
	 * Wraps a phase value to make sure it maps to a wavetable index in the
	 * second half of the dutycycle and is contiguous with the previous chunk.
	 *
	 * @param pPhase current phase value to wrap.
	 * @param pDuty dutycycle of this waveform.
	 * @return the wrapped phase.
	 */
	protected static float wrap_second(float pPhase, float pDuty)
	{
		float tChunk = sSampleRate * (1 - pDuty);
		while(pPhase >= sSampleRate)
			pPhase -= tChunk;
		while(pPhase < sSampleRate - tChunk)
			pPhase += tChunk;
		return pPhase;
	}
}
