// MusicTrack.kt
// MusicTrack class
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


import android.media.MediaPlayer
import rogue_opcode.AudioResource
import rogue_opcode.GameProc


/**
 * This class represents and gives you access to longer audio tracks. It is
 * intended primarily for long-playing songs, rather than quick, low-latency
 * sound effects.
 *
 * **Usage:** Construct a `MusicTrack` instance, and start it with the
 * [Play()][MusicTrack.Play] method. You also have access to various
 * aspects of playback, such as volume, pan, and looping. You should expect some
 * latency both in loading and starting playback; You `MusicTrack`s should
 * be loaded early on during initialization, so they will be ready when they are
 * needed.
 *
 * @see AudioResource
 * @see SoundEffect
 * @author Brigham Toskin
 */
class MusicTrack(pResID: Int, private var mLoop: Boolean = false)
	: AudioResource(pResID) {

	private var mMediaPlayer: MediaPlayer = MediaPlayer.create(GameProc.sOnly, pResID)

	// c'tors //////////////////////////////////////////////////////////////////

	init {
		mMediaPlayer.isLooping = mLoop
	}

	/** Stops all playing tracks and frees their resources.  */
	override fun die() {
		mMediaPlayer.stop()
		mMediaPlayer.release()
	}

	// media control ///////////////////////////////////////////////////////////

	/**
	 * Play this track.
	 * @see rogue_opcode.AudioResource.Play
	 */
	override fun Play() = mMediaPlayer.start()

	/**
	 * Stop this track, if playing.
	 * @see rogue_opcode.AudioResource.Stop
	 */
	override fun Stop() = mMediaPlayer.stop()

	/**
	 * Pause this track, if playing.
	 * @see rogue_opcode.AudioResource.Pause
	 */
	override fun Pause() = mMediaPlayer.pause()

	/**
	 * Resume this track, if paused.
	 * @see rogue_opcode.AudioResource.Resume
	 */
	override fun Resume() = mMediaPlayer.start()

	/**
	 * @param pGain new gain to set for this track in range [0.0, 1.0].
	 * @see rogue_opcode.AudioResource.Gain
	 */
	override fun Gain(pGain: Float) {
		super.Gain(pGain)
		mMediaPlayer.setVolume(mGain - 0.5f * mPan, mGain + 0.5f * mPan)
	}

	/**
	 * @return current looping property for this track.
	 * @see rogue_opcode.AudioResource.Loop
	 */
	override fun Loop(): Boolean {
		return mMediaPlayer.isLooping
	}

	/**
	 * @param pLoop whether or not this track should be looped.
	 * *
	 * @see rogue_opcode.AudioResource.Loop
	 */
	override fun Loop(pLoop: Boolean) {
		mMediaPlayer.isLooping = pLoop
	}

	/**
	 * @param pPan new stereo pan to set for this track in range [-1.0, 1.0].
	 * *
	 * @see rogue_opcode.AudioResource.Pan
	 */
	override fun Pan(pPan: Float) {
		super.Pan(pPan)
		mMediaPlayer.setVolume(mGain - 0.5f * mPan, mGain + 0.5f * mPan)
	}
}
