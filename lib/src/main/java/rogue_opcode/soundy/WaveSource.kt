// WaveSource.kt
// WaveSource class
//
// Copyright © 2010-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <https://github.com/IonoclastBrigham/RogueOp/blob/master/LICENSE.md>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.soundy

import java.util.*


/**

 * @author Brigham Toskin
 */
abstract class WaveSource protected constructor() {
	companion object {
		var sFormat = 8
		var sSampleRate = 11025

		fun ICanHas(pWF: WaveForm): WaveSource {
			when(pWF) {
				WaveSource.WaveForm.PULSE -> return PulseWave()
				WaveSource.WaveForm.NOISE_AM -> return WhiteNoise()
				WaveSource.WaveForm.NOISE_FM -> return FMNoise()
				WaveSource.WaveForm.NOISE_PINK -> return PinkNoise()
				WaveSource.WaveForm.SAW, WaveSource.WaveForm.TRIANGLE, WaveSource.WaveForm.SIN, WaveSource.WaveForm.TAN, WaveSource.WaveForm.HEMICYCLE -> return WaveTabler(pWF)
				else -> throw RuntimeException("Invalid enumeration value")
			}
		}

		/**
		 * Applies amplitude modulation to one sample.

		 * @param pW the wave sample to modulate.
		 * *
		 * @param pLFO the modulating lfo sample.
		 * *
		 * @return the modulated sample.
		 */
		protected fun amp_mod(pW: Float, pLFO: Float): Float {
			return pW * ((pLFO + 1.0f) * 0.5f) // times normalized lfo amp
		}

		/**
		 * Mixes two input samples by taking their mean.

		 * @param pW1 first sample to mix.
		 * *
		 * @param pW2 second sample to mix.
		 * *
		 * @return the mixed sample.
		 */
		protected fun mix_mean(pW1: Float, pW2: Float): Float {
			return (pW1 + pW2) / 2.0f
		}

		/**
		 * Clips the signal to the range [-1.0, 1.0] inclusive.

		 * @param pWavAmp the wave sample to clip.
		 * *
		 * @return The clipped sample.
		 */
		fun clip(pWavAmp: Float): Float {
			return if(pWavAmp > 1.0f) 1.0f else if(pWavAmp < -1.0f) -1.0f else pWavAmp
		}

		// utility methods /////////////////////////////////////////////////////////

		/**
		 * Utility method to calculate the wavelength period in samples, based on
		 * output sample rate and requested tone frequency.

		 * @param pSampleRate current signal output sample rate.
		 * *
		 * @param pFreq frequency of tone context.
		 * *
		 * @return the wave period, in samples.
		 */
		fun Period(pSampleRate: Int, pFreq: Float): Float {
			return pSampleRate / pFreq
		}

		/**
		 * Wraps a phase value to make sure it maps to a wavetable index and is
		 * contiguous with the previous chunk.

		 * @param pPhase current phase value to wrap.
		 * *
		 * @return the wrapped phase.
		 */
		protected fun wrap_around(pPhase: Float): Float {
			var pPhase = pPhase
			val tChunk = sSampleRate.toFloat()
			while(pPhase >= tChunk)
				pPhase -= tChunk
			while(pPhase < 0.0000f)
				pPhase += tChunk
			return pPhase
		}

		/**
		 * Wraps a phase value to make sure it maps to a wavetable index in the
		 * first half of the dutycycle and is contiguous with the previous chunk.

		 * @param pPhase current phase value to wrap.
		 * *
		 * @param pDuty dutycycle of this waveform.
		 * *
		 * @return the wrapped phase.
		 */
		protected fun wrap_first(pPhase: Float, pDuty: Float): Float {
			var pPhase = pPhase
			val tChunk = sSampleRate * pDuty
			while(pPhase >= tChunk)
				pPhase -= tChunk
			while(pPhase < 0.0000f)
				pPhase += tChunk
			return pPhase
		}

		/**
		 * Wraps a phase value to make sure it maps to a wavetable index in the
		 * second half of the dutycycle and is contiguous with the previous chunk.

		 * @param pPhase current phase value to wrap.
		 * *
		 * @param pDuty dutycycle of this waveform.
		 * *
		 * @return the wrapped phase.
		 */
		protected fun wrap_second(pPhase: Float, pDuty: Float): Float {
			var pPhase = pPhase
			val tChunk = sSampleRate * (1 - pDuty)
			while(pPhase >= sSampleRate)
				pPhase -= tChunk
			while(pPhase < sSampleRate - tChunk)
				pPhase += tChunk
			return pPhase
		}
	}

