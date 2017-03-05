// XY.java
// XY class
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

import android.graphics.Point;


/** The only purpose of this class is to make Point serializable. */
public class XY extends Point implements Serializable
{
	private static final long serialVersionUID = -6432112948349497390L;

	public XY(int pX, int pY)
	{
		super(pX, pY);
	}
}