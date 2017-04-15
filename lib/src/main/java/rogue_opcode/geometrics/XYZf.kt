// XYZf.kt
// Three dimensional point/vector class
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


/**
 * *Cartesian (x, y, z) or vector components <i, j, k>.*
 * @author Brigham Toskin
 */
open class XYZf
	@JvmOverloads constructor(pX: Float = 0.0f, pY: Float = 0.0f,
	                          var z: Float = 0.0f) : XYf(pX, pY) {
	companion object {
		private val serialVersionUID = 4186038949433460479L

		val ZERO = XYZf()
		val X_HAT = XYZf(1f, 0f, 0f)
		val Y_HAT = XYZf(0f, 1f, 0f)
		val Z_HAT = XYZf(0f, 0f, 1f)
	}


	constructor(pSource: XYZf) : this(pSource.x, pSource.y, pSource.z)

	fun Set(pSource: XYZf) {
		x = pSource.x
		y = pSource.y
		z = pSource.z
	}

	// arithmetic operators ////////////////////////////////////////////////////

	operator fun plus(pOther: XYZf) = XYZf(pOther.x + x, pOther.y + y, pOther.z + z)

	operator fun minus(pOther: XYZf) = XYZf(x - pOther.x, y - pOther.y, z - pOther.z)

	override operator fun times(pScalar: Float): XYZf {
		return XYZf(x * pScalar, y * pScalar, z * pScalar)
	}

	override fun div(pScalar: Float): XYZf {
		return XYZf(x / pScalar, y / pScalar, z / pScalar)
	}

	fun add(pOther: XYZf): XYZf {
		x += pOther.x
		y += pOther.y
		z += pOther.z
		return this
	}

	fun subtract(pOther: XYZf): XYZf {
		x -= pOther.x
		y -= pOther.y
		z -= pOther.z
		return this
	}

	override fun multiplyBy(pScalar: Float): XYZf {
		x *= pScalar
		y *= pScalar
		z *= pScalar
		return this
	}

	override fun divideBy(pScalar: Float): XYZf {
		x /= pScalar
		y /= pScalar
		z /= pScalar
		return this
	}

	// linear algebra operators ////////////////////////////////////////////////

	// TODO: cross-product

	fun Dot(pOther: XYZf) = x * pOther.x + y * pOther.y + z * pOther.z

	override fun Magnitude() = Math.sqrt((x * x + y * y + z * z).toDouble())
		.toFloat()

	override fun Normalize(): XYZf {
		val tMag = Magnitude()
		x /= tMag
		y /= tMag
		z /= tMag
		return this
	}

	// Any overrides ///////////////////////////////////////////////////////////

	override fun equals(other: Any?) = when(other) {
		is XYZf -> x == other.x && y == other.y && z == other.z
		else -> false
	}

	override fun toString(): String {
		return "($x, $y, $z)"
	}
}
