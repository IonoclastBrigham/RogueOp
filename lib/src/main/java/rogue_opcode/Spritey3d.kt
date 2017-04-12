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


import rogue_opcode.geometrics.XYZf
import android.graphics.Canvas
import android.graphics.RectF


/**
 * Extends the Spritey paradigm to allow for pseudo-3d perspective correction.
 * This is achieved by simply scaling the mSize and XY drift of sprites based on
 * how far from the "camera" they are along the the Z axis.
 * <br></br><br></br>
 * Some important differences from a standard `Spritey` to note: this
 * class treats the center of the screen as (0,0,0), positive Y is toward the
 * top of the screen, and positive Z into the screen. Additionally, sprites are
 * drawn centered, rather than from the top-left corner.
 * <br></br><br></br>
 * **Example:** Drawing a sprite at (0,0,0) will draw it centered on the
 * screen with a scaling factor of 1.0.

 * @see Spritey

 * @author Brigham Toskin
 */
class Spritey3d : Spritey {

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

	 * @see rogue_opcode.Spritey.Draw
	 */
	override fun Draw() {
		val tCanvas = AnimatedView.sCurrentCanvas
		if(tCanvas == null || mPos.z > sFarZ)
		// not ready or far clip
			return

		val tPos = mPos
		val tScreenW = AnimatedView.sOnly.ScreenWidth().toFloat()
		val tScreenH = AnimatedView.sOnly.ScreenHeight().toFloat()

		val tScaleFactor = sNormalZ / tPos.z
		val tLeft = (tPos.x - mFrameWidth / 2) * tScaleFactor + tScreenW / 2
		val tTop = tScreenH / 2 - (tPos.y - mFrameHeight / 2) * tScaleFactor
		val tDrawDest = mDrawDest
		tDrawDest.left = tLeft
		tDrawDest.top = tTop
		tDrawDest.right = tLeft + mFrameWidth * tScaleFactor
		tDrawDest.bottom = tTop + mFrameHeight * tScaleFactor
		tCanvas.drawBitmap(currentGR!!.mImage!!, mFrame.toRect(), tDrawDest, null)
	}

	companion object {
		private val serialVersionUID = 7198684330563604332L

		var sCameraW: Float = 0.toFloat()
		var sCameraH: Float = 0.toFloat()
		var sNormalZ: Float = 0.toFloat()
		var sFarZ: Float = 0.toFloat()

		init {
			sCameraW = 2f
			sCameraH = 3f
			val tRatio = AnimatedView.sOnly.mScreenWidth.toFloat() / AnimatedView.sOnly.mScreenHeight.toFloat()
			if(AnimatedView.sOnly.mBaseAspectRatio < tRatio)
				sNormalZ = AnimatedView.sOnly.mScreenWidth / sCameraW
			else
				sNormalZ = AnimatedView.sOnly.mScreenHeight / sCameraH
			sFarZ = sNormalZ * 4
		}
	}
}
