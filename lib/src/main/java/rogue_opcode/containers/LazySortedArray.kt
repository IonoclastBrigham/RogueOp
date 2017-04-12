// LazyPQueue.kt
// LazyPQueue class
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


package rogue_opcode.containers

import java.util.*


/**
 * `LazySortedArray` is an efficient, array-based, sorted structure. It is
 * designed for situations which require a lot of runtime manipulation of sorted
 * contents. It avoids doing fix-ups in tight loops, instead simply marking the
 * structure as "dirty". Because the underlying structure is an array, it avoids
 * activating garbage collection when manipulating the contents as a node-based
 * structure might.
 * <br></br><br></br>
 * The target use case involves one or more loops that
 * will modify the contents and/or structure of the underlying array, with a
 * single point where the structure truly needs to be sorted. Upon reaching this
 * point, the program can execute a single sort action to resynchronize the
 * structure.

 * @param E the storage type parameter.
 * @author Brigham Toskin
 */
@Suppress("UNCHECKED_CAST")
class LazySortedArray<E>(pCapacity: Int, val mComparator: Comparator<in E>) : Array<E>(pCapacity) {
	companion object {
		private val serialVersionUID = 4854620935053392136L
	}


	private var mDirty = false

	// c'tors //

	/**
	 * Constructs a sorted array instance, and sets the sorting Comparator to
	 * the supplied reference.

	 * @param pCmp the Comparator to use for sorting operations.
	 */
	constructor(pCapacity: Int = 32, pCmp: (a: E, b: E)->Int) : this(pCapacity, Comparator(pCmp))

	// container interfaces ////////////////////////////////////////////////////

	/**
	 * Appends a new element to the end of the array, reallocating the buffer if
	 * necessary. This marks the array as dirty.

	 * @param pVal the item to append.
	 */
	override fun Append(pVal: E) {
		synchronized(this) {
			super.Append(pVal)
			mDirty = true
		}
	}

	/**
	 * Clears this array, and resets the dirty flag.
	 */
	override fun Clear() {
		synchronized(this) {
			super.Clear()
			mDirty = false
		}
	}

	/**
	 * @return the first element, sorting the mData first if necessary.
	 */
	override fun First(): E {
		SortIfDirty()
		return super.First()
	}

	/**
	 * @return the last element, sorting the mData first if necessary.
	 */
	override fun Last(): E {
		SortIfDirty()
		return super.Last()
	}

	/**
	 * Retrieve element at offset `pIndex`. This marks the array as
	 * dirty.

	 * @param pIndex offset from beginning of array to retrieve element.
	 * @return the element at the requested index.
	 */
	override fun At(pIndex: Int): E {
		mDirty = true
		return super.At(pIndex)
	}

	// sorting interfaces //////////////////////////////////////////////////////

	/**
	 * *Sorts the array.*
	 */
	@Synchronized
	fun Sort() {
		if(mSize < 2) return

		mData.sortWith(Comparator { a, b ->
			mComparator.compare(a as E, b as E)
		}, 0, LastIndex)

		mDirty = false
	}

	/**
	 * Sorts the array if in a dirty state.
	 */
	fun SortIfDirty() {
		if(mDirty) Sort()
	}
}
