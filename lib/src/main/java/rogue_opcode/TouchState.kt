package rogue_opcode

import android.view.MotionEvent
import rogue_opcode.geometrics.XYf

/**
 * *TouchState is an inner class that holds state information about touch
 * events.*
 *
 * It is designed to be polled instead of event-driven.
 *
 * @author Christopher R. Tooley
 */
class TouchState {
	companion object {
		val SINGLE_TAP = 1
		val DOUBLE_TAP = 2 // these have been removed for performance reasons
		val FLING = 4
		val SCROLL = 8 // Think "drag"
		val LONG_TOUCH = 16
	}

	private var mState: Int = 0

	private var mMainMotionEvent: MotionEvent? = null
	private var mSecondaryMotionEvent: MotionEvent? = null
	private var mYScrollDist: Float = 0f //holds either the velocity of the fling or the distance of the scroll
	private var mXScrollDist: Float = 0f //holds either the velocity of the fling or the distance of the scroll

	init { Clear() }

	fun SetState(pState: Int, pMainMotionEvent: MotionEvent,
	             pSecondaryMotionEvent: MotionEvent, pYScrollDist: Float,
	             pXScrollDist: Float) {
		SetState(pState, pMainMotionEvent, pSecondaryMotionEvent)
		mXScrollDist = pXScrollDist
		mYScrollDist = pYScrollDist
	}

	fun SetState(pState: Int, pMainMotionEvent: MotionEvent,
	             pSecondaryMotionEvent: MotionEvent?) {
		synchronized(this) {
			mState = mState or pState
			mMainMotionEvent = pMainMotionEvent
			mSecondaryMotionEvent = pSecondaryMotionEvent
		}
	}

	fun Clear() {
		synchronized(this) {
			mState = 0
		}
	}

	fun Clear(pInternalCall: Boolean) {
		synchronized(this) {
			mState = 0
		}
	}

	fun TouchPos(): XYf {
		if(mMainMotionEvent == null)
			return XYf(0f, 0f)

		return XYf(mMainMotionEvent!!.x, mMainMotionEvent!!.y)
	}

	fun SecondaryTouchPos(): XYf {
		if(mSecondaryMotionEvent == null)
			return XYf(0f, 0f)

		return XYf(mSecondaryMotionEvent!!.x / AnimatedView.sOnly.mPreScaler,
		           mSecondaryMotionEvent!!.y / AnimatedView.sOnly.mPreScaler)
	}

	fun GetXScrollDist(): Float {
		return mXScrollDist
	}

	fun GetYScrollDist(): Float {
		return mYScrollDist
	}

	fun GetMainX(): Float {
		if(mMainMotionEvent == null) return 0f

		var tX = 0f
		synchronized(this) {
			tX = mMainMotionEvent!!.x / AnimatedView.sOnly.mPreScaler
		}
		return tX
	}

	fun GetMainY(): Float {
		if(mMainMotionEvent == null) return 0f

		var tY = 0f
		synchronized(this) {
			tY = mMainMotionEvent!!.y / AnimatedView.sOnly.mPreScaler
		}
		return tY
	}

	fun MainTouchPos() = XYf(GetMainX(), GetMainY())

	fun GetSecondaryX(): Float {
		if(mSecondaryMotionEvent == null)
			return 0f

		var tX = 0f
		synchronized(this) {
			tX = mSecondaryMotionEvent!!.x / AnimatedView.sOnly.mPreScaler
		}
		return tX
	}

	fun GetSecondaryY(): Float {
		if(mSecondaryMotionEvent == null)
			return 0f

		var tY = 0f
		synchronized(this) {
			tY = mSecondaryMotionEvent!!.y / AnimatedView.sOnly.mPreScaler
		}
		return tY
	}

	fun Is(pState: Int) = (mState and pState) != 0
}