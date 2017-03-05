// FXR.java
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


package rogue_opcode.soundy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import rogue_opcode.AudioResource;
import rogue_opcode.GameProc;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;


/**
 * This class is for loading and playing back synthesis parameter files created
 * by the open source sound effect generator, SFXR. It supports file formats up
 * through version 102, in addition to some more advanced/standard envelope
 * functionality.
 * <br /><br />
 * FXR can load and pre-render the synthesized audio once for fast, efficient
 * playback at runtime. Or, it can be configured to apply a small amount of
 * randomness to the synthesis parameters and regenerate the sound every time it
 * is played; this will lead to a more diverse soundscape, delivering a more
 * interesting and realistic experience to players at the expense of increased
 * processing overhead.
 *
 * @author Brigham Toskin
 */
public class FXR extends AudioResource
{
	protected AudioTrack mOutStream;

	protected WaveSource mSynth;
	protected boolean mRandomize;
	protected byte[] mData;

	// c'tors //

	/**
	 * Constructs an instance for the specified resource.
	 *
	 * @param pResID raw resource ID of the SFXR audio file.
	 */
	public FXR(int pResID)
	{
		this(pResID, false);
	}

	/**
	 * Constructs an instance for the specified resource.
	 *
	 * @param pResID raw resource ID of the SFXR audio file.
	 * @param pRandomize whether to randomize the sound params on playback.
	 */
	public FXR(int pResID, boolean pRandomize)
	{
		super(pResID);
		mRandomize = pRandomize;
		Resources tRes = GameProc.sOnly.getResources();
		InputStream tStream = new BufferedInputStream(tRes.openRawResource(pResID));
		int tLength = 0;
		try
		{
			tLength = tStream.available();
			mData = new byte[tLength];
			tStream.read(mData);
		}
		catch(Exception e)
		{
			Log.e("ionoclast", "Error loading specified resource.", e);
			try
			{
				tStream.close();
			}
			catch(IOException e1)
			{
				// nothing
			}
			return;
		}
		// TODO: deduce the kind of WaveSource from file
		mOutStream = new AudioTrack(AudioManager.STREAM_MUSIC,
			WaveSource.sSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
			WaveSource.sFormat, tLength,
			(pRandomize ? AudioTrack.MODE_STREAM : AudioTrack.MODE_STATIC));
	}

	// playback interfaces /////////////////////////////////////////////////////

	@Override
	public boolean Loop()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void Loop(boolean pLoop)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void Pause()
	{

	}

	@Override
	public void Play()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void Resume()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void Stop()
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void die()
	{
		// TODO Auto-generated method stub
	}
}
