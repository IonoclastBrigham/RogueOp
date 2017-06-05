// ScreenElement.kt
// Updatable active sprite class.
//
// Copyright © 2010-2017 Christopher R. Tooley
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <https://github.com/IonoclastBrigham/RogueOp/blob/master/LICENSE.md>
//
// Formatting:
// 80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode

import android.graphics.Canvas
import android.graphics.Paint
import rogue_opcode.DrawableElement.Companion.sAllSEs
import rogue_opcode.geometrics.XYZf
import rogue_opcode.geometrics.XYf


/**
 * *Generic updateable graphical object class.*
 *
 * Override `Update()` to perform custom movement and animation,
 * handle user input, update AI, etc.
 *
 * @see GraphicResource
 * @see ActionElement
 */
open class ScreenElement : ActionElement, DrawableElement {
	companion object {
		private val serialVersionUID = 8512900123394987036L
	}


	var currentGR: GraphicResource? = null
		protected set
	protected var mCurrentGRResourceID: Int = 0		//Now that GRs can come in and out of scope, mGR is more likely to be null.
	//If this SE is drawing a null mGR it will try to create a new GR based on this ID.

	override var Topmost: Boolean = false				//If true this will be drawn in the
	//mEffectsHookBitmap after all non topmost SEs have been drawn

	lateinit var mPos: XYZf
	lateinit var mVel: XYZf

	lateinit var mTextOffset: XYZf				//offset for text drawing relative to the adjusted mPos TODO - in what coordinates?
	//TODO - add coordinate scaling and accessor methods


	override var Visible: Boolean = false

	var mSelfGuided: Boolean = false
	lateinit internal var mSelfGuidedDestination: XYf
	internal var mSelfGuidedSpeed: Float = 0.toFloat()

	protected var mText: String? = null

	protected var mDrawCentered: Boolean = false
	protected var mDrawAbsolute: Boolean = false

	protected var mTextPaint: Paint? = null

	// c'tors //////////////////////////////////////////////////////////////////

	constructor(pResourceID: Int) {
		init(pResourceID, null, 0, 0)
	}

	constructor(pText: String) {
		init(0, pText, 0, 0)
	}

	constructor(pResourceID: Int, pX: Int, pY: Int) {
		init(pResourceID, null, pX, pY)
	}

	constructor(pText: String, pX: Int, pY: Int) {
		init(0, pText, pX, pY)
	}

	constructor(pResourceID: Int, pText: String, pX: Int, pY: Int) {
		init(pResourceID, pText, pX, pY)
		mDrawCentered = false
		mDrawAbsolute = false
	}

	protected fun init(pResourceID: Int, pText: String?, pX: Int, pY: Int) {
		if(pResourceID != 0) {
			currentGR = GraphicResource.FindGR(pResourceID)
		} else {
			currentGR = null
		}

		mCurrentGRResourceID = pResourceID

		mPos = XYZf(pX.toFloat(), pY.toFloat(), 100f)
		mVel = XYZf()

		mTextOffset = XYZf(0f, 0f, 0f)
		mText = pText

		mDrawCentered = true
		Visible = true
		mSelfGuided = false
		Topmost = false

		mSelfGuidedDestination = XYf()

		sAllSEs.Append(this)
	}

	//In theory we can call unload on an SE to remove it's reference from the list of all SEs
	//and the list of all AEs.  The goal is to remove all references to the SE so that the garbage
	//collector can get rid of them.
	//An example scenario could be a situation where you have a dynamic menu structure that uses SEs
	//to draw itself.  These SEs are generated (new) automatically as menu items are added.  We want
	//to be able to call RemoveMenuItem() and have the associated SE go away permanently (not just invisible).
	fun Unload() {
		//iterate over array to find and then remove
		for(i in 0..sAllSEs.LastIndex) {
			if(sAllSEs.At(i) === this) {
				sAllSEs.Remove(i)
				break
			}
		}

		// We're also an ActionElement, so remove from that list too
		for(i in 0..ActionElement.sAllAEs.LastIndex) {
			if(ActionElement.sAllAEs.At(i) === this) {
				ActionElement.sAllAEs.Remove(i)
				break
			}
		}

		//TODO - MUST ENSURE THAT THIS IS REALLY WORKING OR WE WILL "LEAK" ALL OVER THE PLACE!
	}

	// public interfaces ///////////////////////////////////////////////////////

	fun Hibernate() {
		Visible = false // Turn off drawing
		mActive = false // stop calling update function
	}

	fun Wake() {
		Visible = true
		mActive = true
	}

	fun SetCurrentGR(pResourceID: Int) {
		mCurrentGRResourceID = pResourceID
		currentGR = GraphicResource.FindGR(pResourceID)
	}

	fun WithinRange(pTargetSE: ScreenElement, pRadius: Float): Boolean? {
		val tXYf = XYf(pTargetSE.mPos)

		if(!pTargetSE.mDrawCentered) {
			tXYf.x -= (pTargetSE.Width() / 2).toFloat()
			tXYf.y -= (pTargetSE.Height() / 2).toFloat()
		}

		return WithinRange(tXYf, pRadius)
	}

