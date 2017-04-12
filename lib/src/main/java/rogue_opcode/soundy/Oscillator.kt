// Oscillator.kt
// Oscillator class
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

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.effect.Effect
import rogue_opcode.containers.Array
import rogue_opcode.soundy.WaveSource.WaveForm
import rogue_opcode.soundy.WaveSource.WaveModulation


/**

 * @author Brigham Toskin
 */
class Oscillator private constructor(pBufferSize: Int,
                                     mWaveForm: WaveForm? = null,
                                     mLFOForm: WaveForm? = null) {
	companion object {
		var sAll: Array<Oscillator> = Array()
//		var sPlaybackThread: Thread? = null

	}


	// audio parameters
	var mFreq = 0f
	var mLFOFreq = 0f
	var mVol = 0f
	var mCurrentPhase = 0f
	var mMod: WaveModulation
	var mSlideTo = 0f
	var mSlideSpeed = 0f
	var mEnabled = false

	// output streaming stuff
	var mWaveData = FloatArray(pBufferSize)
	var mLFOData: FloatArray? = null
	var mOutBuffer: ShortArray
	var mWave: WaveSource? = null
	var mLFO: WaveSource? = null
	var mChannel: AudioTrack

	var mEffects: Array<Effect>

	init {
		synchronized(sAll) {
			sAll.Append(this)
		}
		// TODO?
//		if(sPlaybackThread ==
//				null) {
//			sPlaybackThread = object : Thread() {
//				override fun run() {
//					//					while(true);
//				}
//			}
//			sPlaybackThread!!.name = "soundy"
//			sPlaybackThread!!.start()
//		}

		mVol = 0.5f
		mCurrentPhase = 0f
		mMod = WaveModulation.NONE
		mSlideTo = 0f
		mSlideSpeed = 0f

		mWave = mWaveForm?.let { WaveSource.ICanHas(it) }
		mLFOForm?.let {
			mLFOData = FloatArray(pBufferSize)
			mLFO = WaveSource.ICanHas(it)
		}

		mOutBuffer = ShortArray(pBufferSize)
		mChannel = AudioTrack(AudioManager.STREAM_MUSIC,
				WaveSource.sSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, pBufferSize * 2, // 2b per smpl
				AudioTrack.MODE_STATIC)
		mEffects = Array<Effect>()
	}

	// playback and such ///////////////////////////////////////////////////////

	fun Generate() {
		val tWave = mWave ?: return

		mLFO?.SynthBasic(mLFOData!!, mLFOFreq, mCurrentPhase, 1.0f, mEnabled)
		tWave.SynthMaster(mWaveData, mFreq, mCurrentPhase, mVol, mMod,
						  mLFOData, 0f, mSlideTo, mSlideSpeed, mEnabled)

		// TODO:
		//		for(Effect tFX : mEffects.mData)
		//			tFX.Process(mWaveData);
	}

	// audio processing ////////////////////////////////////////////////////////

	/**
	 * Adds the specified effect to the output processing chain.
	 * @param pFX the [Effect] to add.
	 * @return this reference, so multiple calls may be chained.
	 */
	fun Chain(pFX: Effect): Oscillator {
		mEffects.Append(pFX)
		return this
	}

	// inner classes ///////////////////////////////////////////////////////////

	/**
	 * This interface represents an effects processor which gets chained to the
	 * output. It will alter the output stream in some way, resulting in a
	 * different sound.
	 */
	interface Effect {
		/**
		 * This is the main processing interface. It should handle the entire
		 * `oSignal` buffer, substituting processed output in-place. The
		 * buffer passed in may be only part of an ongoing audio stream, so your
		 * implementation should maintain whatever state is necessary to
		 * maintain continuous output between calls.
		 * @param oSignal audio buffer to operate on.
		 */
		fun Process(oSignal: FloatArray)

		// TODO: some way of querying and accessing parameters, to ultimately
		// facilitate something akin to buzz machine param edit GUI?
	}
}
