// Deq.java
// Dynamical array-based double-ended, circular queue class
//
// Copyright © 2009-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.containers;

import java.util.Iterator;
import java.util.NoSuchElementException;


/** This class implements an array-based, double-ended, circular queue.
 * <br /><br />
 * Allocates in power-of-two chunks for efficiency. Handles looping by always
 * doing {@code data[mask&(head+i)]} which is a cheap alternative to either a
 * mod or two compares and an assignment. For example, an array size of 16 would
 * give us a mask of 15 (or 00001111 binary). Assuming the head is at 0, if we
 * ask for elements -1 or 17, we would get:
 * <code><ol><li />00001111 & 11111111 = 00001111 (data[-1] => data[15])
 * <li />00001111 & 00010001 = 00000001 (data[17] => data[1])</ol></code>
 *
 * @param <E> Generic storage type parameter.
 * @see rogue_opcode.containers.Container
 * @author Brigham Toskin
 */
public class Deq<E> extends Container<E>
{
	private static final long serialVersionUID = 225699902551302855L;

	/**
	 * Default size to allocate storage for.
	 * <br/><br/>
	 * <b>Const value:</b> <code>32</code>
	 */
	public static final int DEFAULT_ALLOCATION_SIZE = 32;

	public int head = 0, tail = 0, mask;

	// c'tor //

	/**
	 * Construct a queue with a default capacity of DEFAULT_ALLOCATION_SIZE.
	 *
	 * @see #DEFAULT_ALLOCATION_SIZE
	 */
	public Deq()
	{
		super(DEFAULT_ALLOCATION_SIZE);
	}

	/**
	 * Construct a queue with requested capacity. Note that the requested
	 * capacity should be a power of two; it will be increased to the next
	 * highest power of two, if it is not.
	 *
	 * @param pCapacity capacity to preallocate.
	 */
	public Deq(int pCapacity)
	{
		super(next_power_of_2(pCapacity));
	}

	// memory management ///////////////////////////////////////////////////////

	/**
	 * This is overridden to handle tail wrapping and non-zero heads. Note that
	 * the requested capacity must be a power of two, or you will break mostly
	 * everything.
	 *
	 * @param pCount number of elements to preallocate space for.
	 * @throws ContainerError on allocation failure
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void Reserve(int pCount)
	{
		// shortcut for empty containers
		if(data == null || size == 0)
		{
			super.Reserve(pCount);
			mask = pCount - 1;
			return;
		}

		E[] tOldData = data; // grab local ref
		int tLength = tOldData.length; // grab local ref
		if(pCount <= tLength)
			return;
		int tHead = head; // grab local ref
		int tTail = tail; // grab local ref

		E[] tData;
		try
		{
			tData = (E[])new Object[pCount];
			//			validate_alloc(tData);
		}
		catch(OutOfMemoryError e)
		{
			throw new ContainerError("Allocation failure", e);
		}

		// We want to copy all of the buffer's current valid contents into a
		// new contiguous block of memory. Because this is a circular queue,
		// we have two possible cases to handle:
		//   1. The data has wrapped around the end of the buffer
		//   2. The data is in a contiguous block
		// In either case, the head may not be at zero, so we must take this
		// into account we calculating index ranges.
		if(tHead >= tTail)	// case 1: data has wrapped around
		{
			for(int i = tHead; i < tLength; i++) // copy from head to end
				tData[i-tHead] = tOldData[i];
			for(int i = 0; i < tTail; i++) // copy from start to tail
				tData[i+tLength-tHead] = tOldData[i];
		}
		else				// case 2: data is contiguous
			for(int i = tHead; i < tTail; i++)
				tData[i] = tOldData[i];

		head = 0;
		tail = size;
		mask = pCount - 1;
		data = tData;
	}

	// accessors ///////////////////////////////////////////////////////////////

	/** Returns the element at the specified index.
	 * @param pIndex index of element to retrieve.
	 * @see rogue_opcode.containers.Container#At(int)
	 */
	@Override
	public E At(int pIndex)
	{
		validate_index(pIndex);
		int tIndex = head + pIndex;
		return data[mask&tIndex];
	}

	/**
	 * @see rogue_opcode.containers.Container#First()
	 */
	@Override
	public E First()
	{
		validate_nonempty();
		return data[head];
	}

	/**
	 * @see rogue_opcode.containers.Container#Last()
	 */
	@Override
	public E Last()
	{
		validate_nonempty();
		return data[tail-1];
	}

	/**
	 * @param pVal object to add to back of queue.
	 * @throws ContainerError on allocation failure.
	 */
	public void PushBack(E pVal)
	{
		resize_inc();
		data[tail++] = pVal;
		tail &= mask;
	}

	/**
	 * @param pVal object to add to back of queue.
	 * @throws ContainerError on allocation error
	 */
	public void PushFront(E pVal)
	{
		resize_inc();
		int tHead = (head - 1) & mask;
		data[tHead] = pVal;
		head = tHead;
	}

	/**
	 * Removes and returns the last element.
	 *
	 * @return The element from the back of the queue.
	 * @throws ContainerError if empty.
	 */
	public E PopBack()
	{
		validate_nonempty();

		int tTail = (tail - 1) & mask;
		E tVal = data[tTail];
		data[tTail] = null;
		tail = tTail;
		size--;
		return tVal;
	}

	/**
	 * Removes and returns the first element.
	 *
	 * @return The element from the front of the queue.
	 * @throws ContainerError if empty.
	 */
	public E PopFront()
	{
		validate_nonempty();

		int tHead = head;
		E tVal = data[tHead];
		data[tHead] = null;
		head = (tHead +1) & mask;
		size--;
		return tVal;
	}

	/**
	 * Removes an arbitrary element from the middle of the queue.<br />
	 * <b>Linear copy is inefficient; you probably should not be doing this in a
	 * loop, or for large datasets.</b>
	 *
	 * @param pIndex index of element to remove.
	 * @see rogue_opcode.containers.Container#Remove(int)
	 */
	// TODO: optimize to copy down shorter partition
	@Override
	public void Remove(int pIndex) // overload for special case in circular queue
	{
		int tIndex = (pIndex + head) & mask;

		if(head < tail) // shortcut for simple linear layout
		{
			size += head; // fake out Remove algo to get full range
			super.Remove(tIndex);
			size -= head;
			tail--; // tail was > head so no need to wrap
			return;
		}

		// from here, we are in a wrapped layout //

		// find and null-out element
		E[] tData = data; // get local ref
		validate_index(pIndex);
		tData[tIndex] = null;
		int tHead = head, tTail = tail;

		// removing from wrapped region
		if(pIndex < tTail)
		{
			for(int i = tIndex + 1; i < tTail; i++)
				tData[i-1] = tData[i];
			tail--;
		}
		// removing from non-wrapped region
		else
		{
			for(int i = tIndex - 1; i >= tHead; i--)
				tData[i+1] = tData[i];
			head++;
		}
		size--;
	}

	protected class DeqIterator extends ContainerIterator
	{
		@Override
		public E next()
		{
			mDirty = false;
			if(!hasNext())
				throw new NoSuchElementException("End of container.");
			return At(mIndex++);
		}
	}

	@Override
	public Iterator<E> iterator()
	{
		return new DeqIterator();
	}

	protected static int next_power_of_2(final int pInitialValue)
	{
		int tPow2 = pInitialValue;
		tPow2 |= tPow2 >> 1;
		tPow2 |= tPow2 >> 2;
		tPow2 |= tPow2 >> 4;
		tPow2 |= tPow2 >> 8;
		tPow2 |= tPow2 >> 16;
		return tPow2 + 1;
	}
}
