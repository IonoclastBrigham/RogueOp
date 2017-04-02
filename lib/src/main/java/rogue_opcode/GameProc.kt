// GameProc.kt
// Game main logic class
//
// Copyright © 2010-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode


// import dalvik.system.VMRuntime;
// import com.ngc.MGEPCT.BaG;

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.GestureDetector.OnGestureListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import rogue_opcode.geometrics.XYf
import rogue_opcode.soundy.SoundEffect


// import android.view.GestureDetector.OnDoubleTapListener;
// CRT - the OnDoubleTapListener code works great, but because android is
// listening for the double tap after a single
// tap you cannot tap and then flick in rapid succession as is needed in a game
// - for example if a single tap triggers a jump
// and a fling triggers a move then if double tap support is enabled you cannot
// immediately jump and then move - the move
// will be delayed by about a (critical) 1/2 second.
// I'd like to make this some kind of parameter but don't know how to do that
// with interfaces... -CRT
open class GameProc : Activity(), Runnable, OnGestureListener {
	companion object {
		const val TAG = "Rogue-Op"

		lateinit var sOnly: GameProc

		const val UPDATE_FREQ: Long = 30
		const val UPDATE_PERIOD = 1000 / UPDATE_FREQ

		private var sUpdateThread: Thread? = null
		private var sSeconds: Int = 0
		private var sFPS: Long = 0
	}

	inner class EditableTextPositionParams(pCurrentTE: TextSE, pPos: XYf, pWidth: Int, pHeight: Int) {
		var mQueuedViewLocation: XYf? = pPos
		var mWidth: Int = pWidth
		var mHeight: Int = pHeight
		var mCurrentTE: TextSE? = pCurrentTE
		var mEdit: EditText? = null
		var mRLP: RelativeLayout.LayoutParams? = null
		var mRL: RelativeLayout? = null
	}


	private lateinit var mLayout: RelativeLayout

	// stats
	private var mElapsedTime: Long = 0

	private var mMousing: Boolean = false
	private var mMousePos = XYf()

	private var mMotionDetector: GestureDetector? = null
	internal var mTouchState = TouchState()

	internal var mCurrentKey: Int = 0
	internal var mKeys = BooleanArray(525)

	private var mRunning: Boolean = false
	private var mRestarting: Boolean = false
	private var mExiting: Boolean = false

	internal var mEditTextParams: EditableTextPositionParams? = null

	// app lifecycle ///////////////////////////////////////////////////////////

	override fun onCreate(savedState: Bundle?) {
		super.onCreate(savedState)
		sOnly = this

		// set up graphics
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		window.run {
			addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
			clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
		}

		mLayout = RelativeLayout(this)
		setContentView(mLayout)
		mLayout.addView(AnimatedView(this))
		mEditTextParams = null

		// initialize stats
		mElapsedTime = 0
		sSeconds = 0
		sFPS = 0

		// user-provided init code
		mMotionDetector = GestureDetector(this, this)

		InitializeOnce()
	}

	public override fun onSaveInstanceState(outState: Bundle) {
		Log.d(TAG, "onSaveInstanceState()")
		super.onSaveInstanceState(outState)
		//			outState.putSerializable("sAllAEs", ActionElement.sAllAEs);
		//			outState.putSerializable("sAllARs", AudioResource.sAllARs);
	}

	/**
	 * Called when app becomes active.
	 */
	override fun onResume() {
		Log.d(TAG, "onResume()")
		super.onResume()

		// input mData
		mMousing = false

		// restore user settings
		val tPrefs = getPreferences(0)
		if(tPrefs.getBoolean("saved", false)) {
			// TODO: restore settings to SettingsDB object in loop
		} else {
			// TODO: load SettingsDB with default values
		}

		// user initialization code
		InitializeOnResume()

		// start the render and update threads
		sUpdateThread = Thread(this, "Rogue-Op Update Thread").apply { start() }
		AnimatedView.StartRenderThread()
		AnimatedView.sOnly.requestFocus()

		// clean some up now to avoid latency later
		Runtime.getRuntime().gc()
	}

