// Rectangle.java
// Rectangle class
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

import android.graphics.Rect;


/**
 *
 * @author Brigham Toskin
 */
public class Rectangle implements Serializable
{
	private static final long serialVersionUID = -7071451744257630068L;

	protected Rect mRect;
	protected int w, h;

	public Rectangle()
	{
		mRect = new Rect();
		w = h = 0;
	}

	public Rectangle(int pX, int pY, int pW, int pH)
	{
		this();
		mRect.left = pX;
		mRect.top = pY;
		w = pW;
		h = pH;
		mRect.right = mRect.left + w;
		mRect.bottom = mRect.top + h;
	}

	public int X()
	{
		return mRect.left;
	}

	public void X(int pX)
	{
		mRect.left = pX;
	}

	public int Y()
	{
		return mRect.top;
	}

	public void Y(int pY)
	{
		mRect.top = pY;
	}

	public int W()
	{
		return w;
	}

	public void W(int pW)
	{
		w = pW;
		mRect.right = mRect.left + w;
	}

	public int H()
	{
		return h;
	}

	public void H(int pH)
	{
		h = pH;
		mRect.bottom = mRect.top + h;
	}

	public Rect toRect()
	{
		return mRect;
	}
}
