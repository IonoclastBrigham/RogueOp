// Rectangle.kt
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


package rogue_opcode.geometrics

import android.graphics.Rect
import java.io.Serializable


/**

 * @author Brigham Toskin
 */
class Rectangle() : Serializable {
	companion object {
		private const val serialVersionUID = -7071451744257630068L
	}


	protected var mRect: Rect = Rect()
	protected var w: Int = 0
	protected var h: Int = 0

	constructor(pX: Int, pY: Int, pW: Int, pH: Int) : this() {
		mRect.left = pX
		mRect.top = pY
		w = pW
		h = pH
		mRect.right = mRect.left + w
		mRect.bottom = mRect.top + h
	}

	fun X(): Int {
		return mRect.left
	}

	fun X(pX: Int) {
		mRect.left = pX
	}

	fun Y(): Int {
		return mRect.top
	}

	fun Y(pY: Int) {
		mRect.top = pY
	}

	fun W(): Int {
		return w
	}

	fun W(pW: Int) {
		w = pW
		mRect.right = mRect.left + w
	}

	fun H(): Int {
		return h
	}

	fun H(pH: Int) {
		h = pH
		mRect.bottom = mRect.top + h
	}

	fun toRect(): Rect {
		return mRect
	}
}
