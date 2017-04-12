// ActionElement.kt
// Updatable interface for active entities.
//
// Copyright © 2010-2017 Christopher R. Tooley, Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <https://github.com/IonoclastBrigham/RogueOp/blob/master/LICENSE.md>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode

import rogue_opcode.containers.Array
import java.io.Serializable


/**
 * `ActionElement` is an abstract class that is intended to be extended
 * by the implementing program. Its sole function is to give objects a shot at
 * the processor at regular intervals.
 * <br></br><br></br>
 * All extant ActionElements may be accessed through the static member `sAllAEs`.
 * <br></br><br></br>
 * Example usage: extend this class to create a coconut factory which has no
 * graphical component, but still needs to determine on every game tick if it
 * should spawn a new coconut.
 */
abstract class ActionElement : Serializable {
	companion object {
		private const val serialVersionUID = 3959649135411049295L

		val sAllAEs: Array<ActionElement> = Array(128)
	}


	// only active items will have their update routine called
	protected var mActive = true

	init {
		sAllAEs.Append(this)
	}

	/** override in your derived class to do something exciting.  */
	open fun Update() {
	}

	/** override in your derived class to do something exciting.  */
	fun Reset() {
	}

	fun Active(pActive: Boolean) {
		mActive = pActive
	}

	fun Active(): Boolean {
		return mActive
	}
}
