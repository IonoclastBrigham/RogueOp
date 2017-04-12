// WaveTable.kt
// WaveTable class
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


/**
 * Generates and buffers 1 cycle of various waveforms at 1Hz in normalized
 * floating point format (amplitudes over the range [-1.0,1.0] inclusive).

 * @author Brigham Toskin
 */
open class WaveTabler
/**
 * Constructs an instance with an internal wavetable buffer of an
 * appropriate mSize to hold one cycle at 1 Hz at the specified sample rate.

 * @param pType the type of waveform to create a table for.
 */
(pType: WaveSource.WaveForm) : WaveSource() {
	private lateinit var mWaveData: Array<Float>

	init {
		generate_table(pType)
	}

	/**
	 * Returns the next sample from the wavetable at the specified phase offset.
	 * @param pPhase the phase offset of the sample to retrieve.
	 * @return the next sample.
	 */
	override fun get_sample(pPhase: Float) = mWaveData[Math.floor(pPhase.toDouble()).toInt()]

	// table generation ////////////////////////////////////////////////////////

	/**
	 * @param pType the type of waveform to create a table for.
	 */
	protected fun generate_table(pType: WaveSource.WaveForm) {
		mWaveData = when(pType) {
			WaveSource.WaveForm.SAW -> generate_saw()
			WaveSource.WaveForm.TRIANGLE -> generate_triangle()
			WaveSource.WaveForm.SIN -> generate_sin()
			WaveSource.WaveForm.TAN -> generate_tan()
			WaveSource.WaveForm.HEMICYCLE -> generate_hemicycle()
			else -> throw IllegalArgumentException("Unsupported WaveForm type $pType")
		}
	}

	protected fun generate_saw(): Array<Float> {
		val tPeriod = WaveSource.Period(WaveSource.sSampleRate, 1f)
		return Array(WaveSource.sSampleRate) { i -> i / tPeriod * 2 - 1 }
	}

	protected fun generate_triangle(): Array<Float> {
		val tPeriod = WaveSource.Period(WaveSource.sSampleRate, 1f)
		val tHalfPeriod = tPeriod / 2
		return Array(WaveSource.sSampleRate) { i ->
			(if (i <= tHalfPeriod) i / tHalfPeriod else (tPeriod - i) / tHalfPeriod) * 2 - 1
		}
	}

	protected fun generate_sin(): Array<Float> {
		val tPeriod = WaveSource.Period(WaveSource.sSampleRate, 1f)
		return Array(WaveSource.sSampleRate) { i ->
			val tTheta = 2.0f * Math.PI.toFloat() * (i / tPeriod)
			Math.sin(tTheta.toDouble()).toFloat()
		}
	}

	protected fun generate_tan(): Array<Float> {
		val tPeriod = WaveSource.Period(WaveSource.sSampleRate, 1f)
		var tTheta: Float
		return Array(WaveSource.sSampleRate) { i ->
			tTheta = 2.0f * Math.PI.toFloat() * (i / tPeriod)
			WaveSource.clip((Math.tan(tTheta.toDouble()) / (4 * Math.PI)).toFloat())
		}
	}

	protected fun generate_hemicycle(): Array<Float> {
		val tPeriod = WaveSource.Period(WaveSource.sSampleRate, 1f)
		val tHalfPeriod = tPeriod * 0.5f
		val tQuarterPeriod = tHalfPeriod * 0.5f
		var tSample: Float
		var tPos: Int
		return Array(WaveSource.sSampleRate) { i ->
			tPos = i % Math.round(tHalfPeriod)
			tSample = Math.sqrt((tQuarterPeriod * tQuarterPeriod - (tPos - tQuarterPeriod) * (tPos - tQuarterPeriod)).toDouble()).toFloat() // about 0
			tSample *= 1.0f / tQuarterPeriod // scale
			if(i > tHalfPeriod) tSample *= -1f
			tSample
		}
	}
}
