// Container.kt
// Dynamical array-based container abstract base class
//
// Copyright © 2009-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <https://github.com/IonoclastBrigham/RogueOp/blob/master/LICENSE.md>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.containers

import java.io.Serializable
import java.util.*
import kotlin.Array


/**
 * *Simple `Exception` class that builds a friendly error message.*
 * @see java.lang.Exception
 */
internal class ContainerError
/**
 * *Constructs an `Exception` with a friendly error message.*
 *
 * Message includes the name of the calling method two frames back. This is because
 * we don't usually throw one of these ourselves, but pawn it off to a
 * helper method.
 *
 * @param msg brief description of error condition.
 * @param context used to get stack trace.
 */
@JvmOverloads
constructor(msg: String, context: Throwable)
	: RuntimeException("Error in " + context.stackTrace[1].toString() + " - " + msg) {
	init {
		stackTrace = context.stackTrace.run { sliceArray(1..lastIndex) }
	}
}


/**
 * `Container` is an abstract base class for array-based mData structures.
 * Efficiency in storage, access, and memory management are the primary design
 * goals. `Container`'s *raison d'être* is to provide a fast access
 * to self-sizing collections of mData elements suitable for real-time game
 * applications.
 * <br></br><br></br>
 * With this in mind, all mData members have been
 * declared public and accessors should be utilized judiciously at runtime. When
 * accessing the `mData` array directly, you should always keep in mind
 * that the length of the array is not necessarily the same as the logical mSize
 * of the container. Thus, you should check both values when calculating range
 * iterations.
 * <br></br><br></br>
 * Note that `Container` is an abstract class; some of the
 * common and default interfaces have been implemented for you. To use
 * `Container`, extend it with your derived class, and implement the
 * missing
 * methods and any others that require customized behavior, *e.g.* for a
 * circular queue.

 * @param <E> Generic storage type parameter.
 * @author Brigham Toskin
 */
@Suppress("UNCHECKED_CAST")
abstract class Container<E>(pCapacity: Int) : Serializable, Iterable<E> {
	companion object {
		private const val serialVersionUID = -3657997720954461476L

		inline fun nextPow2(pVal: Int) = (pVal - 1).let { tVal ->
			tVal.or(tVal shr 1)
				.or(tVal shr 2)
				.or(tVal shr 4)
				.or(tVal shr 8)
				.or(tVal shr 16)
		} + 1
	}


	protected var mData = arrayOfNulls<Any?>(pCapacity)
	val Data get() = mData as Array<out E?>

	protected var mSize = 0
	val Size get() = mSize

	val LastIndex get() = mSize - 1

	// memory management ///////////////////////////////////////////////////////

	open fun Reserve(pCount: Int) {
		if(pCount <= mSize) return

		mData = try {
			Array(pCount) { i -> if(i in mData.indices) mData[i] else null }
		} catch(e: OutOfMemoryError) {
			throw ContainerError("Allocation failure", e)
		}
	}

	/** *Removes an arbitrary element from the middle of the container.*
	 *
	 * **Linear copy is inefficient; you probably should not be doing this.**
	 * @param pIndex index of element to remove.
	 */
	open fun Remove(pIndex: Int) {
		validate_index(pIndex)
		mData[pIndex] = null // "delete" the item
		val tLastSafeIndex = LastIndex - 1
		for(i in pIndex..tLastSafeIndex) {
			// copy  down if necessary
			mData[i] = mData[i + 1]
		}
		mData[LastIndex] = null
		mSize--
	}

	open fun Clear() {
		for(i in 0..LastIndex) mData[i] = null
		mSize = 0
	}

	fun Empty(): Boolean {
		return mSize == 0
	}

	// element access aliases //////////////////////////////////////////////////

	abstract fun First(): E

	abstract fun Last(): E

	abstract fun At(pIndex: Int): E

	protected open inner class ContainerIterator : MutableIterator<E> {
		internal var mIndex = 0
		internal var mDirty = true

		override fun hasNext() = mIndex < mSize

		override fun next(): E {
			mDirty = false
			if(!hasNext()) throw NoSuchElementException("End of container.")
			return mData[mIndex++] as E
		}

		override fun remove() {
			if(mDirty) throw IllegalStateException("remove() called before next()")
			Remove(--mIndex)
		}
	}

	override fun iterator(): MutableIterator<E> {
		return ContainerIterator()
	}

	protected fun validate_nonempty() {
		if(mSize < 1) {
			val ex = Exception()
			throw ContainerError("Accessing empty container.", ex)
		}
	}

	protected fun validate_index(pIndex: Int) {
		if(pIndex >= mSize) {
			val ex = Exception()
			throw ContainerError("Index out of bounds.", ex)
		}
	}

	protected fun resize_inc() {
		if(mData.size == mSize) Reserve(mSize * 2)
		mSize++
	}

	protected fun resize_add(pCount: Int) {
		val tRequired = mSize + pCount
		var tCapacity = mData.size
		if(tRequired > tCapacity) {
			Reserve(nextPow2(tRequired))
		}
	}
}