	fun WithinRange(pTarget: XYf, pRadius: Float): Boolean? {
		return WithinRange(pTarget, pRadius, pRadius)
	}

	fun WithinRange(pTarget: XYf, pXRadius: Float, pYRadius: Float): Boolean? {
		if(!Visible) return false

		val tXYf = XYf(mPos)

		if(!mDrawCentered) {
			tXYf.x += (Width() / 2).toFloat()
			tXYf.y += (Height() / 2).toFloat()
		}

		return (tXYf.x < pTarget.x + pXRadius &&
				tXYf.x > pTarget.x - pXRadius &&
				tXYf.y < pTarget.y + pYRadius &&
				tXYf.y > pTarget.y - pYRadius)
	}

	fun DrawCentered(pDrawCentered: Boolean) {
		mDrawCentered = pDrawCentered
	}

	fun DrawAbsolute(pDrawAbsolute: Boolean) {
		mDrawAbsolute = pDrawAbsolute
	}

	open fun Width(): Int {
		return currentGR?.VirtualWidth() ?: 0
	}

	open fun Height(): Int {
		return currentGR?.VirtualHeight() ?: 0
	}

	fun Text() = mText

	open fun Text(pText: String) {
		mText = pText
	}

	fun Pos() = mPos

	fun Pos(pX: Float, pY: Float) {
		mPos.x = pX
		mPos.y = pY
	}

	fun ZDepth() = mPos.z

	fun ZDepth(pZ: Float) {
		mPos.z = pZ
		sAllSEs.At(0) // force mark dirty
	}

	fun moveTo(pSpeed: Float, pDestination: XYf) {
		mSelfGuided = true

		mSelfGuidedDestination.x = pDestination.x
		mSelfGuidedDestination.y = pDestination.y

		mSelfGuidedSpeed = pSpeed
	}

	//Query if a self-guided SE has reached its destination
	fun HasReachedDestination(): Boolean {
		return !mSelfGuided
	}

	fun setTextPaint(pPaint: Paint) {
		mTextPaint = pPaint
	}

	var onPreUpdate: (ScreenElement.()->Unit)? = null
	var onPostUpdate: (ScreenElement.()->Unit)? = null

	/**
	 * GameProc will call this repeatedly during the program's lifetime
	 * automatically. Override in your derived class to do something exciting.
	 *
	 * @see rogue_opcode.ActionElement.Update
	 */
	override fun Update() {
		onPreUpdate?.invoke(this)

		if(mSelfGuided) {
			val xOff = (mSelfGuidedDestination.x - mPos.x).toDouble()
			val yOff = (mSelfGuidedDestination.y - mPos.y).toDouble()
			val tDistSq = xOff * xOff + yOff * yOff

			// ease out as we reach destination
			if(tDistSq != 0.0) {
				val tDist = Math.sqrt(tDistSq)
				mVel.x = (xOff / tDist).toFloat() * mSelfGuidedSpeed
				mVel.y = (yOff / tDist).toFloat() * mSelfGuidedSpeed

				if(tDistSq <= mSelfGuidedSpeed) {
					mPos.x = mSelfGuidedDestination.x
					mPos.y = mSelfGuidedDestination.y

					mSelfGuided = false
					mVel.Set(XYZf.ZERO)
				}
			}
		}

		if(mVel != XYZf.ZERO) {
			mPos.add(mVel)
		}

		onPostUpdate?.invoke(this)
	}

	/**
	 * AnimatedView will call this interface at draw time. Override in your
	 * derived class to do something more than draw the currentGR at it's
	 * current `mPos` coordinates.
	 */
	override fun Draw(pCanvas: Canvas, pPreScaler: Float, pDefaultPaint: Paint) {
		var tX = mPos.x * pPreScaler
		var tY = mPos.y * pPreScaler
		if(currentGR != null && currentGR!!.Valid()) {
			if(mDrawCentered) {
				tX -= (currentGR!!.PhysicalWidth() / 2).toFloat()
				tY -= (currentGR!!.PhysicalHeight() / 2).toFloat()
			}
			pCanvas.drawBitmap(currentGR!!.mImage!!, tX, tY, null)
		} else {
			//We are trying to draw an empty GR - lets see if it has recently been loaded
			SetCurrentGR(mCurrentGRResourceID)
		}
		mText?.takeUnless { it.isEmpty() } ?.let { tText ->
			val tPaint = mTextPaint ?: pDefaultPaint
			pCanvas.drawText(tText, tX + mTextOffset.x, tY + mTextOffset.y, tPaint)
		}
	}

	override fun compareTo(pOther: DrawableElement): Int {
		val tOtherSE = pOther as? ScreenElement ?: return 0
		return when {
			mPos.z > tOtherSE.mPos.z -> -1
			mPos.z < tOtherSE.mPos.z -> 1
			else -> 0
		}
	}
}

