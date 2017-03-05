// Array.java
// Dynamical array container class
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


/**
 * When directly accessing the underlying {@code data} array, indices
 * {@code [0, size)} are valid.
 *
 * @param <E> Generic storage type parameter.
 * @see rogue_opcode.containers.Container
 * @author Brigham Toskin
 */
public class Array<E> extends Container<E> implements Iterator<E>, Iterable<E>
{
	private static final long serialVersionUID = -90938033040808162L;
	int mIteratorIndex;

	// c'tor //

	/** Default constructor preallocates space for up to 32 elements.
	 */
	public Array()
	{
		super(32);
	}

	/** Preallocates space for up to {@code pCapacity} elements.
	 * @param pCapacity number of elements to allocate space for.
	 */
	public Array(int pCapacity)
	{
		super(pCapacity);
	}

	// data access /////////////////////////////////////////////////////////////

	/**
	 * @see rogue_opcode.containers.Container#First()
	 */
	@Override
	public E First()
	{
		validate_nonempty();
		return data[0];
	}

	/**
	 * @see rogue_opcode.containers.Container#Last()
	 */
	@Override
	public E Last()
	{
		validate_nonempty();
		return data[size - 1];
	}

	/**
	 * Retrieve element at index {@code pOffset}.
	 *
	 * @param pIndex element index to retrieve.
	 * @see rogue_opcode.containers.Container#At(int)
	 */
	@Override
	public E At(int pIndex)
	{
		validate_index(pIndex);
		return data[pIndex];
	}

	/** Appends a new element to the end of the array, reallocating the buffer
	 * if necessary.
	 * @param pVal the item to append.
	 */
	public void Append(E pVal)
	{
		resize_inc();
		data[size-1] = pVal;
	}


	@Override
	public boolean hasNext()
	{
		return (mIteratorIndex < size);
	}

	@Override
	public E next()
	{
		validate_index(mIteratorIndex);
		return data[mIteratorIndex ++];
	}

	@Override
	public void remove()
	{
	}

	@Override
	public Iterator<E> iterator()
	{
		mIteratorIndex = 0;
		return this;
	}
}
