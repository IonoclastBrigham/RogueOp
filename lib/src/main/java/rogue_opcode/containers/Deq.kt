// Deq.kt
// Dynamical array-based double-ended, circular queue class
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

import java.util.*
import kotlin.Array


/**
 * *This class implements an array-based, double-ended, circular queue.*
 *
 * Allocates in power-of-two chunks for efficiency. Handles looping by always
 * doing `mData[mask&(head+i)]` which is a cheap alternative to either a
 * mod or two compares and an assignment. For example, an array mSize of 16 would
 * give us a mask of 15 (or 00001111 binary). Assuming the head is at 0, if we
 * ask for elements -1 or 17, we would get:
 * ` 1. 00001111 & 11111111 = 00001111 (mData[-1] => mData[15])
 *  1. 00001111 & 00010001 = 00000001 (mData[17] => mData[1])`
 *
 * @param <E> Generic storage type parameter.
 * @see rogue_opcode.containers.Container
 * @author Brigham Toskin
 */
@Suppress("UNCHECKED_CAST")
class Deq<E> : Container<E> {
	companion object {
		private val serialVersionUID = 225699902551302855L

		/**
		 * Default mSize to allocate storage for.
		 * **Const value:** `32`
		 */
		const val DEFAULT_ALLOCATION_SIZE = 32
	}


	var head = 0
	var tail = 0
	var mask: Int = 0

	// c'tor //

	/**
	 * Construct a queue with a default capacity of DEFAULT_ALLOCATION_SIZE.

	 * @see .DEFAULT_ALLOCATION_SIZE
	 */
	constructor() : super(DEFAULT_ALLOCATION_SIZE)

	/**
	 * Construct a queue with requested capacity. Note that the requested
	 * capacity should be a power of two; it will be increased to the next
	 * highest power of two, if it is not.

	 * @param pCapacity capacity to preallocate.
	 */
	constructor(pCapacity: Int) : super(nextPow2(pCapacity))

	// memory management ///////////////////////////////////////////////////////

	/**
	 * This is overridden to handle tail wrapping and non-zero heads. Note that
	 * the requested capacity must be a power of two, or you will break mostly
	 * everything.
	 * @param pCount number of elements to preallocate space for.
	 * @throws ContainerError on allocation failure
	 */
	override fun Reserve(pCount: Int) {
		// shortcut for empty containers
		if(mSize == 0) {
			super.Reserve(pCount)
			mask = nextPow2(mData.size) - 1
			return
		}

		val tOldData = mData as Array<out E>
		val tLength = tOldData.size
		if(pCount <= tLength) return

		val tHead = head // grab local ref
		val tTail = tail // grab local ref

		val tData: Array<in E?>
		try {
			tData = arrayOfNulls<Any?>(pCount) as Array<in E?>
			//			validate_alloc(tData);
		} catch(e: OutOfMemoryError) {
			throw ContainerError("Allocation failure", e)
		}

		// We want to copy all of the buffer's current valid contents into a
		// new contiguous block of memory. Because this is a circular queue,
		// we have two possible cases to handle:
		//   1. The mData has wrapped around the end of the buffer
		//   2. The mData is in a contiguous block
		// In either case, the head may not be at zero, so we must take this
		// into account we calculating index ranges.
		if(tHead >= tTail)
		// case 1: mData has wrapped around
		{
			for(i in tHead..tLength - 1) {
				// copy from head to end
				tData[i - tHead] = tOldData[i]
			}
			for(i in 0..tTail - 1) {
				// copy from start to tail
				tData[i + tLength - tHead] = tOldData[i]
			}
		} else {
			// case 2: mData is contiguous
			for(i in tHead..tTail - 1) {
				tData[i] = tOldData[i]
			}
		}

		head = 0
		tail = mSize
		mask = pCount - 1
		mData = tData as Array<Any?>
	}

	// accessors ///////////////////////////////////////////////////////////////

	/** Returns the element at the specified index.
	 * @param pIndex index of element to retrieve.
	 * *
	 * @see rogue_opcode.containers.Container.At
	 */
	override fun At(pIndex: Int): E {
		validate_index(pIndex)
		val tIndex = head + pIndex
		return mData[mask and tIndex] as E
	}

	/**
	 * @see rogue_opcode.containers.Container.First
	 */
	override fun First(): E {
		validate_nonempty()
		return mData[head] as E
	}

	/**
	 * @see rogue_opcode.containers.Container.Last
	 */
	override fun Last(): E {
		validate_nonempty()
		val tIndex = tail - 1
		return mData[mask and tIndex] as E
	}

	/**
	 * @param pVal object to add to back of queue.
	 * *
	 * @throws ContainerError on allocation failure.
	 */
	fun PushBack(pVal: E) {
		resize_inc()
		mData[tail++] = pVal
		tail = tail and mask
	}

	/**
	 * @param pVal object to add to back of queue.
	 * *
	 * @throws ContainerError on allocation error
	 */
	fun PushFront(pVal: E) {
		resize_inc()
		val tHead = head - 1 and mask
		mData[tHead] = pVal
		head = tHead
	}

	/**
	 * Removes and returns the last element.
	 * @return The element from the back of the queue.
	 * @throws ContainerError if empty.
	 */
	fun PopBack(): E {
		validate_nonempty()

		val tTail = tail - 1 and mask
		tail = tTail
		val tVal = mData[tTail] as E
		mData[tTail] = null
		mSize--
		return tVal
	}

	/**
	 * Removes and returns the first element.
	 * @return The element from the front of the queue.
	 * @throws ContainerError if empty.
	 */
	fun PopFront(): E {
		validate_nonempty()

		val tHead = head
		head = tHead + 1 and mask
		val tVal = mData[tHead] as E
		mData[tHead] = null
		mSize--
		return tVal
	}

	/**
	 * *Removes an arbitrary element from the middle of the queue.*
	 *
	 * This removes the nth element from the head of the queue, in contrast to
	 * the nth element from the start of the backing array.
	 *
	 * **Linear copy is inefficient; you probably should not be doing this in a
	 * loop, or for large datasets.** If you will be removing the the start or
	 * end of the queue, [PopBack] and [PopFront] are more efficient.
	 *
	 * @param pIndex index of element to remove.
	 * @throws ContainerError if empty.
	 *
	 * @see rogue_opcode.containers.Container.Remove
	 * @see PopBack
	 * @see PopFront
	 */
	// TODO: optimize to copy down shorter partition
	override fun Remove(pIndex: Int) {
		validate_nonempty()

		val tIndex = pIndex + head and mask

		if(head < tail) {
			// shortcut for simple linear layout
			mSize += head // fake out Remove algo to get full range
			super.Remove(tIndex)
			mSize -= head
			tail-- // tail was > head so no need to wrap
			return
		}

		// from here, we are in a wrapped layout //

		// find and null-out element
		val tData = mData // get local ref
		validate_index(pIndex)
		tData[tIndex] = null
		val tHead = head
		val tTail = tail

		if(pIndex < tTail) {
			// removing from wrapped region
			for(i in tIndex + 1..tTail - 1) {
				tData[i - 1] = tData[i]
			}
			tail--
		} else {
			// removing from non-wrapped region
			for(i in tIndex - 1 downTo tHead) {
				tData[i + 1] = tData[i]
			}
			head++
		}
		mSize--
	}

	private inner class DeqIterator : Container<E>.ContainerIterator() {
		override operator fun next(): E {
			mDirty = false
			if(!hasNext()) throw NoSuchElementException("End of container.")
			return At(mIndex++)
		}
	}

	override fun iterator(): MutableIterator<E> {
		return DeqIterator()
	}
}
