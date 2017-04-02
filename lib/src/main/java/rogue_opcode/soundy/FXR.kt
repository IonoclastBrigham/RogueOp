// FXR.kt
// SFXR file loader and playback class
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

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import rogue_opcode.AudioResource
import rogue_opcode.GameProc


/**
 * This class is for loading and playing back synthesis parameter files created
 * by the open source sound effect generator, SFXR. It supports file formats up
 * through version 102, in addition to some more advanced/standard envelope
 * functionality.
 *
 * FXR can load and pre-render the synthesized audio once for fast, efficient
 * playback at runtime. Or, it can be configured to apply a small amount of
 * randomness to the synthesis parameters and regenerate the sound every time it
 * is played; this will lead to a more diverse soundscape, delivering a more
 * interesting and realistic experience to players at the expense of increased
 * processing overhead.
 *
 * @author Brigham Toskin
 */
class FXR
/**
 * *Constructs an instance for the specified resource.*
 * @param pResID raw resource ID of the SFXR audio file.
 * @param pRandomize whether to randomize the sound params on playback.
 */
@JvmOverloads constructor(pResID: Int, protected var mRandomize: Boolean = false) : AudioResource(pResID) {
	private var mOutStream: AudioTrack

	private var mSynth: WaveSource? = null
	private lateinit var mData: ByteArray

	init {
		val tRes = GameProc.sOnly.resources
		tRes.openRawResource(pResID).buffered().use { tStream ->
			mData = tStream.readBytes()
		}

		// TODO: deduce the kind of WaveSource from file
		mOutStream = AudioTrack(AudioManager.STREAM_MUSIC,
				WaveSource.sSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				WaveSource.sFormat, mData.size,
				if(mRandomize) AudioTrack.MODE_STREAM else AudioTrack.MODE_STATIC)
	}

	// playback interfaces /////////////////////////////////////////////////////

	override fun Loop(): Boolean {
		// TODO Auto-generated method stub
		return false
	}

	override fun Loop(pLoop: Boolean) {
		// TODO Auto-generated method stub
	}

	override fun Pause() {

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
