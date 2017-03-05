// AnimatedView.java
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


package rogue_opcode;

import rogue_opcode.geometrics.XYf;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class AnimatedView
	extends SurfaceView implements SurfaceHolder.Callback, Runnable
{
	// self-refs
	public static AnimatedView sOnly;
	protected static Thread sRenderThread;

	// TODO: make these static for performance when possible? //

	// physical screen stuff
	protected SurfaceHolder mHolder;
	public static Canvas sCurrentCanvas;
	protected boolean mHasSurface = false;
	protected int mScreenWidth, mScreenHeight;
	protected boolean mSized = false;

	// Base refers to desired dimensions and aspect ratio of the ideal machine,
	// used for reference when scaling virtual dimensions to physical dimensions
	protected int mLogicalWidth, mLogicalHeight;
	protected float mBaseAspectRatio;
	public float mPreScaler = 1.0f;

	// scrolling properties
	// TODO: this gets too jerky—need something that jumps as the player gets
	// too close to being offscreen. Maybe scale based on distance from center?
	protected float mBaseHScroll = 0;
	static XYf mScrollSpeed = new XYf(2, 2);
	ScreenElement mKeepCenteredSE = null;

	// debug stats
	public Paint mPaint;
	public Paint mMaskPaint;			//Paint used with Porter-Duff Effects-hook drawing
	public Paint mTransparentPaint;		//Paint used to clear out alpha channel pixels when using mMaskPaint
	//TODO - standardize on when to use static member vars - should mDebugString1 be accessed via sOnly or directly as a static?
	//either is fine but we should do it one way or another - or am I missing something here?
	protected static int sFramesDrawn;
	protected static boolean sDebug;
	public String mDebugString1;
	public String mDebugString2;
	
	boolean mRunning;
	
	Bitmap mEffectsHookBitmap;
	Canvas mEffectsHookCanvas;
	boolean mEffectsEnabled;
	// c'tor ///////////////////////////////////////////////////////////////////

	public AnimatedView(Context pContext)
	{
		super(pContext);
		sOnly = this;
		this.setId(0xdeadbeef); // set ID, so OS will manage restoring state

		// init debug stats
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.BLUE);
		sFramesDrawn = 0;
		sDebug = false;
				
		mDebugString1 = new String();
		mDebugString2 = new String();

		mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		mTransparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTransparentPaint.setColor(Color.TRANSPARENT);

		// init screen/rendering
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		Surface tSurface = mHolder.getSurface();
//		tSurface.setAlpha(100);

		//((Activity)pContext).setContentView(this);
		

		DisplayMetrics tDM = new DisplayMetrics();
		GameProc.sOnly.getWindowManager().getDefaultDisplay().getMetrics(tDM);
		mScreenWidth = tDM.widthPixels;
		mScreenHeight = tDM.heightPixels;
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		mEffectsHookBitmap = null;
		mEffectsHookCanvas = null;
		mEffectsEnabled = false;
	}
	
	@Override
	public boolean onKeyDown(int pKeyCode, KeyEvent pEvent)  {
		GameProc.sOnly.mCurrentKey = pKeyCode;
		GameProc.sOnly.mKeys[pKeyCode] = true;
		
		if (pKeyCode == KeyEvent.KEYCODE_BACK)
			return false;
		
		return true;
	}
	
	@Override
	public boolean onKeyUp(int pKeyCode, KeyEvent pEvent)  {
		GameProc.sOnly.mKeys[pKeyCode] = false;
		
		return true;
	}
	
	// screen and drawing properties ///////////////////////////////////////////

	//The EffectsHook allows us to manipulate the current main bitmap/canvas before it is brought to the screen
	//It can be used to draw glow effects and use Porter-Duff transfer modes for advanced masking/blend effects.
	public void EnableEffectsHook() {
		if (mEffectsHookBitmap == null) {
			mEffectsHookBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888);
			mEffectsHookCanvas = new Canvas(mEffectsHookBitmap);
		}
		mEffectsEnabled = true;
	}
	
	public void DisableEffectsHook() {
		mEffectsEnabled = false;
	}
	
	/**
	 * Set up a display with specified virtual resolution and aspect ratio. This
	 * method is used to tell {@code AnimatedView} the logical screen size and
	 * aspect ratio we are programming to, allowing it to pre-scale all graphics
	 * and draw positions appropriately.
	 *
	 * @param pWidth virtual width of display area.
	 * @param pHeight virtual height of display area.
	 */
	public void NormailzeResolution(int pWidth, int pHeight)
	{
		mLogicalWidth = pWidth;
		mLogicalHeight = pHeight;

		mBaseAspectRatio = (float)pWidth / (float)pHeight;
		float tScreenAspectRatio = (float)mScreenWidth / (float)mScreenHeight;
		if(mBaseAspectRatio < tScreenAspectRatio)
			mPreScaler = (float)mScreenHeight / (float)mLogicalHeight;
		else
			mPreScaler = (float)mScreenWidth / (float)mLogicalWidth;
	}

	/**
	 * The "camera" will scroll around to follow this SE and keep it more or
	 * less centered on the screen.
	 *
	 * @param pKeepCenteredSE ScreenElement to follow.
	 */
	public void SetKeepCenteredSE(ScreenElement pKeepCenteredSE)
	{
		mKeepCenteredSE = pKeepCenteredSE;
	}

	public int LogicalWidth()
	{
		return mLogicalWidth;
	}

	public int LogicalHeight()
	{
		return mLogicalHeight;
	}

	public int ScreenWidth()
	{
		synchronized(this)
		{
			return mScreenWidth;
		}
	}

	public int ScreenHeight()
	{
		synchronized(this)
		{
			return mScreenHeight;
		}
	}

	public int FPS()
	{
		synchronized(this)
		{
			int tFrames = sFramesDrawn;
			sFramesDrawn = 0;
			return tFrames;
		}
	}

	public boolean Debug()
	{
		synchronized(this)
		{
			return sDebug;
		}
	}

	public void Debug(boolean pDebug)
	{
		synchronized(this)
		{
			sDebug = pDebug;
		}
	}
	
	// render thread ///////////////////////////////////////////////////////////

	public void Die()
	{
		mRunning = false;
		sRenderThread = null;
	}

	/** render thread loop */
	@Override
	public void run()
	{
		Log.d(GameProc.TAG, "Entering render thread");
		mRunning = true;
		while(mRunning)
		{
			synchronized(this)
			{
				while(!mHasSurface)
				{
					try
					{
						wait(); // wait for a valid surface
					}
					catch(InterruptedException e)
					{
					}
				}
				sFramesDrawn++;
			}
			draw();
		}
		Log.d(GameProc.TAG, "Exiting render thread");
	}

	private void loopAndDrawSEs(boolean pTopmost) {
		for(int i = 0; i < ScreenElement.sAllSEs.size; i++)
		{
			ScreenElement tSE;
			try
			{
				tSE = ScreenElement.sAllSEs.At(i);
				if(tSE.Visible()) {
					ScreenElement.sActiveSECount ++;
					if ((pTopmost && tSE.Topmost()) || (!pTopmost && !tSE.Topmost()))
						tSE.Draw();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	/** draws all ScreenElements to the screen */
	protected void draw()
	{
		Canvas tCanvas = mHolder.lockCanvas();
		if(tCanvas == null)
			return;

		ScreenElement.sActiveSECount = 0;			//performance monitor variable keeps track of visible count of SEs
													//This is important because now we can unload graphics but unloading them
													//does not stop the draw proc.

		// Draw the "keep-centered" graphic
		// (You are not expected to understand this—I don't!)
		if(mKeepCenteredSE != null)
		{
			if(((mKeepCenteredSE.mPos.x - (mLogicalWidth / 6)) + mBaseHScroll)
					< (mLogicalWidth / 2))
				mBaseHScroll += (mScrollSpeed.x * mPreScaler);
			if(((mKeepCenteredSE.mPos.x + (mLogicalWidth / 6)) + mBaseHScroll)
					> (mLogicalWidth / 2))
				mBaseHScroll -= (mScrollSpeed.x * mPreScaler);
			if(((mKeepCenteredSE.mPos.x - (mLogicalWidth / 6)) + mBaseHScroll)
					< (mLogicalWidth / 2))
				mBaseHScroll += (mScrollSpeed.x * mPreScaler);
			if(((mKeepCenteredSE.mPos.x + (mLogicalWidth / 6)) + mBaseHScroll)
					> (mLogicalWidth / 2))
				mBaseHScroll -= (mScrollSpeed.x * mPreScaler);

			// TODO - need to limit how much we allow mBaseHScroll to go - when
			// we get to the edge of a background graphic we want to pin the
			// graphic to the edge of the screen and move the keep-centered SE
			// to the edge. Problem is that the size of the background graphic
			// we want to contain may not be the same as mBaseWidth and height
		}

		// draw world
		if (mEffectsEnabled) {
			sCurrentCanvas = mEffectsHookCanvas;
			mEffectsHookBitmap.eraseColor(Color.TRANSPARENT);
		} else {
			sCurrentCanvas = tCanvas;
		}
		
		tCanvas.drawRGB(0, 0, 0); // TODO: parameterize whether to do this
		ScreenElement.sAllSEs.mDirty = true;
		ScreenElement.sAllSEs.SortIfDirty();
		loopAndDrawSEs(false);
		
		if (mEffectsEnabled) {
			sCurrentCanvas = tCanvas;
			
			tCanvas.drawBitmap(mEffectsHookBitmap, 0, 0, null);

			loopAndDrawSEs(true);
			
			tCanvas.drawBitmap(mEffectsHookBitmap, 0, 0, mMaskPaint);
		}

		// draw stats
		if(sDebug)
		{
			sCurrentCanvas.drawText("Time: " + GameProc.sOnly.Seconds(), 10, 10, mPaint);
			sCurrentCanvas.drawText("FPS:  " + GameProc.sOnly.FPS(), 10, 22, mPaint);
			sCurrentCanvas.drawText("#1:  " + mDebugString1, 10, 34, mPaint);
			sCurrentCanvas.drawText("#2:  " + mDebugString2, 10, 46, mPaint);
			sCurrentCanvas.drawText(GameProc.sOnly.mTouchState.GetMainX() + ", " + GameProc.sOnly.mTouchState.GetMainY(), 10, 58, mPaint);
		}

		sCurrentCanvas = null;
		mHolder.unlockCanvasAndPost(tCanvas);
	}

	// screen update callbacks /////////////////////////////////////////////////

	@Override
	public void surfaceChanged(SurfaceHolder sh, int fmt, int w, int h)
	{
		Log.d(GameProc.TAG, "surfaceChanged()");
		synchronized(this)
		{
			mSized = true;
			mScreenWidth = w;
			mScreenHeight = h;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.d(GameProc.TAG, "surfaceCreated()");
		synchronized(this)
		{
			mHasSurface = true;
			notify();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.d(GameProc.TAG, "surfaceDestroyed()");
		synchronized(this)
		{
			mHasSurface = false;
		}
	}
}