	/** Called when app is backgrounded, but may still be visible.  */
	override fun onPause() {
		Log.d(TAG, "onPause()")
		super.onPause()

		Shutdown()

		// save user settings
		val tEditor = getPreferences(0).edit()
		// TODO: save user settings in a loop from a static SettingsDB;
		// each entry is { "name", setting_type, setting_data }.
		tEditor.putBoolean("saved", true)
		tEditor.commit()

		// shut down
		Die()
		AnimatedView.sOnly.Die()
		AudioResource.Die() // stop and free all audio resources
		SoundEffect.Die() // free the sound pool

		// clean some up now to avoid latency later
		val r = Runtime.getRuntime()
		r.gc()
	}

	//	/** Called on app shutdown. */
	//@Override
	/*
	 * protected void onDestroy()
	 * {
	 * Log.d(TAG, "onDestroy()");
	 * super.onDestroy();
	 *
	 * Shutdown();
	 * }
	 */

	/** Stops the update thread.  */
	fun Die() {
		synchronized(this) {
			mRunning = false
			sUpdateThread = null
		}
	}

	/** Override this in your derived class to make stuff.  */
	fun InitializeOnce() {
	}

	/** Override this to hook the resume event.  */
	fun InitializeOnResume() {
	}

	/** Override this to hook the shutdown event.  */
	fun Shutdown() {
	}

	// game update loop ////////////////////////////////////////////////////////

	/** Thread to run logic updates  */
	override fun run() {
		Log.d(TAG, "Entering update thread")
		mRunning = true
		while(mRunning) {
			val start = SystemClock.uptimeMillis()

			// calc stats
			if(mElapsedTime >= 999) {
				synchronized(this) {
					sSeconds++
					mElapsedTime -= 1000
					sFPS = AnimatedView.sOnly.FPS().toLong()
				}
			}

			Update()

			val end = SystemClock.uptimeMillis()
			val tLastUpdate = end - start
			mElapsedTime += UPDATE_PERIOD
			try {
				var tSleep = UPDATE_PERIOD - tLastUpdate
				if(tSleep < 1)
					tSleep = 1
				Thread.sleep(tSleep)
			} catch(e: InterruptedException) {
			}

		}
		Log.d(TAG, "Exiting update thread")
	}

	// public interfaces //

	/** Calls update on all extant ActionElements.  */
	fun Update() {
		for(i in 0..ActionElement.sAllAEs.LastIndex) {
			val tAE = ActionElement.sAllAEs.At(i)
			if(tAE.Active()) tAE.Update()
		}

		mTouchState.Clear(true)
	}

	//AddView allows the user to pass a view (typically a layout that was inflated from XML) to be added above the normal AnimatedView Surface.
	//We need to do this from within the original UI thread, which this function facilitates.
	fun ShowTextEditor(pCurrentTE: TextSE, pPos: XYf,
	                   pWidth: Int, pHeight: Int) {
		if(Looper.myLooper() !== Looper.getMainLooper()) {
			mLayout.post { ShowTextEditor(pCurrentTE, pPos, pWidth, pHeight) }
			return
		}

		val tParams = mEditTextParams ?: init_edit_params(pCurrentTE, pPos,
		                                                  pWidth, pHeight)
		tParams.run {
			mQueuedViewLocation = pPos
			mWidth = pWidth
			mHeight = pHeight
			mCurrentTE = pCurrentTE
		}

			mLayout.postDelayed({
                tParams.run {
	                mRLP?.setMargins(
		                (mQueuedViewLocation!!.x * AnimatedView.sOnly.mPreScaler).toInt(),
		                (mQueuedViewLocation!!.y * AnimatedView.sOnly.mPreScaler).toInt(),
		                0, 0)
	                mRL?.layoutParams = mRLP

	                mEdit?.run {
		                visibility = View.VISIBLE
		                width = (mWidth * AnimatedView.sOnly.mPreScaler).toInt()
		                transformationMethod = android.text.method.SingleLineTransformationMethod()
		                maxLines = 1
		                setBackgroundColor(Color.BLUE)

		                mCurrentTE?.Text()?.takeIf(String::isNotEmpty)?.let {
			                setText(it)
			                setSelection(it.length)
		                } ?: setText("")

		                requestFocus()
	                }
                }
            }, 100)
	}

