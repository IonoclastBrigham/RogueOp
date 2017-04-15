// AudioResource.kt
// Common audio interface
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


package rogue_opcode

import android.support.annotation.AnyRes
import android.util.SparseArray
import rogue_opcode.soundy.FXR
import rogue_opcode.soundy.MusicTrack
import rogue_opcode.soundy.SoundEffect
import rogue_opcode.soundy.Soundy


private operator fun <T> SparseArray<T>.iterator() = object : Iterator<T> {
	private var mIndex = 0

	override fun next() = valueAt(mIndex++)
	override fun hasNext() = mIndex < size()
}

/**
 * *Defines a common interface for simple audio objects.*
 *
 * @author Brigham Toskin
 */
abstract class AudioResource constructor(@AnyRes pResID: Int) {
	companion object {
		/**
		 * Maps `resource` IDs to `AudioResource`s. **Note: This may
		 * be inefficient with memory for some operations; use carefully!**
		 */
		var sAllARs = SparseArray<AudioResource>()

		/**
		 * Constructs an `AudioResource` instance based on the hint
		 * parameter `pType`. A limitation of the Android audio
		 * APIs is that the underlying `SoundPool` objects are ill suited
		 * to playing audio samples that are longer than just a few seconds. As an
		 * alternative, this method can instantiate an implementation that utilizes
		 * `MediaPlayer` on the back end; this allows for longer audio
		 * clips at the expense of more computational resources.

		 * @param pResID audio resource to load.
		 * *
		 * @param pType hint as to the type and characteristics of audio file we
		 * *		wish to load; this parameter affects which underlying
		 * *		implementation is constructed.
		 * *
		 * @return a newly constructed specific instance.
		 * *
		 * @see AudioResource.ICanHas
		 * @see AudioResource.AudioType
		 */
		@JvmOverloads fun ICanHas(pResID: Int, pType: AudioType = AudioType.MUSIC)
			= when(pType) {
				AudioType.EFFECT -> SoundEffect(pResID)
				AudioType.MUSIC -> MusicTrack(pResID)
				AudioType.SYNTH_EFFECT -> FXR(pResID)
				AudioType.SYNTH_MUSIC -> Soundy(pResID)
			}

		fun Die() {
			for(tAR in sAllARs) tAR.die()
		}

		// static access ///////////////////////////////////////////////////////////

		/**
		 * Finds the `AudioResource` associated with the specified `resource` ID, if it exists. You may optionally override this method in
		 * class implementations to create an appropriate instance if the resource
		 * hasn't been previously loaded.
		 * @param pResID the `resource` ID to retrieve.
		 * @return the found `AudioResource` instance, or `null` if it
		 * *		 hasn't been loaded.
		 */
		fun FindByResID(pResID: Int): AudioResource? {
			return sAllARs[pResID]
		}
	}

	/**
	 * Enumeration of available audio types.
	 */
	enum class AudioType {
		/** Indicates a short PCM audio clip suitable for sound effects.  */
		EFFECT,
		/** Indicates a longer PCM audio clip suitable for in game music.  */
		MUSIC,
		/** Indicates a synthesized audio clip from SFXR.  */
		SYNTH_EFFECT,
		/** Indicates a synthesized 4-track song.  */
		SYNTH_MUSIC
	}


	protected var mResID = pResID

	protected var mGain = 0.5f
	protected var mPan = 0f

	init { sAllARs.put(pResID, this) }

	protected abstract fun die()

	// audio interfaces ////////////////////////////////////////////////////////

	abstract fun Play()
	abstract fun Stop()
	abstract fun Pause()
	abstract fun Resume()

	fun Gain() = mGain

	open fun Gain(pGain: Float) {
		mGain = pGain.coerceIn(0f..1.0f)
	}

	fun Mute() = Gain(0f)

	abstract fun Loop(): Boolean
	abstract fun Loop(pLoop: Boolean)

	fun Pan() = mPan

	open fun Pan(pPan: Float) {
		mPan = pPan.coerceIn(-1.0f..1.0f)

	}
}
