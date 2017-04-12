// Rectanglef.kt
// Floating point rectangle class
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

import android.graphics.RectF
import java.io.Serializable


/**
 * @author Brigham Toskin
 */
class Rectanglef() : Serializable {
	companion object {
		private const val serialVersionUID = 1988188856826226217L
	}


	protected var mRect = RectF()
	protected var w: Float = 0f
	protected var h: Float = 0f

	constructor(pX: Float, pY: Float, pW: Float, pH: Float) : this() {
		mRect.left = pX
		mRect.top = pY
		w = pW
		h = pH
		mRect.right = mRect.left + w
		mRect.bottom = mRect.top + h
	}

	fun X(): Float {
		return mRect.left
	}

	fun X(pX: Float) {
		mRect.left = pX
	}

	fun Y(): Float {
		return mRect.top
	}

	fun Y(pY: Float) {
		mRect.top = pY
	}

	fun W(): Float {
		return w
	}

	fun W(pW: Float) {
		w = pW
		mRect.right = mRect.left + w
	}

	fun H(): Float {
		return h
	}

	fun H(pH: Float) {
		h = pH
		mRect.bottom = mRect.top + h
	}

	fun toRectF(): RectF {
		return mRect
	}
}
