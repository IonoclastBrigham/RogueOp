// AudioResource.java
// Common audio interface
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


package rogue_opcode;

import java.util.HashMap;

import rogue_opcode.soundy.FXR;
import rogue_opcode.soundy.MusicTrack;
import rogue_opcode.soundy.SoundEffect;
import rogue_opcode.soundy.Soundy;


/**
 * Defines a common interface for simple audio objects.
 *
 * @author Brigham Toskin
 */
public abstract class AudioResource
{
	/**
	 * Maps {@code resource} IDs to {@code AudioResource}s. <b>Note: This may
	 * be inefficient with memory for some operations; use carefully!</b>
	 */
	public static HashMap<Integer, AudioResource> sAllARs;
	static
	{
		sAllARs = new HashMap<Integer, AudioResource>();
	}

	protected int mResID;
	protected float mGain;
	protected float mPan;

	// factory functionality ///////////////////////////////////////////////////

	/**
	 * Enumeration of available audio types.
	 */
	public enum AudioType
	{
		/** Indicates a short PCM audio clip suitable for sound effects. */
		EFFECT,
		/** Indicates a longer PCM audio clip suitable for in game music. */
		MUSIC,
		/** Indicates a synthesized audio clip from SFXR. */
		SYNTH_EFFECT,
		/** Indicates a synthesized 4-track song. */
		SYNTH_MUSIC
	}

	/**
	 * Constructs an <code>AudioResource</code> instance, defaulting to the more
	 * expensive but safer implementation to allow for longer audio files.
	 * 
	 * @param pResID audio resource to load.
	 * @return a newly constructed specific instance.
	 * @see AudioResource#ICanHas(int, AudioType)
	 */
	// XXX: does this still make sense?
	public static AudioResource ICanHas(int pResID)
	{
		return ICanHas(pResID, AudioType.MUSIC);
	}

	/**
	 * Constructs an <code>AudioResource</code> instance based on the hint
	 * parameter <code>pType</code>. A limitation of the Android audio
	 * APIs is that the underlying <code>SoundPool</code> objects are ill suited
	 * to playing audio samples that are longer than just a few seconds. As an
	 * alternative, this method can instantiate an implementation that utilizes
	 * <code>MediaPlayer</code> on the back end; this allows for longer audio
	 * clips at the expense of more computational resources.
	 *
	 * @param pResID audio resource to load.
	 * @param pType hint as to the type and characteristics of audio file we
	 *        wish to load; this parameter affects which underlying
	 *        implementation is constructed.
	 * @return a newly constructed specific instance.
	 * @see AudioResource#ICanHas(int)
	 * @see AudioResource.AudioType
	 */
	public static AudioResource ICanHas(int pResID, AudioType pType)
	{
		AudioResource tAudio;
		switch(pType)
		{
		case EFFECT:
			tAudio = new SoundEffect(pResID);
			break;
		case MUSIC:
			tAudio = new MusicTrack(pResID);
			break;
		case SYNTH_EFFECT:
			tAudio = new FXR(pResID);
			break;
		case SYNTH_MUSIC:
			tAudio = new Soundy(pResID);
			break;
		default:
			tAudio = null;
		}
		return tAudio;
	}

	protected AudioResource(int pResID)
	{
		sAllARs.put(pResID, this);
		mResID = pResID;
		mGain = 0.5f;
		mPan = 0.0f;
	}

	abstract protected void die();

	public static void Die()
	{
		// we are shutting down, so we don't care about performance too much
		for(AudioResource tAR : sAllARs.values())
		{
			tAR.die();
		}
	}

	// audio interfaces ////////////////////////////////////////////////////////

	public abstract void Play();
	public abstract void Stop();
	public abstract void Pause();
	public abstract void Resume();

	public float Gain()
	{
		return mGain;
	}
	public void Gain(float pGain)
	{
		if(pGain > 1.0f)
			pGain = 1.0f;
		if(pGain < 0)
			pGain = 0;
		mGain = pGain;
	}
	public void Mute()
	{
		Gain(0);
	}

	public abstract boolean Loop();
	public abstract void Loop(boolean pLoop);

	public float Pan()
	{
		return mPan;
	}
	public void Pan(float pPan)
	{
		if(pPan > 1.0f)
			pPan = 1.0f;
		if(pPan < -1.0f)
			pPan = -1.0f;
		mPan = pPan;

	}

	// static access ///////////////////////////////////////////////////////////

	/**
	 * Finds the {@code AudioResource} associated with the specified {@code
	 * resource} ID, if it exists. You may optionally override this method in
	 * class implementations to create an appropriate instance if the resource
	 * hasn't been previously loaded.
	 *
	 * @param pResID the {@code resource} ID to retrieve.
	 * @return the found {@code AudioResource} instance, or {@code null} if it
	 *         hasn't been loaded.
	 */
	public static AudioResource FindByResID(int pResID)
	{
		return sAllARs.get(pResID);
	}
}
