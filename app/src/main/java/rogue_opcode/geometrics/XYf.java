// XYf.java
// floating point 2D point/vector class
//
// Copyright © 2010-2017 Brigham Toskin, Leea Harlan
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode.geometrics;

import java.io.Serializable;

import android.graphics.Point;


public class XYf implements Serializable
{
	private static final long serialVersionUID = -3490122379205325810L;

	// cartesian (x, y) or vector components <i, j>
	public float x;
	public float y;

	// c'tors //

	public XYf()
	{
		this(0.0f, 0.0f);
	}

	public XYf(float pX, float pY)
	{
		this.x = pX;
		this.y = pY;
	}

	public XYf(XYf pSourceXY)
	{
		this(pSourceXY.x, pSourceXY.y);
	}

	// blah
	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}

	public Point toPoint()
	{
		return new Point(Math.round(x), Math.round(y));
	}

	public void set(XYf pSourceXY)
	{
		x = pSourceXY.x;
		y = pSourceXY.y;
	}

	// arithmetic operators ////////////////////////////////////////////////////

	public XYf plus(XYf pOther)
	{
		return new XYf(pOther.x + x, pOther.y + y);
	}

	public XYf minus(XYf pOther)
	{
		return new XYf(x - pOther.x, y - pOther.y);
	}

	public XYf add(XYf pOther)
	{
		x += pOther.x;
		y += pOther.y;
		return this;
	}

	public XYf sub(XYf pOther)
	{
		x -= pOther.x;
		y -= pOther.y;
		return this;
	}

	public XYf times(float pScalar)
	{
		return new XYf(x * pScalar, y * pScalar);
	}

	public XYf dividedBy(float pScalar)
	{
		return new XYf(x / pScalar, y / pScalar);
	}

	public XYf mul(float pScalar)
	{
		x *= pScalar;
		y *= pScalar;
		return this;
	}

	public XYf div(float pScalar)
	{
		x /= pScalar;
		y /= pScalar;
		return this;
	}

	// linear algebra operators ////////////////////////////////////////////////

	public float Dot(XYf pOther)
	{
		return x * pOther.x + y * pOther.y;
	};

	public float Magnitude()
	{
		return (float) Math.sqrt(x * x + y * y);
	}

	public XYf Normalize()
	{
		float tMag = Magnitude();
		x /= tMag;
		y /= tMag;
		return this;
	}
}
