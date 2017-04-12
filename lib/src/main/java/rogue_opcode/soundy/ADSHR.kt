// ADSR.kt
// ADSR amplitude envelope class
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
 * Represents an ADSHR envelope. These stand for Attack, Decay, Sustain, Hold &
 * Release, respectively. By setting these parameters, you can control the
 * behavior of how note events are triggered.
 * <br></br>
 * In more detail, Attack specifies how long it takes for a newly generated
 * tone to go from 0 to full amplitude. Decay, as you might suspect,
 * specified how long it takes the tone to fall off from full amplitude to
 * the sustain level. Sustain specifies what this amplitude is for
 * the generated tone as the note is held. Hold is once again a time period,
 * controlling how long the tone is held at the sustain amplitude. Finally,
 * Release specifies a time period, indicating how long it takes for the tone to
 * decay form the sustain amplitude back down to zero when a frequency
 * change or note-off event is triggered.
 * <br></br><br></br>
 * This class is part of the Soundy library.
 */
class ADSHR {
	var A: Int = 0
	var D: Int = 0
	var H: Int = 0
	var R: Int = 0
	var S: Float = 0.toFloat()
	var mAmp: Float = 0.toFloat()
	var mFreq: Float = 0.toFloat()
	var mElapsed: Int = 0
	var mState: EnvelopeState

	constructor() {
		mElapsed = 0
		R = mElapsed
		H = R
		D = H
		A = D
		S = 0.0f
		mFreq = 0f
		mAmp = mFreq
		mState = EnvelopeState.OFF
	}

	constructor(pA: Int, pD: Int, pS: Float, pH: Int, pR: Int) {
		A = pA
		D = pD
		S = pS
		H = pH
		R = pR
		mElapsed = 0
		mFreq = 0f
		mAmp = mFreq
		mState = EnvelopeState.OFF
	}

	// TODO: on/off method with a single calculate?

	fun Calculate(pFreq: Float, pOn: Boolean): Float {
		var tFreq = pFreq
		val tState = mState

		if(pOn)
		// oscillator on
		{
			// reset envelope on pFreq change
			if(pFreq != mFreq || tState == EnvelopeState.OFF
					|| tState == EnvelopeState.RELEASE) {
				mState = EnvelopeState.ATTACK
			}

			if(tState == EnvelopeState.ATTACK) {
				if(mAmp >= 1.0)
					mState = EnvelopeState.DECAY
				else
					mAmp += (1.0 / A).toFloat()
			}

			if(tState == EnvelopeState.DECAY) {
				if(mAmp <= S)
					mState = EnvelopeState.HOLD
				else
					mAmp -= ((1.0 - S) / D).toFloat()
			}
		} else {
			if(tState != EnvelopeState.OFF)
			// note ended
			{
				mState = EnvelopeState.RELEASE
			}

			if(tState == EnvelopeState.RELEASE) {
				if(mAmp <= 0.0001f) {
					mAmp = 0.0f
					mState = EnvelopeState.OFF
					tFreq = 0f // signal the gen functions to shortcircuit 0's
				} else
					mAmp -= S / R
			}
		}
		return tFreq.also { mFreq = it }
	}

	fun Calculate(pFreq: Float): Float {
		var tFreq = pFreq
		val tState = mState

		// reset envelope on pFreq change
		if(pFreq != mFreq) {
			mElapsed = 0
			mState = EnvelopeState.ATTACK
		}

		if(tState == EnvelopeState.ATTACK) {
			if(mAmp >= 1.0)
				mState = EnvelopeState.DECAY
			else
				mAmp += (1.0 / A).toFloat()
		}

		if(tState == EnvelopeState.DECAY) {
			if(mAmp < S) {
				mAmp = S
				mState = EnvelopeState.HOLD
			} else
				mAmp -= ((1.0 - S) / D).toFloat()
		}

		if(tState == EnvelopeState.HOLD) {
			if(mElapsed++ == H - 1) {
				mElapsed = 0
				mState = EnvelopeState.RELEASE
			}
		}

		if(tState == EnvelopeState.RELEASE) {
			if(mAmp <= 0.0001f) {
				mAmp = 0.0f
				mState = EnvelopeState.OFF
				tFreq = 0f // signal the gen functions to shortcircuit 0's
			} else
				mAmp -= S / R
		}

		return tFreq.also { mFreq = it }
	}

	// inner types and classes /////////////////////////////////////////////////

	enum class EnvelopeState {
		OFF, ATTACK, DECAY, HOLD, RELEASE
	}
}
