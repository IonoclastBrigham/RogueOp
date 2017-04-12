// Soundy.kt
// Soundy synthesizer framework
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

import rogue_opcode.AudioResource


/**
 * Soundy is a high-level encapsulation of the synthesis code. It presents an
 * interface more in line with a media player.

 * @author Brigham Toskin
 */
class Soundy
/**
 * @param pResID the raw resource ID to load.
 */
(pResID: Int)// TODO: read input file and allocate the above audio classes
	: AudioResource(pResID) {
	// These are the audio player guts.
	/*
	 * protected AudioTrack mChannel1;
	 * protected AudioTrack mChannel2;
	 * protected AudioTrack mChannel3;
	 * protected AudioTrack mChannel4;
	 * protected WaveSource mSynth1, mLFO1;
	 * protected WaveSource mSynth2, mLFO2;
	 * protected WaveSource mSynth3, mLFO3;
	 * protected WaveSource mSynth4, mLFO4;
	 */
	protected var mSynth1: Oscillator? = null
	protected var mSynth2: Oscillator? = null
	protected var mSynth3: Oscillator? = null
	protected var mSynth4: Oscillator? = null

	override fun Loop(): Boolean {
		// TODO Auto-generated method stub
		return false
	}

	override fun Loop(pLoop: Boolean) {
		// TODO Auto-generated method stub
	}

	override fun Pause() {
		// TODO Auto-generated method stub
	}

	override fun Play() {
		// TODO Auto-generated method stub
	}

	override fun Resume() {
		// TODO Auto-generated method stub
	}

	override fun Stop() {
		// TODO Auto-generated method stub
	}

	override fun die() {
		// TODO Auto-generated method stub
	}

}