	/**
	 * Specifies the type of modulation to apply to a synthesized waveform.
	 */
	enum class WaveModulation {
		NONE, AMPMOD, FREQMOD, PHASEMOD, DUTYMOD,
		AMPSLIDE, FREQSLIDE, PHASESLIDE, DUTYSLIDE
	}


	var mPhase: Float = 0.toFloat()
	var mDuty: Float = 0.toFloat()
	var mEnvelope: ADSHR? = null

	// c'tor, etc. /////////////////////////////////////////////////////////////

	/**
	 * Specifies the type of waveform to generate. <br></br> [ PULSE][WaveForm.PULSE], [NOISE_AM][WaveForm.NOISE_AM], [ NOISE_FM][WaveForm.NOISE_FM], and [NOISE_PINK][WaveForm.NOISE_PINK] are available for
	 * dynamic generation.<br></br>
	 * Wavetables are available for [SAW][WaveForm.SAW],
	 * [TRIANGLE][WaveForm.TRIANGLE], [SIN][WaveForm.SIN],
	 * [TAN][WaveForm.TAN], and [HEMICYCLE][WaveForm.HEMICYCLE].
	 */
	enum class WaveForm {
		/**
		 * Generates a pulse wave; a dutycycle of `.5` will result in a
		 * square wave, and a random dutycycle will result in FM white noise.
		 */
		PULSE,
		/** Generates amplitude modulated white noise.  */
		NOISE_AM,
		/** Generates frequency modulated noise.  */
		NOISE_FM,
		/** Generates FM pink noise.  */
		NOISE_PINK,
		/** Generates a sawtooth signal.  */
		SAW,
		/**
		 * Generates a triangle wave; a dutycycle of `1.0` will result in
		 * a sawtooth, and `0.0` will result in a reverse sawtooth.
		 */
		TRIANGLE,
		/** Generates a sine wave.  */
		SIN,
		/** Generates a clipped tangent signal.  */
		TAN,
		/** Generates a hemicycle signal.  */
		HEMICYCLE
	}

	init {
		mPhase = 0f
		mDuty = 0.5f
	}

	// signal generation ///////////////////////////////////////////////////////

	/**
	 * Implement this method to return the appropriate raw, full amplitude
	 * sample at the given phase offset. This will be called in a loop, so make
	 * it as efficient as possible.
	 * <br></br><br></br>
	 * `get_sample()` returns only a single sample, rather than
	 * filling the entire array with samples and then applying transformations
	 * to those. This may seem inefficient, but it is necessary because the
	 * frequency and duty cycle may be modulated dynamically. The tradeoff here
	 * is flexibility and power at the expense of some efficiency.

	 * @param pPhase the phase offset of the sample to retrieve.
	 * *
	 * @return the next sample.
	 */
	protected abstract fun get_sample(pPhase: Float): Float