	private fun init_edit_params(pCurrentTE: TextSE, pPos: XYf, pWidth: Int,
	                             pHeight: Int)
		= EditableTextPositionParams(pCurrentTE, pPos, pWidth, pHeight).apply {
			mQueuedViewLocation = pPos
			mRLP = RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT)
			mRLP!!.setMargins(
				mQueuedViewLocation!!.x.toInt(),
				mQueuedViewLocation!!.y.toInt(), 0, 0)
			mRL = RelativeLayout(GameProc.sOnly)

			mRL!!.layoutParams = mRLP

			mEdit = EditText(GameProc.sOnly)

			mRL!!.addView(mEdit)

			mLayout.addView(mRL)

			mEdit!!.requestFocus()

			mEditTextParams = this
		}

	fun HideTextEditor(pCurrentTE: TextSE) {
		val tParams = mEditTextParams?.takeIf { pCurrentTE === it.mCurrentTE } ?: return
		try {
			mLayout.post {
				tParams.mEdit?.visibility = View.INVISIBLE
				try {
					val tIMM = getSystemService(Context.INPUT_METHOD_SERVICE)
						as InputMethodManager
					tIMM.hideSoftInputFromWindow(tParams.mEdit?.windowToken, 0)
				} catch(e: Exception) { /* nothing */ }
			}
		} catch(e: Exception) { /* nothing */ }
	}

	// runtime stats ///////////////////////////////////////////////////////////

	/** Query performance statistics based on update timing.  */
	// TODO: does this belong here? Move to AnimatedView
	internal fun FPS(): Long {
		synchronized(this) {
			return sFPS
		}
	}

	/** Query uptime statistics for update thread.  */
	internal fun Seconds(): Long {
		synchronized(this) {
			return sSeconds.toLong()
		}
	}

	// user input callbacks ////////////////////////////////////////////////////

	override fun onTrackballEvent(pEvent: MotionEvent): Boolean {
		mMousePos.x = (pEvent.x * 100).toInt().toFloat()
		mMousePos.y = (pEvent.y * 100).toInt().toFloat()

		mMousing = true
		return true
	}

	fun Mousing(): Boolean {
		return mMousing
	}

	fun MousePos(): XYf {
		mMousing = false
		return mMousePos
	}


	override fun onTouchEvent(pMotionEvent: MotionEvent): Boolean {
		this.mMotionDetector!!.onTouchEvent(pMotionEvent)
		return super.onTouchEvent(pMotionEvent)
	}

	override fun onDown(e: MotionEvent): Boolean {
		return false
	}

	override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float,
						 velocityY: Float): Boolean {
		mTouchState.SetState(TouchState.FLING, e1, e2)
		return false
	}

	override fun onLongPress(e: MotionEvent) {
		mTouchState.SetState(TouchState.LONG_TOUCH, e, null)
	}

	override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float,
						  distanceY: Float): Boolean {
		AnimatedView.sOnly.mDebugString1 = distanceY.toString() + ""

		//if(Math.abs(distanceY) > 1.5f)
		mTouchState.SetState(TouchState.SCROLL, e1, e2, distanceY, distanceX)
		return false
	}

	override fun onShowPress(e: MotionEvent) {}

	override fun onSingleTapUp(e: MotionEvent): Boolean {
		mTouchState.SetState(TouchState.SINGLE_TAP, e, null)
		return false
	}

	//See notes above about the double tap limitations - OnDoubleTapListener
	/*
	 * @Override
	 * public boolean onDoubleTap(MotionEvent e)
	 * {
	 * mTouchState.SetState(TouchState.DOUBLE_TAP, e, null);
	 * return false;
	 * }
	 *
	 * @Override
	 * public boolean onDoubleTapEvent(MotionEvent e)
	 * {
	 * //Toast.makeText(GameProc.sOnly, "double tap event", 0).show();
	 * return false;
	 * }
	 *
	 * @Override
	 * public boolean onSingleTapConfirmed(MotionEvent e)
	 * {
	 * //mTouchState.SetState(TouchState.SINGLE_TAP, e, null);
	 * return false;
	 * }
	 */
}