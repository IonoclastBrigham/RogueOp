package rogue_opcode

import android.graphics.Canvas
import android.graphics.Paint
import rogue_opcode.containers.LazySortedArray


/**
 * @author btoskin &lt;brigham@ionoclast.com&gt; Ionoclast Laboratories, LLC.
 */
interface DrawableElement : Comparable<DrawableElement> {
	companion object {
		// sorted on Z depth
		// XXX: may be inefficient; keep an eye on performance
		val sAllSEs =  LazySortedArray<DrawableElement> { a, b ->
			a.compareTo(b)
		}
		var sActiveSECount = 0
	}


	var Visible: Boolean
	var Topmost: Boolean

	fun Draw(pCanvas: Canvas, pPreScaler: Float, pPain: Paint)
}