// Spritey3d.java
// Spritey class extended with pseudo-3D perspective
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


package rogue_opcode

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF


/**
 * Extends the Spritey paradigm to allow for pseudo-3d perspective correction.
 * This is achieved by simply scaling the mSize and XY drift of sprites based on
 * how far from the "camera" they are along the the Z axis.
 *
 * Some important differences from a standard `Spritey` to note: this
 * class treats the center of the screen as (0,0,0), positive Y is toward the
 * top of the screen, and positive Z into the screen. Additionally, sprites are
 * drawn centered, rather than from the top-left corner.
 *
 * **Example:** Drawing a sprite at (0,0,0) will draw it centered on the
 * screen with a scaling factor of 1.0.
 *
 * @see Spritey
 * @author Brigham Toskin
 */
open class Spritey3d : Spritey {
	companion object {
		private val serialVersionUID = 7198684330563604332L

		var sCameraW = 2f
		var sCameraH = 3f

		var sNormalZ: Float
		var sFarZ: Float

		init {
			val tRatio = AnimatedView.sOnly!!.mScreenWidth.toFloat() /
				AnimatedView.sOnly!!.mScreenHeight.toFloat()
			if(AnimatedView.sOnly!!.mBaseAspectRatio < tRatio) {
				sNormalZ = AnimatedView.sOnly!!.mScreenWidth / sCameraW
			} else {
				sNormalZ = AnimatedView.sOnly!!.mScreenHeight / sCameraH
			}
			sFarZ = sNormalZ * 4
		}
	}


	protected var mDrawDest: RectF

	// c'tor ///////////////////////////////////////////////////////////////////

	/**
	 * @param pResourceID image resource ID of sprite strip graphic.
	 * *
	 * @param pFrameCount number of frames in the sprite strip.
	 */
	constructor(pResourceID: Int, pFrameCount: Int) : super(pResourceID, pFrameCount) {
		mDrawDest = RectF()
	}

	/**
	 * @param pResourceID image resource ID of sprite strip graphic.
	 * *
	 * @param pFrameCount number of frames in the sprite strip.
	 * *
	 * @param pX initial X position.
	 * *
	 * @param pY initial Y position.
	 * *
	 * @param pZ initial Z position.
	 */
	constructor(pResourceID: Int, pFrameCount: Int, pX: Int, pY: Int, pZ: Int) : super(pResourceID, pFrameCount, pX, pY) {
		mDrawDest = RectF()
		ZDepth(pZ.toFloat())
	}

	// game loop callbacks /////////////////////////////////////////////////////

	/**
	 * Draws the object at it's specified position, scaled for pseudo-3D
	 * perspective based on Z distance.
	 *
	 * @see rogue_opcode.Spritey.Draw
	 */
	override fun Draw(pCanvas: Canvas, pPreScaler: Float, pDefaultPaint: Paint) {
		// bail if not ready or far clip
		if(mPos.z > sFarZ) return

		val (tX, tY, tZ) = mPos
		val tScreenW = pCanvas.width.toFloat()
		val tScreenH = pCanvas.height.toFloat()

		val tScaleFactor = sNormalZ / tZ
		val tLeft = (tX - mFrameWidth / 2) * tScaleFactor + tScreenW / 2
		val tTop = tScreenH / 2 - (tY - mFrameHeight / 2) * tScaleFactor
		val tDrawDest = mDrawDest
		tDrawDest.left = tLeft
		tDrawDest.top = tTop
		tDrawDest.right = tLeft + mFrameWidth * tScaleFactor
		tDrawDest.bottom = tTop + mFrameHeight * tScaleFactor

		pCanvas.drawBitmap(currentGR!!.mImage!!, mFrame.toRect(), tDrawDest, pDefaultPaint)
	}
}
