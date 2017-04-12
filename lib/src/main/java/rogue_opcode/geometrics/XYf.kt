// XYf.kt
// floating point 2D point/vector class
//
// Copyright © 2010-2017 Brigham Toskin, Leea Harlan
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <https://github.com/IonoclastBrigham/RogueOp/blob/master/LICENSE.md>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode.geometrics

import android.graphics.Point
import java.io.Serializable


/**
 * *Cartesian (x, y) or vector components <i, j>.*
 * @author Brigham Toskin
 */
open class XYf
	@JvmOverloads constructor(var x: Float = 0.0f, var y: Float = 0.0f)
	: Serializable {

	companion object {
		private const val serialVersionUID = -3490122379205325810L
	}


	constructor(pSourceXY: XYf) : this(pSourceXY.x, pSourceXY.y) {}

	// blah
	override fun toString(): String {
		return "($x, $y)"
	}

	fun toPoint(): Point {
		return Point(Math.round(x), Math.round(y))
	}

	fun set(pSourceXY: XYf) {
		x = pSourceXY.x
		y = pSourceXY.y
	}

	// arithmetic operators ////////////////////////////////////////////////////

	operator fun plus(pOther: XYf) = XYf(pOther.x + x, pOther.y + y)

	operator fun minus(pOther: XYf) = XYf(x - pOther.x, y - pOther.y)

	open operator fun times(pScalar: Float) = XYf(x * pScalar, y * pScalar)

	open operator fun div(pScalar: Float) = XYf(x / pScalar, y / pScalar)

	fun add(pOther: XYf): XYf {
		x += pOther.x
		y += pOther.y
		return this
	}

	fun subtract(pOther: XYf): XYf {
		x -= pOther.x
		y -= pOther.y
		return this
	}

	open fun multiplyBy(pScalar: Float): XYf {
		x *= pScalar
		y *= pScalar
		return this
	}

	open fun divideBy(pScalar: Float): XYf {
		x /= pScalar
		y /= pScalar
		return this
	}

	// linear algebra operators ////////////////////////////////////////////////

	// TODO: cross-product

	fun Dot(pOther: XYf) = x * pOther.x + y * pOther.y

	open fun Magnitude() = Math.sqrt((x * x + y * y).toDouble()).toFloat()

	open fun Normalize(): XYf {
		val tMag = Magnitude()
		x /= tMag
		y /= tMag
		return this
	}
}
