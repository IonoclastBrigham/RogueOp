// Spritey.kt
// Sprite class with sheet/frame animation and collision
//
// Copyright © 2009-2017 Brigham Toskin, Leea Harlan
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
import rogue_opcode.geometrics.Rectangle
import rogue_opcode.geometrics.Rectanglef


/**
 * Extends `ScreenElement` with sprite strip animation, and collision.

 * @see ScreenElement

 * @author Brigham Toskin
 */
open class Spritey : ScreenElement {
	companion object {
		private val serialVersionUID = -5463549117882033160L
	}


	protected var mFrame = Rectangle()
	protected var mFrameWidth: Int = 0
	protected var mFrameHeight: Int = 0
	protected var mFrameCount: Int = 0
	protected var msecsPerFrame = 33	// ~30 FPS
	protected var timeSinceLastFrame = 0

	var BoundingBox = Rectanglef()

	// Translates between frames per second and delay between frames.
	var FrameRate
		get() = 1000 / msecsPerFrame
		set(value) { msecsPerFrame = 1000 / value }

	// Gets or sets the collision bounding box offset
	var Offset = 0
		set(value) {
			field = value
			BoundingBox.X((mPos.x.toInt() + field).toFloat())
			BoundingBox.Y((mPos.y.toInt() + field).toFloat())
			BoundingBox.W((mFrameWidth - field * 2).toFloat()) // shrink in from both sides
			BoundingBox.H((mFrameHeight - field * 2).toFloat())
		}

	// c'tor ///////////////////////////////////////////////////////////////////

	/**
	 * Constructs a Spritey to load the specified resource.
	 *
	 * @param pResourceID framestrip resource to load.
	 * @param pFrameCount number of frames in the sprite strip.
	 */
	constructor(pResourceID: Int, pFrameCount: Int) : super(pResourceID) {
		init(pFrameCount)
	}

	/**
	 * Constructs a Spritey to load the specified resource at specified logical
	 * screen coordinates.
	 *
	 * @param pResourceID framestrip resource to load.
	 * @param pFrameCount number of frames in the sprite strip.
	 * @param pX horizontal position of new Spritey.
	 * @param pY vertical position of new Spritey.
	 */
	constructor(pResourceID: Int, pFrameCount: Int, pX: Int, pY: Int) : super(pResourceID, pX, pY) {
		init(pFrameCount)
	}

	protected fun init(pFrameCount: Int) {
		Hibernate()
		mFrameCount = pFrameCount
		mFrameWidth = currentGR!!.PhysicalWidth() / pFrameCount
		mFrameHeight = currentGR!!.PhysicalHeight()
		mFrame = Rectangle(0, 0, mFrameWidth, mFrameHeight)
	}

	// game loop callbacks /////////////////////////////////////////////////////

	/**
	 * Handles frame animation and velocity-based displacement updates.
	 *
	 * @see rogue_opcode.ScreenElement.Update
	 */
	override fun Update() {
		val tFrame = mFrame
		timeSinceLastFrame += GameProc.UPDATE_PERIOD.toInt()
		while(timeSinceLastFrame > msecsPerFrame) {
			timeSinceLastFrame -= msecsPerFrame
			tFrame.X(mFrameWidth)
			if(tFrame.X() >= currentGR!!.PhysicalWidth())
			// right side is off the end; reset
				resetFrame()
		}
		super.Update()
	}

	override fun Draw(pCanvas: Canvas, pPreScaler: Float, pDefaultPaint: Paint) {
		val (tX, tY) = mPos
		val tDest = RectF(tX, tY, tX + mFrameWidth, tY + currentGR!!.PhysicalHeight())
		pCanvas.drawBitmap(currentGR!!.mImage!!, mFrame.toRect(), tDest, pDefaultPaint)
	}

	protected fun resetFrame() {
		mFrame.X(0)
	}

	/**
	 * Checks for collision between two `Spritey`s.
	 *
	 * @param pSpritey `Spritey` to check against
	 * @return `true` on collision, `false` otherwise
	 */
	fun Collide(pSpritey: Spritey): Boolean {
		return Collide(pSpritey.BoundingBox)
	}

	/**
	 * Checks for collision with an arbitrary [Rectangle].
	 *
	 * @param pBBox bounding box of `Spritey` to check against
	 * @return `true` on collision, `false` otherwise
	 */
	fun Collide(pBBox: Rectanglef): Boolean {
		return BoundingBox.toRectF().intersect(pBBox.toRectF())
	}
}
