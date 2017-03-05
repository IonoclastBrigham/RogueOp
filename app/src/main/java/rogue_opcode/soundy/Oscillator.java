// Oscillator.java
// Oscillator class
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

import rogue_opcode.containers.Array;
import rogue_opcode.soundy.WaveSource.WaveForm;
import rogue_opcode.soundy.WaveSource.WaveModulation;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


/**
 *
 * @author Brigham Toskin
 */
public class Oscillator
{
	// static playback management
	public static Array<Oscillator> sAll;
	public static Thread sPlaybackThread;
	static
	{
		sAll = new Array<Oscillator>();
	}

	// audio parameters
	public float mFreq;
	public float mLFOFreq;
	public float mVol;
	public float mCurrentPhase;
	public WaveModulation mMod;
	public float mSlideTo;
	public float mSlideSpeed;
	public boolean mEnabled;

	// output streaming stuff
	public float[] mWaveData;
	public float[] mLFOData;
	public short[] mOutBuffer;
	public WaveSource mWave;
	public WaveSource mLFO;
	public AudioTrack mChannel;

	public Array<Effect> mEffects;

	// c'tor, etc. ///

	protected Oscillator(int pBufferSize)
	{
		synchronized(sAll)
		{
			sAll.Append(this);
		}
		if(sPlaybackThread == null)
		{
			sPlaybackThread = new Thread()
			{
				@Override
				public void run()
				{
					//					while(true);
				}
			};
			sPlaybackThread.setName("soundy");
			sPlaybackThread.start();
		}

		mVol = 0.5f;
		mCurrentPhase = 0;
		mMod = WaveModulation.NONE;
		mSlideTo = 0;
		mSlideSpeed = 0;

		mOutBuffer = new short[pBufferSize];
		mChannel = new AudioTrack(AudioManager.STREAM_MUSIC,
			WaveSource.sSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT, pBufferSize * 2, // 2b per smpl
			AudioTrack.MODE_STATIC);
		mEffects = new Array<Effect>();
	}

	public Oscillator(int pBufferSize, WaveForm pWaveForm)
	{
		this(pBufferSize);
		mWaveData = new float[pBufferSize];
		mWave = WaveSource.ICanHas(pWaveForm);
	}

	public Oscillator(int pBufferSize, WaveForm pWaveForm, WaveForm pLFOForm)
	{
		this(pBufferSize, pWaveForm);
		mLFOData = new float[pBufferSize];
		mLFO = WaveSource.ICanHas(pLFOForm);
	}

	// playback and such ///////////////////////////////////////////////////////

	public void Generate()
	{
		if(mLFO == null)
			mWave.SynthMaster(mWaveData, mFreq, mCurrentPhase, mVol, mMod,
					null, 0, mSlideTo, mSlideSpeed, mEnabled);
		else
		{
			mLFO.SynthBasic(mLFOData, mLFOFreq, mCurrentPhase, 1.0f, mEnabled);
			mWave.SynthMaster(mWaveData, mFreq, mCurrentPhase, mVol, mMod,
					mLFOData, 0, mSlideTo, mSlideSpeed, mEnabled);
		}

		//		for(Effect tFX : mEffects.data)
		//			tFX.Process(mWaveData);
	}

	// audio processing ////////////////////////////////////////////////////////

	/**
	 * Adds the specified effect to the output processing chain.
	 *
	 * @param pFX the {@link Effect} to add.
	 * @return this reference, so multiple calls may be chained.
	 */
	public Oscillator Chain(Effect pFX)
	{
		mEffects.Append(pFX);
		return this;
	}

	// inner classes ///////////////////////////////////////////////////////////

	/**
	 * This interface represents an effects processor which gets chained to the
	 * output. It will alter the output stream in some way, resulting in a
	 * different sound.
	 */
	public interface Effect
	{
		/**
		 * This is the main processing interface. It should handle the entire
		 * {@code oSignal} buffer, substituting processed output in-place. The
		 * buffer passed in may be only part of an ongoing audio stream, so your
		 * implementation should maintain whatever state is necessary to
		 * maintain continuous output between calls.
		 *
		 * @param oSignal audio buffer to operate on.
		 */
		public void Process(float[] oSignal);

		// TODO: some way of querying and accessing parameters, to ultimately
		// facilitate something akin to buzz machine param edit GUI?
	}
}
