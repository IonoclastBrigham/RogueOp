// ADSR.java
// ADSR amplitude envelope class
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
 * Represents an ADSHR envelope. These stand for Attack, Decay, Sustain, Hold &
 * Release, respectively. By setting these parameters, you can control the
 * behavior of how note events are triggered.
 * <br /></br />
 * In more detail, Attack specifies how long it takes for a newly generated
 * tone to go from 0 to full amplitude. Decay, as you might suspect,
 * specified how long it takes the tone to fall off from full amplitude to
 * the sustain level. Sustain specifies what this amplitude is for
 * the generated tone as the note is held. Hold is once again a time period,
 * controlling how long the tone is held at the sustain amplitude. Finally,
 * Release specifies a time period, indicating how long it takes for the tone to
 * decay form the sustain amplitude back down to zero when a frequency
 * change or note-off event is triggered.
 * <br /><br />
 * This class is part of the Soundy library.
 */
public class ADSHR
{
	public int A, D, H, R;
	public float S;
	public float mAmp;
	public float mFreq;
	public int mElapsed;
	public EnvelopeState mState;

	public ADSHR()
	{
		A = D = H = R = mElapsed = 0;
		S = 0.0f;
		mAmp = mFreq = 0;
		mState = EnvelopeState.OFF;
	}

	public ADSHR(int pA, int pD, float pS, int pH, int pR)
	{
		A = pA;
		D = pD;
		S = pS;
		H = pH;
		R = pR;
		mAmp = mFreq = mElapsed = 0;
		mState = EnvelopeState.OFF;
	}

	// TODO: on/off method with a single calculate?

	public float Calculate(float pFreq, boolean pOn)
	{
		EnvelopeState tState = mState;

		if(pOn) // oscillator on
		{
			// reset envelope on pFreq change
			if(pFreq != mFreq || tState == EnvelopeState.OFF
					|| tState == EnvelopeState.RELEASE)
			{
				mState = EnvelopeState.ATTACK;
			}

			if(tState == EnvelopeState.ATTACK)
			{
				if(mAmp >= 1.0)
					mState = EnvelopeState.DECAY;
				else
					mAmp += 1.0 / A;
			}

			if(tState == EnvelopeState.DECAY)
			{
				if(mAmp <= S)
					mState = EnvelopeState.HOLD;
				else
					mAmp -= (1.0 - S) / D;
			}
		}
		else
		{
			if(tState != EnvelopeState.OFF) // note ended
			{
				mState = EnvelopeState.RELEASE;
			}

			if(tState == EnvelopeState.RELEASE)
			{
				if(mAmp <= 0.0001f)
				{
					mAmp = 0.0f;
					mState = EnvelopeState.OFF;
					pFreq = 0; // signal the gen functions to shortcircuit 0's
				}
				else
					mAmp -= S / R;
			}
		}
		return (mFreq = pFreq);
	}

	public float Calculate(float pFreq)
	{
		EnvelopeState tState = mState;

		// reset envelope on pFreq change
		if(pFreq != mFreq)
		{
			mElapsed = 0;
			mState = EnvelopeState.ATTACK;
		}

		if(tState == EnvelopeState.ATTACK)
		{
			if(mAmp >= 1.0)
				mState = EnvelopeState.DECAY;
			else
				mAmp += 1.0 / A;
		}

		if(tState == EnvelopeState.DECAY)
		{
			if(mAmp < S)
			{
				mAmp = S;
				mState = EnvelopeState.HOLD;
			}
			else
				mAmp -= (1.0 - S) / D;
		}

		if(tState == EnvelopeState.HOLD)
		{
			if(mElapsed++ == H - 1)
			{
				mElapsed = 0;
				mState = EnvelopeState.RELEASE;
			}
		}

		if(tState == EnvelopeState.RELEASE)
		{
			if(mAmp <= 0.0001f)
			{
				mAmp = 0.0f;
				mState = EnvelopeState.OFF;
				pFreq = 0; // signal the gen functions to shortcircuit 0's
			}
			else
				mAmp -= S / R;
		}

		return (mFreq = pFreq);
	}

	// inner types and classes /////////////////////////////////////////////////

	public enum EnvelopeState
	{
		OFF, ATTACK, DECAY, HOLD, RELEASE
	}
}
