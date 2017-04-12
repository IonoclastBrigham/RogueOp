// XY.kt
// XY class
//
// Copyright © 2010-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <https://github.com/IonoclastBrigham/RogueOp/blob/master/LICENSE.md>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.geometrics


import java.io.Serializable

import android.graphics.Point


/** The only purpose of this class is to make Point serializable.  */
class XY(pX: Int, pY: Int) : Point(pX, pY), Serializable {
	companion object {
		private const val serialVersionUID = -6432112948349497390L
	}
}