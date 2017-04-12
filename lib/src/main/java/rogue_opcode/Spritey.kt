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

import android.graphics.RectF
import rogue_opcode.geometrics.Rectangle
import rogue_opcode.geometrics.Rectanglef


/**
 * Extends `ScreenElement` with sprite strip animation, and collision.

 * @see ScreenElement

 * @author Brigham Toskin
 */
open class Spritey : ScreenElement {

	protected var mFrame = Rectangle()
	protected var mFrameWidth: Int = 0
	protected var mFrameHeight: Int = 0
	protected var mFrameCount: Int = 0
	var msecsPerFrame = 33	// ~30 FPS
	var timeSinceLastFrame = 0

	var boundingBox = Rectanglef()
	var bbOffset = 0

	// c'tor ///////////////////////////////////////////////////////////////////

	/**
	 * Constructs a Spritey to load the specified resource.

	 * @param pResourceID resource to load.
	 * *
	 * @param pFrameCount number of frames in the sprite strip.
	 */
	constructor(pResourceID: Int, pFrameCount: Int) : super(pResourceID) {
		init(pFrameCount)
	}

	/**
	 * Constructs a Spritey to load the specified resource at specified logical
	 * screen coordinates.

	 * @param pResourceID resource to load.
	 * *
	 * @param pFrameCount number of frames in the sprite strip.
	 * *
	 * @param pX horizontal position of new Spritey.
	 * *
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

	override fun Draw() {
		val tCanvas = AnimatedView.sCurrentCanvas
		if(tCanvas != null) {
			val tPos = mPos
			val tDest = RectF(tPos.x, tPos.y, tPos.x + mFrameWidth,
					tPos.y + currentGR!!.PhysicalHeight())
			tCanvas.drawBitmap(currentGR!!.mImage!!, mFrame.toRect(), tDest, null)
		}
	}

	protected fun resetFrame() {
		mFrame.X(0)
	}

	/**
	 * Checks for collision between two `Spritey`s.

	 * @param pSpritey `Spritey` to check against
	 * *
	 * @return `true` on collision, `false` otherwise
	 */
	fun Collide(pSpritey: Spritey): Boolean {
		return Collide(pSpritey.boundingBox)
	}

	/**
	 * Checks for collision with an arbitrary [Rectangle].

	 * @param pBBox bounding box of `Spritey` to check against
	 * *
	 * @return `true` on collision, `false` otherwise
	 */
	fun Collide(pBBox: Rectanglef): Boolean {
		return boundingBox.toRectF().intersect(pBBox.toRectF())
	}

	// Spritey properties //////////////////////////////////////////////////////

	// Translates between frames per second and delay between frames.
	fun FrameRate(): Int {
		return 1000 / msecsPerFrame
	}

	fun FrameRate(value: Int) {
		msecsPerFrame = 1000 / value
	}

	// Gets or sets the collision bounding box offset
	fun Offset(): Int {
		return bbOffset
	}

	fun Offset(value: Int) {
		bbOffset = value
		boundingBox.X((mPos.x.toInt() + bbOffset).toFloat())
		boundingBox.Y((mPos.y.toInt() + bbOffset).toFloat())
		boundingBox.W((mFrameWidth - bbOffset * 2).toFloat()) // shrink in from both sides
		boundingBox.H((mFrameHeight - bbOffset * 2).toFloat())
	}


	/**
	 * Gets the current bounding box.

	 * @return the current bounding box.
	 */
	fun BoundingBox(): Rectanglef {
		return boundingBox
	}

	fun BoundingBox(box: Rectanglef) {
		boundingBox = box
	}

	companion object {
		private val serialVersionUID = -5463549117882033160L
	}
}
