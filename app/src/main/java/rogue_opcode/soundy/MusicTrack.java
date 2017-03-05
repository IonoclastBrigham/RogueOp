// MusicTrack.java
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


package rogue_opcode.soundy;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import rogue_opcode.AudioResource;
import rogue_opcode.GameProc;

import android.media.MediaPlayer;


/**
 * This class represents and gives you access to longer audio tracks. It is
 * intended primarily for long-playing songs, rather than quick, low-latency
 * sound effects.
 * <br /><br />
 * <b>Usage:</b> Construct a {@code MusicTrack} instance, and start it with the
 * {@link MusicTrack#Play() Play()} method. You also have access to various
 * aspects of playback, such as volume, pan, and looping. You should expect some
 * latency both in loading and starting playback; You {@code MusicTrack}s should
 * be loaded early on during initialization, so they will be ready when they are
 * needed.
 *
 * @see AudioResource
 * @see SoundEffect
 * @author Brigham Toskin
 */
public class MusicTrack extends AudioResource implements Serializable
{
	private static final long serialVersionUID = 7946172188710354003L;

	protected MediaPlayer mMediaPlayer;
	protected boolean mLoop;

	// c'tors //////////////////////////////////////////////////////////////////

	public MusicTrack(int pResID)
	{
		super(pResID);
		load(pResID, false);
	}

	public MusicTrack(int pResID, boolean pLoop)
	{
		super(pResID);
		load(pResID, pLoop);
	}

	protected void load(int pResID, boolean pLoop)
	{
		mMediaPlayer = MediaPlayer.create(GameProc.sOnly, pResID);
		mMediaPlayer.setLooping(pLoop);
		mLoop = pLoop;
	}

	/** Stops all playing tracks and frees their resources. */
	@Override
	protected void die()
	{
		mMediaPlayer.stop();
		mMediaPlayer.release();
	}

	// media control ///////////////////////////////////////////////////////////

	/**
	 * Play this track.
	 *
	 * @see rogue_opcode.AudioResource#Play()
	 */
	@Override
	public void Play()
	{
		mMediaPlayer.start();
	}

	/**
	 * Stop this track, if playing.
	 *
	 * @see rogue_opcode.AudioResource#Stop()
	 */
	@Override
	public void Stop()
	{
		mMediaPlayer.stop();
	}

	/**
	 * Pause this track, if playing.
	 *
	 * @see rogue_opcode.AudioResource#Pause()
	 */
	@Override
	public void Pause()
	{
		mMediaPlayer.pause();
	}

	/**
	 * Resume this track, if paused.
	 *
	 * @see rogue_opcode.AudioResource#Resume()
	 */
	@Override
	public void Resume()
	{
		mMediaPlayer.start();
	}

	/**
	 * @param pGain new gain to set for this track in range [0.0, 1.0].
	 * @see rogue_opcode.AudioResource#Gain(float)
	 */
	@Override
	public void Gain(float pGain)
	{
		super.Gain(pGain);
		mMediaPlayer.setVolume(pGain - 0.5f * mPan, pGain + 0.5f * mPan);
	}

	/**
	 * @return current looping property for this track.
	 * @see rogue_opcode.AudioResource#Loop()
	 */
	@Override
	public boolean Loop()
	{
		return mMediaPlayer.isLooping();
	}

	/**
	 * @param pLoop whether or not this track should be looped.
	 * @see rogue_opcode.AudioResource#Loop(boolean)
	 */
	@Override
	public void Loop(boolean pLoop)
	{
		mMediaPlayer.setLooping(pLoop);
	}

	/**
	 * @param pPan new stereo pan to set for this track in range [-1.0, 1.0].
	 * @see rogue_opcode.AudioResource#Pan(float)
	 */
	@Override
	public void Pan(float pPan)
	{
		super.Pan(pPan);
		mMediaPlayer.setVolume(mGain - 0.5f * pPan, mGain + 0.5f * pPan);
	}

	// static access ///////////////////////////////////////////////////////////

	/**
	 * Finds the {@code AudioResource} associated with the specified {@code
	 * resource} ID, if it exists. Otherwise, constructs a new {@code
	 * MusicTrack} instance and loads the appropriate resource.
	 * 
	 * @param pResID the {@code resource} ID to retrieve.
	 * @return an {@code AudioResource} handle to a {@code MusicTrack} instance
	 *         with the requested resource.
	 */
	public static AudioResource FindByResID(int pResID)
	{
		AudioResource tAR = AudioResource.FindByResID(pResID);
		if(tAR == null)
			tAR = new MusicTrack(pResID);
		return tAR;
	}

	// serialization protocol //////////////////////////////////////////////////

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeBoolean(mLoop); // others will be saved in AR, or reloaded
	}

	private void readObject(ObjectInputStream pIn) throws IOException,
	ClassNotFoundException
	{
		pIn.defaultReadObject(); // read everything we saved
		load(mResID, mLoop); // loads the sound resource
	}
}
