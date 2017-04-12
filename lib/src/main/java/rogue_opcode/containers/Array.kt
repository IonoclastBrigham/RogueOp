// Array.kt
// Dynamical array container class
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


/**
 * When directly accessing the underlying `mData` array, indices
 * `[0, mSize)` are valid.
 *
 * @param <E> Generic storage type parameter.
 * @see rogue_opcode.containers.Container
 * @author Brigham Toskin
 */
@Suppress("UNCHECKED_CAST")
open class Array<E> : Container<E>, MutableIterator<E>, Iterable<E> {
	companion object {
		private val serialVersionUID = -90938033040808162L
	}


	internal var mIteratorIndex: Int = 0

	// c'tor //

	/** Default constructor preallocates space for up to 32 elements.
	 */
	constructor() : super(32)

	/**
	 * Preallocates space for up to `pCapacity` elements.
	 * @param pCapacity number of elements to allocate space for.
	 */
	constructor(pCapacity: Int) : super(pCapacity)

	// mData access /////////////////////////////////////////////////////////////

	/**
	 * @see rogue_opcode.containers.Container.First
	 */
	override fun First(): E {
		validate_nonempty()
		return mData[0] as E
	}

	/**
	 * @see rogue_opcode.containers.Container.Last
	 */
	override fun Last(): E {
		validate_nonempty()
		return mData[LastIndex] as E
	}

	/**
	 * Retrieve element at index `pOffset`.

	 * @param pIndex element index to retrieve.
	 * *
	 * @see rogue_opcode.containers.Container.At
	 */
	override fun At(pIndex: Int): E {
		validate_index(pIndex)
		return mData[pIndex] as E
	}

	/** Appends a new element to the end of the array, reallocating the buffer
	 * if necessary.
	 * @param pVal the item to append.
	 */
	open fun Append(pVal: E) {
		resize_inc()
		mData[LastIndex] = pVal
	}

	// Iterators ///////////////////////////////////////////////////////////////

	override fun hasNext(): Boolean {
		return mIteratorIndex < mSize
	}

	override fun next(): E {
		validate_index(mIteratorIndex)
		return mData[mIteratorIndex++] as E
	}

	override fun remove() {} // TODO

	override fun iterator(): MutableIterator<E> {
		mIteratorIndex = 0
		return this
	}
}
