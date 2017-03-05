// SuperHashMap.java
// High performance string-indexed hashtable
//
// Copyright © 2010-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// hash() method based on super_fast_hash.c
// Copyright © 2004-2008 Paul Hsieh.
//
// Formatting:
// 80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.containers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import static java.lang.System.err;


// Overrides HashMap's key-indexed functions to use a custom hashcode digest,
// rather than the standard String.hashCode().
public class StringMap<V> extends HashMap<Integer, V>
{
	private static final long serialVersionUID = 7710690646684396854L;
	
	private Set<String> mKeySet;
	
	// c'tors //////////////////////////////////////////////////////////////////

	public StringMap()
	{
		super();
	}
	
	public StringMap(int initialCapacity)
	{
		super(initialCapacity);
	}
	
	public StringMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}
	
	public StringMap(Map<? extends Integer,? extends V> m)
	{
		super(m);
	}
	
	// HashMap overrides ///////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see java.util.HashMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key)
	{
//		err.println("containsKey(" + key + ": " + hash((String)key) + ")");
		return super.containsKey(hash((String)key));
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	@Override
	public V get(Object key)
	{
//		err.println("get(" + key + ": " + hash((String)key) + ")");
		return super.get(hash((String)key));
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#keySet()
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set keySet()
	{
		return new HashSet<String>(mKeySet);
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	public V put(String key, V value)
	{
//		err.println("put(" + key + ": " + hash((String)key) + ")");
		if(mKeySet == null)
			mKeySet = new HashSet<String>();
		mKeySet.add(key);
		return super.put(hash((String)key), value);
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key)
	{
		mKeySet.remove(key);
		return super.remove(hash((String)key));
	}
	
	@Override
	public String toString()
	{
		String tString = "{";
		for(String tKey : mKeySet)
			tString = tString + tKey + "=" + get(tKey) + ", ";
		tString = tString.substring(0, tString.lastIndexOf(",")) + "}";
		return tString;
	}

	// private util methods ////////////////////////////////////////////////////

	private int hash(String pData)
	{
		if(pData == null || pData.length() == 0)
			return 0;

		int tLen = pData.length();
		int tHash = tLen;
		int next;
		int tRemainder;

		tRemainder = tLen & 3;
		//tLen >>= 2;

		// iterate over data chars
		int i = 0;
		for(; tLen - i >= 4; i+=4)
		{
			tHash += get_word(pData, i);
			next = (get_word(pData, i+2) << 11) ^ tHash;
			tHash = (tHash << 16) ^ next;
			tHash += tHash >> 11;
		}

		switch(tRemainder)
		{
		case 3:
			tHash += get_word(pData, i);
			tHash ^= tHash << 16;
			tHash ^= pData.charAt(i+ 2) << 18;
			tHash += tHash >> 11;
			break;
		case 2:
			tHash += get_word(pData, i);
			tHash ^= tHash << 11;
			tHash += tHash >> 17;
			break;
		case 1:
			tHash += pData.charAt(i);
			tHash ^= tHash << 10;
			tHash += tHash >> 1;
		}

		// Avalanche the remaining bits
		tHash ^= tHash << 3;
		tHash += tHash >> 5;
		tHash ^= tHash << 4;
		tHash += tHash >> 17;
		tHash ^= tHash << 25;
		tHash += tHash >> 6;

		return tHash;
	}
	
	private int get_word(String pData, int i)
	{
		return (pData.charAt(i) << 8) + pData.charAt(i + 1) & 0x0000ffff;
	}
}