	/**
	 * Master sample generator for functional and wavetable synthesis. This is a
	 * very complicated method to use; normally you won't want to call this
	 * directly, instead using one of the interfaces with a simpler call
	 * signature which in turn calls `SynthMaster()`. However, it is
	 * being made public for advanced users and for interfacing external classes
	 * and adaptors that in turn make synthesis programming simpler.

	 * @param oStream reference to output audio stream buffer. This buffer can
	 * *		be any mSize, and does not need to comply to any limitations
	 * *		imposed by the underlying audio output system. It is assumed that
	 * *		this buffer will be mixed down later before being pushed to sound
	 * *		hardware, so buffers may be as large or small as is convenient.
	 * *
	 * @param pFreq the output tone frequency, in Hz.
	 * *
	 * @param pPhaseOffset starting phase offset in the range [0 , 1.0].
	 * *
	 * @param pVol output volume in the range [0 , 1.0].
	 * *
	 * @param pWaveMod waveform modulation mode. This parameter affects how the
	 * *		generated waveform is affected by the signal `pLFO`.
	 * *
	 * @param pLFO low-frequency oscillator signal, used to modulate the
	 * *		generated waveform.
	 * *
	 * @param pModArg prescales effect of lfo.
	 * *
	 * @param pSlideTo slide end value for linear parameter attenuation.
	 * *
	 * @param pSlideTime time in samples it takes for the modulated parameter
	 * *		to slide from its original specified value to pSlideArg.
	 * *
	 * @param pNoteOn flag to turn the output on or off.
	 */
	fun SynthMaster(oStream: FloatArray, pFreq: Float, pPhaseOffset: Float,
					pVol: Float, pWaveMod: WaveModulation, pLFO: FloatArray?, pModArg: Float,
					pSlideTo: Float, pSlideTime: Float, pNoteOn: Boolean) {
		var pFreq = pFreq
		// precalc envelope to see if we need to turn pFreq back on
		var tAmp = 1.0f
		if(mEnvelope != null) {
			pFreq = mEnvelope!!.Calculate(pFreq, pNoteOn)
			tAmp = mEnvelope!!.mAmp
		} else if(pFreq.toDouble() == 0.0 || !pNoteOn) {
			Arrays.fill(oStream, 0.0f)
			return
		}// ensure a reasonable "no signal" value and bail

		// precalculate slide step
		var tSlideStep = 0f
		when(pWaveMod) {
			WaveSource.WaveModulation.AMPSLIDE -> tSlideStep = (pSlideTo - 1.0f) / pSlideTime
			WaveSource.WaveModulation.FREQSLIDE -> tSlideStep = (pSlideTo - pFreq) / pSlideTime
			WaveSource.WaveModulation.DUTYSLIDE -> tSlideStep = (pSlideTo - mDuty) / pSlideTime
			WaveSource.WaveModulation.PHASESLIDE -> tSlideStep = (pSlideTo - mPhase) / pSlideTime
		}

		// generate each sample
		val tPeriod = Period(sSampleRate, pFreq)
		var tPortion: Float
		var tPhase: Float
		var tDuty = mDuty
		for(i in oStream.indices) {
			// predomulate dutycycle
			if(pWaveMod == WaveModulation.DUTYMOD) {
				tDuty = mix_mean(tDuty, amp_mod(1.0f, pLFO!![i] * pModArg))
				tDuty = wrap_around(tDuty)
			} else if(pWaveMod == WaveModulation.DUTYSLIDE)
				tDuty += tSlideStep

			// premodulate phaseshift
			tPhase = wrap_around(mPhase)
			if(pWaveMod == WaveModulation.PHASEMOD)
				tPhase = tPhase + sSampleRate.toFloat() * pLFO!![i] * pModArg
			else if(pWaveMod == WaveModulation.PHASESLIDE)
				tPhase += tSlideStep
			else
				tPhase = tPhase + sSampleRate * pPhaseOffset
			tPhase = wrap_around(tPhase)

			// handle envelope calculations
			if(mEnvelope != null) {
				mEnvelope!!.Calculate(pFreq, pNoteOn)
				tAmp = mEnvelope!!.mAmp
			}

			// grab and attenuate sample
			oStream[i] = get_sample(tPhase) * pVol * tAmp


			// step to next sample, based on dutycycle
			tPhase = mPhase
			if(tPhase < sSampleRate / 2.0)
			// in first halfperiod
			{
				if(tDuty.toDouble() == 0.0)
				//we should never be here; wrap
				{
					tPhase = wrap_second(tPhase, tDuty)
					tPortion = 1.0f - tDuty
				} else
					tPortion = tDuty
			} else
			// in second halfperiod
			{
				if(tDuty.toDouble() == 1.0)
				// we should never be here; wrap
				{
					tPhase = wrap_first(tPhase, tDuty)
					tPortion = tDuty
				} else
					tPortion = 1.0f - tDuty
			}
			// equivalent to pFreq, when duty == .5
			mPhase += sSampleRate / (2f * tPortion * tPeriod)

			// handle modulation
			when(pWaveMod) {
				WaveSource.WaveModulation.AMPMOD -> oStream[i] = amp_mod(oStream[i], pLFO!![i] * pModArg)
				WaveSource.WaveModulation.FREQMOD -> tPhase += pLFO!![i] * pModArg
				WaveSource.WaveModulation.AMPSLIDE -> oStream[i] = amp_mod(oStream[i], pLFO!![i] * pModArg)
				WaveSource.WaveModulation.FREQSLIDE -> tPhase += pLFO!![i] * pModArg
				else // PM, DM handled above
				-> {
				}
			}
		}
	}


