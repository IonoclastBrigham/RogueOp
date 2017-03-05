// SoundResource.java
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


package rogue_opcode.soundy;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import rogue_opcode.AudioResource;
import rogue_opcode.GameProc;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;


/**
 * SoundResource is a class for producing sounds which closely parallels GR's
 * API.
 * <br /><br />
 * At a high level, usage simply consists of creating an instance, and calling
 * {@link SoundEffect#Play() Play()}. You have several options for controlling
 * playback, including looping, gain, and pan.
 */
public class SoundEffect extends AudioResource implements Serializable
{
	private static final long serialVersionUID = -8555715326127030497L;

	/** Default value for unimplemented sound load priority parameter. */
	static final int PRIORITY = 1;
	/** Default value for unimplemented resample quality parameter. */
	static final int QUALITY = 0;

	public static SoundPool sPool = new SoundPool(4, AudioManager.STREAM_MUSIC,
		QUALITY);

	protected int mSndID;
	protected int mStrmID = 0;
	protected int mLoop;

	// c'tor ///////////////////////////////////////////////////////////////////

	/**
	 * Constructs a SoundResource object to play the specified sound.
	 *
	 * @param pResID resource ID for the desired sound file.
	 */
	public SoundEffect(int pResID)
	{
		super(pResID);
		Log.d(GameProc.TAG, "  SoundEffect(int)");
		load(pResID, 0);
	}

	/**
	 * Constructs a SoundResource object to play the specified sound with the
	 * specified looping property.
	 *
	 * @param pResID resource ID for the desired sound file.
	 * @param pLoop whether or not to loop the sound.
	 */
	public SoundEffect(int pResID, boolean pLoop)
	{
		super(pResID);
		Log.d(GameProc.TAG, "  SoundEffect(int, boolean)");
		load(pResID, pLoop ? -1 : 0);
	}

	/**
	 * Loads the sound into memory and decodes it.
	 *
	 * @param pResID resource ID for sound file to load.
	 * @param pLoop -1 for loop, 0 for no loop.
	 */
	protected void load(int pResID, int pLoop)
	{

		mSndID = sPool.load(GameProc.sOnly, pResID, 1);
		mLoop = pLoop;
		Log.d(GameProc.TAG, "  SoundEffect.load(" + pResID + ")" + " => "
				+ mSndID);
	}

	public static void Die()
	{
		Log.d(GameProc.TAG, "  SoundEffect.Die()");
		if(sPool != null)
		{
			sPool.release();
			sPool = null;
		}
	}

	@Override
	protected void die()
	{
		Log.d(GameProc.TAG, "  SoundEffect.die()");
		sPool.stop(mStrmID);
		sPool.unload(mSndID);
	}

	// audio control ///////////////////////////////////////////////////////////

	@Override
	public void Play()
	{
		float lVol = mGain - 0.5f * mPan;
		float rVol = mGain + 0.5f * mPan;
		System.err
				.println("  SoundEffect.Play(): " + mSndID + " => " + mStrmID);
		mStrmID = sPool.play(mSndID, lVol, rVol, PRIORITY, mLoop, 1);
	}
	@Override
	public void Stop()
	{
		Log.d(GameProc.TAG, "  SoundEffect.Stop()");
		mStrmID = 0;
		sPool.stop(mStrmID);
	}

	@Override
	public void Pause()
	{
		Log.d(GameProc.TAG, "  SoundEffect.Pause()");
		if(mStrmID > 0)
			sPool.pause(mStrmID);
	}
	@Override
	public void Resume()
	{
		Log.d(GameProc.TAG, "  SoundEffect.Resume()");
		if(mStrmID > 0)
			sPool.resume(mStrmID);
	}

	@Override
	public void Gain(float pGain)
	{
		Log.d(GameProc.TAG, "  SoundEffect.Gain(float)");
		super.Gain(pGain);
		if(mStrmID > 0)
			sPool.setVolume(mStrmID, pGain - 0.5f * mPan, pGain + 0.5f * mPan);
	}

	@Override
	public boolean Loop()
	{
		Log.d(GameProc.TAG, "  SoundEffect.Loop()");
		return mLoop == -1;
	}
	@Override
	public void Loop(boolean pLoop)
	{
		Log.d(GameProc.TAG, "  SoundEffect.Loop(boolean)");
		mLoop = (pLoop ? -1 : 0); // -1 for loop, 0 for no loop
		if(mStrmID > 0)
			sPool.setLoop(mStrmID, mLoop);
	}
	public void Loop(int pReplayCount)
	{
		Log.d(GameProc.TAG, "  SoundEffect.Loop(int)");
		mLoop = pReplayCount;
		if(mStrmID > 0)
			sPool.setLoop(mStrmID, pReplayCount);
	}

	@Override
	public void Pan(float pPan)
	{
		Log.d(GameProc.TAG, "  SoundEffect.Pan(float)");
		super.Pan(pPan);
		if(mStrmID > 0)
			sPool.setVolume(mStrmID, mGain - 0.5f * pPan, mGain + 0.5f * pPan);
	}

	// static access ///////////////////////////////////////////////////////////

	/**
	 * Finds the {@code AudioResource} associated with the specified {@code
	 * resource} ID, if it exists. Otherwise, constructs a new {@code
	 * SoundEffect} instance and loads the appropriate resource.
	 *
	 * @param pResID the {@code resource} ID to retrieve.
	 * @return an {@code AudioResource} handle to a {@code SoundEffect} instance
	 *         with the requested resource.
	 */
	public static AudioResource FindByResID(int pResID)
	{
		AudioResource tAR = AudioResource.FindByResID(pResID);
		if(tAR == null)
			tAR = new SoundEffect(pResID);
		return tAR;
	}

	// serialization protocol //////////////////////////////////////////////////

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeInt(mLoop); // everything else will be saved in AR, or reloaded
	}

	private void readObject(ObjectInputStream pIn) throws IOException,
	ClassNotFoundException
	{
		pIn.defaultReadObject(); // read everything we saved
		load(mResID, mLoop); // loads the sound resource
	}
}
