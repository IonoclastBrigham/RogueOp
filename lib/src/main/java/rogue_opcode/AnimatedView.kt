// AnimatedView.kt
// AnimatedView class, Android version.
//
// Copyright © 2010-2017 Christopher R Tooley, Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode

import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import rogue_opcode.ScreenElement.Companion.sAllSEs
import rogue_opcode.geometrics.XYf


open class AnimatedView(pContext: Context)
	: SurfaceView(pContext), SurfaceHolder.Callback, Runnable {
	companion object {
		// self-refs
		lateinit var sOnly: AnimatedView
		var sRenderThread: Thread? = null
		var sCurrentCanvas: Canvas? = null

		internal var mScrollSpeed = XYf(2f, 2f)
		//TODO - standardize on when to use static member vars - should mDebugString1 be accessed via sOnly or directly as a static?
		//either is fine but we should do it one way or another - or am I missing something here?
		private var sFramesDrawn: Int = 0
		private var sDebug: Boolean = false

		fun StartRenderThread() {
			sRenderThread = Thread(AnimatedView.sOnly, "Rogue-Op Render Thread")
				.apply(Thread::start)
		}
	}


	// physical screen stuff
	protected var mHolder: SurfaceHolder
	protected var mHasSurface = false
	var mScreenWidth: Int = 0
	var mScreenHeight: Int = 0
	protected var mSized = false

	// Base refers to desired dimensions and aspect ratio of the ideal machine,
	// used for reference when scaling virtual dimensions to physical dimensions
	protected var mLogicalWidth: Int = 0
	protected var mLogicalHeight: Int = 0
	var mBaseAspectRatio: Float = 0.toFloat()
	var mPreScaler = 1.0f

	// scrolling properties
	// TODO: this gets too jerky—need something that jumps as the player gets
	// too close to being offscreen. Maybe scale based on distance from center?
	protected var mBaseHScroll = 0f
	internal var mKeepCenteredSE: ScreenElement? = null

	// debug stats
	var mPaint: Paint
	var mMaskPaint: Paint			//Paint used with Porter-Duff Effects-hook drawing
	var mTransparentPaint: Paint		//Paint used to clear out alpha channel pixels when using mMaskPaint
	var mDebugString1: String
	var mDebugString2: String

	internal var mRunning: Boolean = false

	internal var mEffectsHookBitmap: Bitmap? = null
	internal var mEffectsHookCanvas: Canvas? = null
	internal var mEffectsEnabled: Boolean = false

	init {
		sOnly = this
		this.id = 0xdeadbeef.toInt() // set ID, so OS will manage restoring state

		// init debug stats
		mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
		mPaint.color = Color.BLUE
		sFramesDrawn = 0
		sDebug = false

		mDebugString1 = String()
		mDebugString2 = String()

		mMaskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
		mMaskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

		mTransparentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
		mTransparentPaint.color = Color.TRANSPARENT

		// init screen/rendering
		mHolder = holder
		mHolder.addCallback(this)
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL)
		val tSurface = mHolder.surface
		//		tSurface.setAlpha(100);

		//((Activity)pContext).setContentView(this);


		val tDM = DisplayMetrics()
		GameProc.sOnly.windowManager.defaultDisplay.getMetrics(tDM)
		mScreenWidth = tDM.widthPixels
		mScreenHeight = tDM.heightPixels

		isFocusable = true
		isFocusableInTouchMode = true

		mEffectsHookBitmap = null
		mEffectsHookCanvas = null
		mEffectsEnabled = false
	}

	override fun onKeyDown(pKeyCode: Int, pEvent: KeyEvent): Boolean {
		GameProc.sOnly.mCurrentKey = pKeyCode
		GameProc.sOnly.mKeys[pKeyCode] = true

		if(pKeyCode == KeyEvent.KEYCODE_BACK)
			return false

		return true
	}

	override fun onKeyUp(pKeyCode: Int, pEvent: KeyEvent): Boolean {
		GameProc.sOnly.mKeys[pKeyCode] = false

		return true
	}

	// screen and drawing properties ///////////////////////////////////////////

	//The EffectsHook allows us to manipulate the current main bitmap/canvas before it is brought to the screen
	//It can be used to draw glow effects and use Porter-Duff transfer modes for advanced masking/blend effects.
	fun EnableEffectsHook() {
		if(mEffectsHookBitmap == null) {
			mEffectsHookBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888)
			mEffectsHookCanvas = Canvas(mEffectsHookBitmap!!)
		}
		mEffectsEnabled = true
	}

	fun DisableEffectsHook() {
		mEffectsEnabled = false
	}

	/**
	 * Set up a display with specified virtual resolution and aspect ratio. This
	 * method is used to tell `AnimatedView` the logical screen mSize and
	 * aspect ratio we are programming to, allowing it to pre-scale all graphics
	 * and draw positions appropriately.

	 * @param pWidth virtual width of display area.
	 * *
	 * @param pHeight virtual height of display area.
	 */
	fun NormailzeResolution(pWidth: Int, pHeight: Int) {
		mLogicalWidth = pWidth
		mLogicalHeight = pHeight

		mBaseAspectRatio = pWidth.toFloat() / pHeight.toFloat()
		val tScreenAspectRatio = mScreenWidth.toFloat() / mScreenHeight.toFloat()
		if(mBaseAspectRatio < tScreenAspectRatio)
			mPreScaler = mScreenHeight.toFloat() / mLogicalHeight.toFloat()
		else
			mPreScaler = mScreenWidth.toFloat() / mLogicalWidth.toFloat()
	}

	/**
	 * The "camera" will scroll around to follow this SE and keep it more or
	 * less centered on the screen.

	 * @param pKeepCenteredSE ScreenElement to follow.
	 */
	fun SetKeepCenteredSE(pKeepCenteredSE: ScreenElement) {
		mKeepCenteredSE = pKeepCenteredSE
	}

	fun LogicalWidth(): Int {
		return mLogicalWidth
	}

	fun LogicalHeight(): Int {
		return mLogicalHeight
	}

	fun ScreenWidth(): Int {
		synchronized(this) {
			return mScreenWidth
		}
	}

	fun ScreenHeight(): Int {
		synchronized(this) {
			return mScreenHeight
		}
	}

	fun FPS(): Int {
		synchronized(this) {
			val tFrames = sFramesDrawn
			sFramesDrawn = 0
			return tFrames
		}
	}

	fun Debug() = synchronized(this) { sDebug }

	fun Debug(pDebug: Boolean) {
		synchronized(this) {
			sDebug = pDebug
		}
	}

	// render thread ///////////////////////////////////////////////////////////

	fun Die() {
		synchronized(this) {
			mRunning = false
			sRenderThread = null
		}
	}

	/** render thread loop  */
	override fun run() {
		Log.d(GameProc.TAG, "Entering render thread")
		mRunning = true
		while(synchronized(this) { mRunning }) {
			while(synchronized(this) { !mHasSurface }) {
				try {
					Thread.sleep(1) // wait for a valid surface
				} catch(e: InterruptedException) { }

			}
			sFramesDrawn++
			draw()
		}
		Log.d(GameProc.TAG, "Exiting render thread")
	}

	/** draws all ScreenElements to the screen  */
	protected fun draw() {
		val tCanvas = mHolder.lockCanvas() ?: return

		ScreenElement.sActiveSECount = 0			//performance monitor variable keeps track of visible count of SEs
		//This is important because now we can unload graphics but unloading them
		//does not stop the draw proc.

		// Draw the "keep-centered" graphic
		// (You are not expected to understand this—I don't!)
		if(mKeepCenteredSE != null) {
			if(mKeepCenteredSE!!.mPos.x - mLogicalWidth / 6 + mBaseHScroll < mLogicalWidth / 2)
				mBaseHScroll += mScrollSpeed.x * mPreScaler
			if(mKeepCenteredSE!!.mPos.x + mLogicalWidth / 6 + mBaseHScroll > mLogicalWidth / 2)
				mBaseHScroll -= mScrollSpeed.x * mPreScaler
			if(mKeepCenteredSE!!.mPos.x - mLogicalWidth / 6 + mBaseHScroll < mLogicalWidth / 2)
				mBaseHScroll += mScrollSpeed.x * mPreScaler
			if(mKeepCenteredSE!!.mPos.x + mLogicalWidth / 6 + mBaseHScroll > mLogicalWidth / 2)
				mBaseHScroll -= mScrollSpeed.x * mPreScaler

			// TODO - need to limit how much we allow mBaseHScroll to go - when
			// we get to the edge of a background graphic we want to pin the
			// graphic to the edge of the screen and move the keep-centered SE
			// to the edge. Problem is that the mSize of the background graphic
			// we want to contain may not be the same as mBaseWidth and height
		}

		// draw world
		if(mEffectsEnabled) {
			sCurrentCanvas = mEffectsHookCanvas
			mEffectsHookBitmap!!.eraseColor(Color.TRANSPARENT)
		} else {
			sCurrentCanvas = tCanvas
		}

		tCanvas.drawRGB(0, 0, 0) // TODO: parameterize whether to do this
//		ScreenElement.sAllSEs.mDirty = true
		ScreenElement.sAllSEs.SortIfDirty()
		loopAndDrawSEs(false)

		if(mEffectsEnabled) {
			sCurrentCanvas = tCanvas

			tCanvas.drawBitmap(mEffectsHookBitmap!!, 0f, 0f, null)

			loopAndDrawSEs(true)

			tCanvas.drawBitmap(mEffectsHookBitmap!!, 0f, 0f, mMaskPaint)
		}

		// draw stats
		if(sDebug) {
			sCurrentCanvas?.run {
				drawText("Time: " + GameProc.sOnly.Seconds(), 10f, 10f, mPaint)
				drawText("FPS:  " + GameProc.sOnly.FPS(), 10f, 22f, mPaint)
				drawText("#1:  " + mDebugString1, 10f, 34f, mPaint)
				drawText("#2:  " + mDebugString2, 10f, 46f, mPaint)
				drawText(GameProc.sOnly.mTouchState.MainTouchPos().toString(),
				         10f, 58f, mPaint)
			}
		}

		sCurrentCanvas = null
		mHolder.unlockCanvasAndPost(tCanvas)
	}

	private fun loopAndDrawSEs(pTopmost: Boolean) {
		for(tSE in sAllSEs) {
			if(tSE.Visible() && pTopmost == tSE.Topmost()) {
				ScreenElement.sActiveSECount++
				tSE.Draw()
			}
		}
	}

	// screen update callbacks /////////////////////////////////////////////////

	override fun surfaceChanged(sh: SurfaceHolder, fmt: Int, w: Int, h: Int) {
		Log.d(GameProc.TAG, "surfaceChanged()")
		synchronized(this) {
			mSized = true
			mScreenWidth = w
			mScreenHeight = h
		}
	}

	override fun surfaceCreated(holder: SurfaceHolder) {
		Log.d(GameProc.TAG, "surfaceCreated()")
		synchronized(this) { mHasSurface = true }
	}

	override fun surfaceDestroyed(holder: SurfaceHolder) {
		Log.d(GameProc.TAG, "surfaceDestroyed()")
		synchronized(this) { mHasSurface = false }
	}
}
