// Rectanglef.java
// Floating point rectangle class
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


package rogue_opcode.geometrics;

import java.io.Serializable;

import android.graphics.RectF;


/**
 *
 * @author Brigham Toskin
 */
public class Rectanglef implements Serializable
{
	private static final long serialVersionUID = 1988188856826226217L;

	protected RectF mRect;
	protected float w, h;

	public Rectanglef()
	{
		mRect = new RectF();
		w = h = 0;
	}

	public Rectanglef(float pX, float pY, float pW, float pH)
	{
		this();
		mRect.left = pX;
		mRect.top = pY;
		w = pW;
		h = pH;
		mRect.right = mRect.left + w;
		mRect.bottom = mRect.top + h;
	}

	public float X()
	{
		return mRect.left;
	}

	public void X(float pX)
	{
		mRect.left = pX;
	}

	public float Y()
	{
		return mRect.top;
	}

	public void Y(float pY)
	{
		mRect.top = pY;
	}

	public float W()
	{
		return w;
	}

	public void W(float pW)
	{
		w = pW;
		mRect.right = mRect.left + w;
	}

	public float H()
	{
		return h;
	}

	public void H(float pH)
	{
		h = pH;
		mRect.bottom = mRect.top + h;
	}

	public RectF toRectF()
	{
		return mRect;
	}
}
