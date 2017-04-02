// SoundResource.kt
// Represents a loaded audio resource
//
// Copyright © 2010-2017 Christopher R. Tooley, Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.soundy


import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import rogue_opcode.AudioResource
import rogue_opcode.GameProc


/**
 * SoundResource is a class for producing sounds which closely parallels GR's
 * API.
 *
 * At a high level, usage simply consists of creating an instance, and calling
 * [Play()][SoundEffect.Play]. You have several options for controlling
 * playback, including looping, gain, and pan.
 */
class SoundEffect : AudioResource {

	companion object {
		/** Default value for unimplemented sound load priority parameter.  */
		internal val PRIORITY = 1
		/** Default value for unimplemented resample quality parameter.  */
		internal val QUALITY = 0

		var sPool: SoundPool? = SoundPool(4, AudioManager.STREAM_MUSIC, QUALITY)

		fun Die() {
			val tPool = sPool ?: return

			Log.d(GameProc.TAG, "  SoundEffect.Die()")
			tPool.release()
			sPool = null
		}

		// static access ///////////////////////////////////////////////////////////

		/**
		 * Finds the `AudioResource` associated with the specified `resource` ID, if it exists. Otherwise, constructs a new `SoundEffect` instance and loads the appropriate resource.
		 * @param pResID the `resource` ID to retrieve.
		 * @return an `AudioResource` handle to a `SoundEffect` instance
		 * 		 with the requested resource.
		 */
		fun FindByResID(pResID: Int)
			= AudioResource.FindByResID(pResID) ?: SoundEffect(pResID)
	}

	protected var mSndID: Int = 0
	protected var mStrmID = 0
	protected var mLoop: Int = 0

	// c'tor ///////////////////////////////////////////////////////////////////

	/**
	 * Constructs a SoundResource object to play the specified sound.

	 * @param pResID resource ID for the desired sound file.
	 */
	constructor(pResID: Int) : super(pResID) {
		Log.d(GameProc.TAG, "  SoundEffect(int)")
		load(pResID, 0)
	}

	/**
	 * Constructs a SoundResource object to play the specified sound with the
	 * specified looping property.

	 * @param pResID resource ID for the desired sound file.
	 * *
	 * @param pLoop whether or not to loop the sound.
	 */
	constructor(pResID: Int, pLoop: Boolean) : super(pResID) {
		Log.d(GameProc.TAG, "  SoundEffect(int, boolean)")
		load(pResID, if(pLoop) -1 else 0)
	}

	/**
	 * Loads the sound into memory and decodes it.

	 * @param pResID resource ID for sound file to load.
	 * *
	 * @param pLoop -1 for loop, 0 for no loop.
	 */
	protected fun load(pResID: Int, pLoop: Int) {

		mSndID = sPool!!.load(GameProc.sOnly, pResID, 1)
		mLoop = pLoop
		Log.d(GameProc.TAG, "  SoundEffect.load(" + pResID + ")" + " => "
				+ mSndID)
	}

	override fun die() {
		Log.d(GameProc.TAG, "  SoundEffect.die()")
		sPool!!.stop(mStrmID)
		sPool!!.unload(mSndID)
	}

	// audio control ///////////////////////////////////////////////////////////

	override fun Play() {
		val lVol = mGain - 0.5f * mPan
		val rVol = mGain + 0.5f * mPan
		System.err
				.println("  SoundEffect.Play(): $mSndID => $mStrmID")
		mStrmID = sPool!!.play(mSndID, lVol, rVol, PRIORITY, mLoop, 1f)
	}

	override fun Stop() {
		Log.d(GameProc.TAG, "  SoundEffect.Stop()")
		mStrmID = 0
		sPool!!.stop(mStrmID)
	}

	override fun Pause() {
		Log.d(GameProc.TAG, "  SoundEffect.Pause()")
		if(mStrmID > 0)
			sPool!!.pause(mStrmID)
	}

	override fun Resume() {
		Log.d(GameProc.TAG, "  SoundEffect.Resume()")
		if(mStrmID > 0)
			sPool!!.resume(mStrmID)
	}

	override fun Gain(pGain: Float) {
		Log.d(GameProc.TAG, "  SoundEffect.Gain(float)")
		super.Gain(pGain)
		if(mStrmID > 0)
			sPool!!.setVolume(mStrmID, pGain - 0.5f * mPan, pGain + 0.5f * mPan)
	}

	override fun Loop(): Boolean {
		Log.d(GameProc.TAG, "  SoundEffect.Loop()")
		return mLoop == -1
	}

	override fun Loop(pLoop: Boolean) {
		Log.d(GameProc.TAG, "  SoundEffect.Loop(boolean)")
		mLoop = if(pLoop) -1 else 0 // -1 for loop, 0 for no loop
		if(mStrmID > 0)
			sPool!!.setLoop(mStrmID, mLoop)
	}

	fun Loop(pReplayCount: Int) {
		Log.d(GameProc.TAG, "  SoundEffect.Loop(int)")
		mLoop = pReplayCount
		if(mStrmID > 0)
			sPool!!.setLoop(mStrmID, pReplayCount)
	}

	override fun Pan(pPan: Float) {
		Log.d(GameProc.TAG, "  SoundEffect.Pan(float)")
		super.Pan(pPan)
		if(mStrmID > 0) {
			sPool!!.setVolume(mStrmID, mGain - 0.5f * pPan, mGain + 0.5f * pPan)
		}
	}
}
