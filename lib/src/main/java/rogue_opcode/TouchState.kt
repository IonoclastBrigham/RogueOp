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
object TouchState {
	const val SINGLE_TAP = 1
	const val DOUBLE_TAP = 2 // these have been removed for performance reasons
	const val FLING = 4
	const val SCROLL = 8 // Think "drag"
	const val LONG_TOUCH = 16


	private var mState: Int = 0

	private var mMainMotionEvent: MotionEvent? = null
	private var mSecondaryMotionEvent: MotionEvent? = null
	private var mYScrollDist: Float = 0f //holds either the velocity of the fling or the distance of the scroll
	private var mXScrollDist: Float = 0f //holds either the velocity of the fling or the distance of the scroll

	@Synchronized
	fun SetState(pState: Int, pMainMotionEvent: MotionEvent,
	             pSecondaryMotionEvent: MotionEvent, pYScrollDist: Float,
	             pXScrollDist: Float) {
		SetState(pState, pMainMotionEvent, pSecondaryMotionEvent)
		mXScrollDist = pXScrollDist
		mYScrollDist = pYScrollDist
	}

	@Synchronized
	fun SetState(pState: Int, pMainMotionEvent: MotionEvent,
	             pSecondaryMotionEvent: MotionEvent?) {
		synchronized(this) {
			mState = mState or pState
			mMainMotionEvent = pMainMotionEvent
			mSecondaryMotionEvent = pSecondaryMotionEvent
		}
	}

	@Synchronized
	fun Clear() {
		mState = 0
		mMainMotionEvent = null
		mSecondaryMotionEvent = null
		mYScrollDist = 0f
		mXScrollDist = 0f
	}

	@Synchronized
	fun Clear(pInternalCall: Boolean) {
		mState = 0
	}

	@Synchronized
	fun TouchPos(): XYf {
		return mSecondaryMotionEvent?.let { tEvent ->
			XYf(tEvent.x, tEvent.y)
		} ?: XYf.ZERO
	}

	@Synchronized
	fun SecondaryTouchPos(): XYf {
		return mSecondaryMotionEvent?.let { tEvent ->
			XYf(tEvent.x / AnimatedView.sOnly!!.mPreScaler,
			    tEvent.y / AnimatedView.sOnly!!.mPreScaler)
		} ?: XYf.ZERO
	}

	@Synchronized
	fun GetXScrollDist(): Float {
		return mXScrollDist
	}

	@Synchronized
	fun GetYScrollDist(): Float {
		return mYScrollDist
	}

	@Synchronized
	fun GetMainX(): Float {
		return mMainMotionEvent?.let { tEvent ->
			tEvent.x / AnimatedView.sOnly!!.mPreScaler
		} ?: 0f
	}

	@Synchronized
	fun GetMainY(): Float {
		return mMainMotionEvent?.let { tEvent ->
			tEvent.y / AnimatedView.sOnly!!.mPreScaler
		} ?: 0f
	}

	@Synchronized
	fun MainTouchPos() = XYf(GetMainX(), GetMainY())

	@Synchronized
	fun GetSecondaryX(): Float {
		return mSecondaryMotionEvent?.let { tEvent ->
			 mSecondaryMotionEvent!!.x / AnimatedView.sOnly!!.mPreScaler
		} ?: 0f
	}

	@Synchronized
	fun GetSecondaryY(): Float {
		return mSecondaryMotionEvent?.let { tEvent ->
			mSecondaryMotionEvent!!.y / AnimatedView.sOnly!!.mPreScaler
		} ?: 0f
	}

	@Synchronized
	fun Is(pState: Int) = (mState and pState) != 0
}