// Container.java
// Dynamical array-based container abstract base class
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

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;


/** Simple {@code Exception} class that builds a friendly error message.
 * @see java.lang.Exception
 */
@SuppressWarnings("serial")
class ContainerError extends RuntimeException
{
	/** Constructs an {@code Exception} with a friendly error message, which
	 * includes the name of the calling method two frames back. This is because
	 * we don't usually throw one of these ourselves, but pawn it off to a
	 * helper method.
	 * @param error_msg brief description of error condition.
	 * @param context used to get stack trace.
	 */
	ContainerError(String error_msg, Throwable context)
	{
		super("Error in " + context.getStackTrace()[1].toString() + " - " +
				error_msg, context);
	}

	/**
	 * Constructs an {@code Exception} with a friendly error message, which
	 * includes the name of the calling method two frames back. This is because
	 * we don't usually throw one of these ourselves, but pawn it off to a
	 * helper method.
	 *
	 * @param error_msg brief description of error condition.
	 */
	ContainerError(String error_msg)
	{
		this(error_msg, null);
	}
}


/**
 * {@code Container} is an abstract base class for array-based data structures.
 * Efficiency in storage, access, and memory management are the primary design
 * goals. {@code Container}'s <i>raison d'être</i> is to provide a fast access
 * to self-sizing collections of data elements suitable for real-time game
 * applications.
 * <br /><br />
 * With this in mind, all data members have been
 * declared public and accessors should be utilized judiciously at runtime. When
 * accessing the {@code data} array directly, you should always keep in mind
 * that the length of the array is not necessarily the same as the logical size
 * of the container. Thus, you should check both values when calculating range
 * iterations.
 * <br /><br />
 * Note that {@code Container} is an abstract class; some of the
 * common and default interfaces have been implemented for you. To use
 * {@code Container}, extend it with your derived class, and implement the
 * missing
 * methods and any others that require customized behavior, <i>e.g.</i> for a
 * circular queue.
 *
 * @param <E> Generic storage type parameter.
 * @author Brigham Toskin
 */
public abstract class Container<E> implements Serializable, Iterable<E>
{
	private static final long serialVersionUID = -3657997720954461476L;

	public int size;
	public E[] data;

	public Container(int pCapacity)// throws Exception
	{
		//		Clear();
		Reserve(pCapacity);
	}

	// memory management ///////////////////////////////////////////////////////

	public E[] Data()
	{
		return data;
	}

	@SuppressWarnings("unchecked")
	public void Reserve(int pCount)
	{
		E[] tData = data; // grab a local ref

		try
		{
			if(tData != null) // already have a data buffer
			{
				if(pCount <= tData.length)
					return; // bail if capacity grater than the requested size
				// copy into larger buffer
				E[] tNewData = (E[])new Object[pCount];
				for(int i = 0; i < tData.length; i++)
					tNewData[i] = tData[i];
				tData = tNewData;
			}
			else
				tData = (E[])new Object[pCount]; // allocate requested buffer
		}
		catch(OutOfMemoryError e)
		{
			throw new ContainerError("Allocation failure", e);
		}

		//		validate_alloc(tData);
		data = tData;
	}


	/** Removes an arbitrary element from the middle of the container.<br />
	 * <b>Linear copy is inefficient; you probably should not be doing this.</b>
	 * @param pIndex index of element to remove.
	 */
	public void Remove(int pIndex)
	{
		validate_index(pIndex);
		E[] tData = data; // grab local ref
		tData[pIndex] = null; // "delete" the item
		for(; pIndex < size - 1; pIndex++) // copy  down if necessary
			tData[pIndex] = tData[pIndex + 1];
		size--;
	}
	public void Clear()
	{
		int tCapacity = data.length;
		data = null;
		size = 0;
		try
		{
			Reserve(tCapacity);
		}
		catch(Exception e)
		{
		}
	}
	public boolean Empty() { return size == 0; }

	// element access aliases //////////////////////////////////////////////////

	public abstract E First();;

	public abstract E Last();;

	public abstract E At(int pIndex);;

	protected class ContainerIterator implements Iterator<E>
	{
		int mIndex = 0;
		boolean mDirty = true;

		@Override
		public boolean hasNext()
		{
			return mIndex < size;
		}

		@Override
		public E next()
		{
			mDirty = false;
			if(!hasNext())
				throw new NoSuchElementException("End of container.");
			return data[mIndex++];
		}

		@Override
		public void remove()
		{
			if(mDirty)
				throw new IllegalStateException("remove() called before next()");
			Remove(--mIndex);
		}
	}

	@Override
	public Iterator<E> iterator()
	{
		return new ContainerIterator();
	}

	// error checking and memory helpers ///////////////////////////////////////

	protected static void validate_pointer(Object pRef)
	{
		if(pRef == null)
			throw new ContainerError("Dereferencing null pointer.",
					new Exception());
	}

	protected static void validate_alloc(Object pRef)
	{
		if(pRef == null)
			throw new ContainerError("Allocation failed.", new Exception());
	}

	protected void validate_nonempty()
	{
		if(size < 1)
			throw new ContainerError("Accessing empty container.",
					new Exception());
	}

	protected void validate_index(int pIndex)
	{
		if(pIndex >= size)
			throw new ContainerError("Index out of bounds.", new Exception());
	}

	protected void resize_inc()
	{
		int tLen = data.length;
		if(size == tLen)
			Reserve(tLen << 1);
		size++;
	}

	protected void resize_add(int count)
	{
		if(size + count > data.length)
		{
			int cap = data.length;
			do cap <<= 1; while(size + count > cap); // find a power of 2 size
			Reserve(cap);
		}
	}
}
