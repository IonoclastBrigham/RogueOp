// LazyPQueue.java
// LazyPQueue class
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


package rogue_opcode.containers;

import java.util.Arrays;
import java.util.Comparator;


/**
 * {@code LazySortedArray} is an efficient, array-based, sorted structure. It is
 * designed for situations which require a lot of runtime manipulation of sorted
 * contents. It avoids doing fix-ups in tight loops, instead simply marking the
 * structure as "dirty". Because the underlying structure is an array, it avoids
 * activating garbage collection when manipulating the contents as a node-based
 * structure might.
 * <br /><br />
 * The target use case involves one or more loops that
 * will modify the contents and/or structure of the underlying array, with a
 * single point where the structure truly needs to be sorted. Upon reaching this
 * point, the program can execute a single sort action to resynchronize the
 * structure.
 *
 * @param <E> the storage type parameter.
 * @author Brigham Toskin
 */
public class LazySortedArray<E> extends Array<E>
{
	private static final long serialVersionUID = 4854620935053392136L;

	public boolean mDirty = false;
	protected Comparator<E> mComparator;

	// c'tors //

	/**
	 * Constructs a sorted array instance, and sets the sorting Comparator to
	 * the supplied reference.
	 *
	 * @param pCmp the Comparator to use for sorting operations.
	 */
	public LazySortedArray(Comparator<E> pCmp)
	{
		super();
		mComparator = pCmp;
	}

	/**
	 * Constructs a sorted array instance with the specified initial capacity,
	 * and sets the sorting Comparator to the supplied reference.
	 *
	 * @param pCmp the Comparator to use for sorting operations.
	 * @param pCapacity initial capacity to allocate.
	 */
	public LazySortedArray(Comparator<E> pCmp, int pCapacity)
	{
		super(pCapacity);
		mComparator = pCmp;
	}

	// container interfaces ////////////////////////////////////////////////////

	/**
	 * Appends a new element to the end of the array, reallocating the buffer if
	 * necessary. This marks the array as dirty.
	 *
	 * @param pVal the item to append.
	 */
	@Override
	public void Append(E pVal)
	{
		synchronized(this)
		{
			super.Append(pVal);
			mDirty = true;
		}
	}

	/**
	 * Clears this array, and resets the dirty flag.
	 */
	@Override
	public void Clear()
	{
		synchronized(this)
		{
			super.Clear();
			mDirty = false;
		}
	}

	/**
	 * @return the first element, sorting the data first if necessary.
	 */
	@Override
	public E First()
	{
		SortIfDirty();
		return super.First();
	}

	/**
	 * @return the last element, sorting the data first if necessary.
	 */
	@Override
	public E Last()
	{
		SortIfDirty();
		return super.Last();
	}

	/**
	 * Retrieve element at offset <code>pIndex</code>. This marks the array as
	 * dirty.
	 * 
	 * @param pIndex offset from beginning of array to retrieve element.
	 * @return the element at the requested index.
	 */
	@Override
	public E At(int pIndex)
	{
		mDirty = true;
		return super.At(pIndex);
	}

	// sorting interfaces //////////////////////////////////////////////////////

	/**
	 * Sorts the array.
	 */
	public void Sort()
	{
		synchronized(this)
		{
			if(size < 1)
				return;
			// place min at head
			/*
			 * int tMinIndex = 0;
			 * for(int i = 1; i < data.length; i++)
			 * {
			 * if(mComparator.compare(data[tMinIndex], data[i]) > 0)
			 * tMinIndex = i;
			 * }
			 * if(tMinIndex > 0)
			 * {
			 * E tVal = data[0];
			 * data[0] = data[tMinIndex];
			 * data[tMinIndex] = tVal;
			 * }
			 *
			 * // sort remaining array
			 * for(int i = 2; i <= data.length; i++)
			 * {
			 * E tVal = data[i];
			 * int j;
			 * for(j = i; mComparator.compare(tVal, data[j - 1]) < 0; j--)
			 * data[j] = data[j - 1];
			 * data[j] = tVal;
			 * }
			 */
			try
			{
				Arrays.sort(data, 0, size, mComparator);
			}
			catch(Exception e)
			{
			}
			mDirty = false;
		}
	}

	/**
	 * Sorts the array if in a dirty state.
	 */
	public void SortIfDirty()
	{
		if(mDirty)
			Sort();
	}
}
