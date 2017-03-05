// ActionElement.java
// Updatable interface for active entities.
//
// Copyright © 2010-2017 Christopher R. Tooley, Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode;

import android.util.Log;

import java.io.Serializable;

import rogue_opcode.containers.Array;


/**
 * {@code ActionElement} is an abstract class that is intended to be extended
 * by the implementing program. Its sole function is to give objects a shot at
 * the processor at regular intervals.
 * <br /><br />
 * All extant ActionElements may be accessed through the static member {@code
 * sAllAEs}.
 * <br /><br />
 * Example usage: extend this class to create a coconut factory which has no
 * graphical component, but still needs to determine on every game tick if it
 * should spawn a new coconut.
 */
public abstract class ActionElement implements Serializable
{
	private static final long serialVersionUID = 3959649135411049295L;

	public static Array<ActionElement> sAllAEs;
	static
	{
		try
		{
			if(sAllAEs == null)
				sAllAEs = new Array<ActionElement>(128);
		}
		catch(Exception e)
		{
			Log.d(GameProc.TAG, e.toString());
		}
	}

	// only active items will have their update routine called
	protected boolean mActive = true;

	public ActionElement()
	{
		sAllAEs.Append(this);
	}

	/** override in your derived class to do something exciting. */
	public void Update()
	{
	}

	/** override in your derived class to do something exciting. */
	public void Reset()
	{
	}

	public void Active(boolean pActive)
	{
		mActive = pActive;
	}

	public boolean Active()
	{
		return mActive;
	}

}
