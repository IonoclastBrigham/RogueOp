// SuperHashMap.kt
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


package rogue_opcode.containers

import android.util.SparseArray
import java.util.*
import kotlin.collections.MutableMap.MutableEntry


// Uses a custom hashcode digest,
// rather than the standard String.hashCode().
class StringMap<V> : MutableMap<String, V> {
	companion object {
		private val serialVersionUID = 7710690646684396854L
	}

	private inner class Entry(override val key: String) : MutableEntry<String, V> {
		override val value get() = this@StringMap[key]!!

		override fun setValue(pNewValue: V) = put(key, value)!!
	}


	private val mKeySet: MutableSet<String> by lazy { HashSet<String>() }
	private val mData: SparseArray<V>

	// c'tors //////////////////////////////////////////////////////////////////

	constructor() {
		mData = SparseArray()
	}

	constructor(pCapacity: Int) {
		mData = SparseArray(pCapacity)
	}

	constructor(pOther: Map<out String, V>) {
		mData = SparseArray(pOther.size)
		pOther.forEach { key, tVal -> this[key] = tVal }

	}

	// HashMap overrides ///////////////////////////////////////////////////////

	override val size get() = mData.size()
	override val keys get() = HashSet(mKeySet)
	override val values get() = HashSet<V>(size).also {
		for(k in mKeySet) it.add(mData[hash(k)])
	}
	override val entries get() = HashSet<MutableEntry<String,  V> >(size).also {
		for(k in mKeySet) it.add(Entry(k))
	}

	override fun isEmpty() = (size == 0)

	override fun containsKey(pKey: String) = (mData.indexOfKey(hash(pKey)) >= 0)

	override fun containsValue(pValue: V) = (mData.indexOfValue(pValue) >= 0)

	override operator fun get(pKey: String): V? = mData.get(hash(pKey))

	override fun put(pKey: String, pValue: V): V? {
		mKeySet.add(pKey)
		val tHash = hash(pKey)
		val tOld = mData[tHash]
		mData.put(tHash, pValue)
		return tOld
	}

	override fun remove(pKey: String): V? {
		mKeySet.remove(pKey)
		val tHash = hash(pKey)
		val tOld = mData[tHash]
		mData.remove(tHash)
		return tOld
	}

	override fun putAll(pOther: Map<out String, V>)
		= pOther.entries.forEach { (k, v) -> put(k, v) }

	override fun clear() {
		mKeySet.clear()
		mData.clear()
	}

	override fun toString()
		= mKeySet.foldIndexed(StringBuilder("{")) { i, tSB, tKey ->
			tSB.append(tKey).append(":").append(get(tKey))
				.append(if(i == mKeySet.size - 1) "" else ", ")
		} .append("}").toString()

	// private util methods ////////////////////////////////////////////////////

	private fun hash(pData: String?): Int {
		if(pData == null || pData.length == 0)
			return 0

		val tLen = pData.length
		var tHash = tLen
		var next: Int
		val tRemainder: Int

		tRemainder = tLen and 3
		//tLen >>= 2;

		// iterate over mData chars
		var i = 0
		while(tLen - i >= 4) {
			tHash += get_word(pData, i)
			next = get_word(pData, i + 1) shl 11 xor tHash
			tHash = tHash shl 16 xor next
			tHash += tHash shr 11
			i += 4
		}

		when(tRemainder) {
			3 -> {
				tHash += get_word(pData, i)
				tHash = tHash xor (tHash shl 16)
				tHash = tHash xor (pData[i + 2].toInt() shl 18)
				tHash += tHash shr 11
			}
			2 -> {
				tHash += get_word(pData, i)
				tHash = tHash xor (tHash shl 11)
				tHash += tHash shr 17
			}
			1 -> {
				tHash += pData[i].toInt()
				tHash = tHash xor (tHash shl 10)
				tHash += tHash shr 1
			}
		}

		// Avalanche the remaining bits
		tHash = tHash xor (tHash shl 3)
		tHash += tHash shr 5
		tHash = tHash xor (tHash shl 4)
		tHash += tHash shr 17
		tHash = tHash xor (tHash shl 25)
		tHash += tHash shr 6

		return tHash
	}

	private inline fun get_word(pData: String, i: Int) = pData[i].toInt()
}