	/**
	 * If you just want to play a simple tone of a particular wave type, this is
	 * the method you probably want. It doesn't offer any parameter modulation
	 * or slide features. It will, however, respect your envelope if you have
	 * defined one for this oscillator.

	 * @param oStream out param for stream buffer to write to.
	 * *
	 * @param pFreq frequency of wave to synthesize.
	 * *
	 * @param pPhase phase offset of wave to synthesize.
	 * *
	 * @param pVol output volume of wave to synthesize.
	 * *
	 * @param pNoteOn analogous to whether keyboard key is being held down. This
	 * *		is used when calculating the envelope.
	 */
	fun SynthBasic(oStream: FloatArray, pFreq: Float, pPhase: Float,
				   pVol: Float, pNoteOn: Boolean) {
		SynthMaster(oStream, pFreq, pPhase, pVol,
				WaveModulation.NONE, null, 0f, 0f, 0f, pNoteOn)
	}

	// LFO modulated synthesis //

	fun SynthPhaseMod(oStream: FloatArray, pFreq: Float, pPhase: Float,
					  pVol: Float, pLFO: FloatArray, pModArg: Float, pNoteOn: Boolean) {
		SynthMaster(oStream, pFreq, pPhase, pVol,
				WaveModulation.PHASEMOD, pLFO, pModArg, 0f, 0f, pNoteOn)
	}

	fun SynthDutyMod(oStream: FloatArray, pFreq: Float, phase: Float,
					 pVol: Float, pLFO: FloatArray, pModArg: Float, pNoteOn: Boolean) {
		SynthMaster(oStream, pFreq, phase, pVol,
				WaveModulation.DUTYMOD, pLFO, pModArg, 0f, 0f, pNoteOn)
	}

	internal fun SynthAmpMod(oStream: FloatArray, pFreq: Float, phase: Float,
							 pVol: Float, pLFO: FloatArray, pModArg: Float, pNoteOn: Boolean) {
		SynthMaster(oStream, pFreq, phase, pVol,
				WaveModulation.AMPMOD, pLFO, pModArg, 0f, 0f, pNoteOn)
	}

	internal fun SynthFreqMod(oStream: FloatArray, pFreq: Float, phase: Float,
							  pVol: Float, pLFO: FloatArray, pModArg: Float, pNoteOn: Boolean) {
		SynthMaster(oStream, pFreq, phase, pVol,
				WaveModulation.FREQMOD, pLFO, pModArg, 0f, 0f, pNoteOn)
	}
}
