// GameProc.java
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


package rogue_opcode;


// import dalvik.system.VMRuntime;
// import com.ngc.MGEPCT.BaG;

import rogue_opcode.geometrics.XYf;
import rogue_opcode.soundy.SoundEffect;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;


// import android.view.GestureDetector.OnDoubleTapListener;
// CRT - the OnDoubleTapListener code works great, but because android is
// listening for the double tap after a single
// tap you cannot tap and then flick in rapid succession as is needed in a game
// - for example if a single tap triggers a jump
// and a fling triggers a move then if double tap support is enabled you cannot
// immediately jump and then move - the move
// will be delayed by about a (critical) 1/2 second.
// I'd like to make this some kind of parameter but don't know how to do that
// with interfaces...

public class GameProc extends Activity implements Runnable, OnGestureListener/*
																			 * ,
																			 * OnDoubleTapListener
																			 */
{
	public static final long UPDATE_FREQ = 30;
	public static final long UPDATE_PERIOD = 1000 / UPDATE_FREQ;
	public static final String TAG = "ionoclast";

	public static GameProc sOnly;
	protected static Thread sUpdateThread;

	public RelativeLayout mLayout;

	// stats
	protected long mElapsedTime;
	protected static int sSeconds;
	protected static long sFPS;

	protected boolean mMousing;
	protected XYf mMousePos;

	private GestureDetector mMotionDetector;
	public TouchState mTouchState;

	int mCurrentKey;
	public boolean[] mKeys; //holds the state of the keys on the keyboard

	protected boolean mRunning;
	protected boolean mRestarting;
	protected boolean mExiting;

	EditableTextPositionParams mEditTextParams;

	class EditableTextPositionParams
	{
		XYf mQueuedViewLocation;
		int mWidth;
		int mHeight;
		TextSE mCurrentTE;
		EditText mEdit;
		RelativeLayout.LayoutParams mRLP;
		RelativeLayout mRL;

		EditableTextPositionParams()
		{
			mQueuedViewLocation = null;
			mWidth = 0;
			mHeight = 0;
			mCurrentTE = null;
			mRLP = null;
			mRL = null;
		}

		EditableTextPositionParams(TextSE pCurrentTE, XYf pPos, int pWidth,
			int pHeight)
		{
			mHeight = pHeight;
			mWidth = pWidth;
			mQueuedViewLocation = pPos;
			mCurrentTE = pCurrentTE;
		}
	}

	// app lifecycle ///////////////////////////////////////////////////////////

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedState)
	{
		Log.d(TAG, "onCreate()");
		super.onCreate(savedState);
		sOnly = this;

		//VMRuntime.getRuntime().setMinimumHeapSize(4 * 1048576);

		mKeys = new boolean[525];

		// set up graphics
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window tWin = getWindow();
		tWin.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		tWin.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		mLayout = new RelativeLayout(this);
		setContentView(mLayout);
		mLayout.addView(new AnimatedView(this));
		mEditTextParams = null;

		// initialize stats
		mElapsedTime = 0;
		sSeconds = 0;
		sFPS = 0;

		//		if(savedState != null)
		//		{
		//			Log.d(TAG, "  Restoring state");
		//			super.onRestoreInstanceState(savedState);
		//			ActionElement.sAllAEs = (Array<ActionElement>)savedState
		//					.getSerializable("sAllAEs");
		//			AudioResource.sAllARs = (HashMap<Integer, AudioResource>)savedState
		//					.getSerializable("sAllARs");
		//		}

		// user-provided init code
		mMotionDetector = new GestureDetector(this, this);
		mTouchState = new TouchState();

		InitializeOnce();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		Log.d(TAG, "onSaveInstanceState()");
		super.onSaveInstanceState(outState);
		//			outState.putSerializable("sAllAEs", ActionElement.sAllAEs);
		//			outState.putSerializable("sAllARs", AudioResource.sAllARs);
	}

	/**
	 * Called when app becomes active.
	 */
	@Override
	protected void onResume()
	{
		Log.d(TAG, "onResume()");
		super.onResume();

		// input data
		mMousing = false;
		mMousePos = new XYf();

		// restore user settings
		SharedPreferences tPrefs = getPreferences(0);
		if(tPrefs.getBoolean("saved", false))
		{
			// TODO: restore settings to SettingsDB object in loop
		}
		else
		{
			// TODO: load SettingsDB with default values
		}

		// user initialization code
		InitializeOnResume();

		// start the render and update threads
		(sUpdateThread = new Thread(this)).start();
		(AnimatedView.sRenderThread = new Thread(AnimatedView.sOnly)).start();

		AnimatedView.sOnly.requestFocus();


		// clean some up now to avoid latency later
		Runtime r = Runtime.getRuntime();
		r.gc();
	}

	/** Called when app is backgrounded, but may still be visible. */
	@Override
	protected void onPause()
	{
		Log.d(TAG, "onPause()");
		super.onPause();

		Shutdown();

		// save user settings
		SharedPreferences.Editor tEditor = getPreferences(0).edit();
		// TODO: save user settings in a loop from a static SettingsDB;
		// each entry is { "name", setting_type, setting_data }.
		tEditor.putBoolean("saved", true);
		tEditor.commit();

		// shut down
		Die();
		AnimatedView.sOnly.Die();
		AudioResource.Die(); // stop and free all audio resources
		SoundEffect.Die(); // free the sound pool

		// clean some up now to avoid latency later
		Runtime r = Runtime.getRuntime();
		r.gc();
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

	/** Stops the update thread. */
	void Die()
	{
		synchronized(this)
		{
			mRunning = false;
			sUpdateThread = null;
		}
	}

	/** Override this in your derived class to make stuff. */
	public void InitializeOnce()
	{
	}

	/** Override this to hook the resume event. */
	public void InitializeOnResume()
	{
	}

	/** Override this to hook the shutdown event. */
	public void Shutdown()
	{
	}

	// game update loop ////////////////////////////////////////////////////////

	/** Thread to run logic updates */
	@Override
	public void run()
	{
		Log.d(TAG, "Entering update thread");
		mRunning = true;
		while(mRunning)
		{
			long start = SystemClock.uptimeMillis();

			// calc stats
			if(mElapsedTime >= 999)
			{
				synchronized(this)
				{
					sSeconds++;
					mElapsedTime -= 1000;
					sFPS = AnimatedView.sOnly.FPS();
				}
			}

			Update();

			long end = SystemClock.uptimeMillis();
			long tLastUpdate = end - start;
			mElapsedTime += UPDATE_PERIOD;
			try
			{
				long tSleep = UPDATE_PERIOD - tLastUpdate;
				if(tSleep < 1)
					tSleep = 1;
				Thread.sleep(tSleep);
			}
			catch(InterruptedException e)
			{
			}
		}
		Log.d(TAG, "Exiting update thread");
	}

	// public interfaces //

	/** Calls update on all extant ActionElements. */
	public void Update()
	{
		for(int i = 0; i < ActionElement.sAllAEs.size; i++)
		{
			ActionElement tAE = ActionElement.sAllAEs.At(i);
			if(tAE.Active())
				tAE.Update();
		}

		mTouchState.Clear(true);
	}

	//AddView allows the user to pass a view (typically a layout that was inflated from XML) to be added above the normal AnimatedView Surface.
	//We need to do this from within the original UI thread which this function facilitates.
	public void ShowTextEditor(TextSE pCurrentTE, XYf pPos, int pWidth,
		int pHeight)
	{
		if(mEditTextParams == null)
		{
			mEditTextParams =
				new EditableTextPositionParams(pCurrentTE, pPos, pWidth,
					pHeight);
			mEditTextParams.mQueuedViewLocation = pPos;

			mLayout.post(new Runnable()
			{
				@Override
				public void run()
				{

					mEditTextParams.mRLP =
						new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.FILL_PARENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					mEditTextParams.mRLP.setMargins(
						(int)mEditTextParams.mQueuedViewLocation.x,
						(int)mEditTextParams.mQueuedViewLocation.y, 0, 0);
					mEditTextParams.mRL = new RelativeLayout(GameProc.sOnly);

					mEditTextParams.mRL.setLayoutParams(mEditTextParams.mRLP);

					mEditTextParams.mEdit = new EditText(GameProc.sOnly);

					mEditTextParams.mRL.addView(mEditTextParams.mEdit);

					mLayout.addView(mEditTextParams.mRL);

					mEditTextParams.mEdit.requestFocus();
				}
			});
		}

		mEditTextParams.mQueuedViewLocation = pPos;
		mEditTextParams.mWidth = pWidth;
		mEditTextParams.mHeight = pHeight;
		mEditTextParams.mCurrentTE = pCurrentTE;

		mLayout.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				mEditTextParams.mRLP.setMargins(
					(int)(mEditTextParams.mQueuedViewLocation.x * AnimatedView.sOnly.mPreScaler),
					(int)(mEditTextParams.mQueuedViewLocation.y * AnimatedView.sOnly.mPreScaler),
					0, 0);

				mEditTextParams.mRL.setLayoutParams(mEditTextParams.mRLP);
				mEditTextParams.mEdit.setVisibility(View.VISIBLE);
				mEditTextParams.mEdit.setWidth((int)(mEditTextParams.mWidth * AnimatedView.sOnly.mPreScaler));
				mEditTextParams.mEdit.setTransformationMethod(new android.text.method.SingleLineTransformationMethod());
				mEditTextParams.mEdit.setMaxLines(1);
				mEditTextParams.mEdit.setBackgroundColor(Color.BLUE);
				if(mEditTextParams.mCurrentTE != null &&
					mEditTextParams.mCurrentTE.Text() != null &&
					mEditTextParams.mCurrentTE.Text().length() > 0)
				{
					mEditTextParams.mEdit.setText(mEditTextParams.mCurrentTE.Text());
					mEditTextParams.mEdit.setSelection(mEditTextParams.mCurrentTE.Text()
																					.length());
				}
				else
				{
					mEditTextParams.mEdit.setText("");
				}
				mEditTextParams.mEdit.requestFocus();
			}

		}, 100);
	}

	public void HideTextEditor(Object pCurrentTE)
	{
		if(mEditTextParams.mCurrentTE == pCurrentTE)
		{
			try
			{
				mLayout.post(new Runnable()
				{
					@Override
					public void run()
					{
						mEditTextParams.mEdit.setVisibility(View.INVISIBLE);
						try
						{
							InputMethodManager tIMM =
								(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							tIMM.hideSoftInputFromWindow(
								mEditTextParams.mEdit.getWindowToken(), 0);
						}
						catch(Exception e)
						{
						}
					}
				});
			}
			catch(Exception e)
			{
			}
		}
	}

	// runtime stats ///////////////////////////////////////////////////////////

	/** Query performance statistics based on update timing. */
	// TODO: does this belong here? Move to AnimatedView
	long FPS()
	{
		synchronized(this)
		{
			return sFPS;
		}
	}

	/** Query uptime statistics for update thread. */
	long Seconds()
	{
		synchronized(this)
		{
			return sSeconds;
		}
	}

	// user input callbacks ////////////////////////////////////////////////////

	@Override
	public boolean onTrackballEvent(MotionEvent pEvent)
	{
		mMousePos.x = (int)(pEvent.getX() * 100);
		mMousePos.y = (int)(pEvent.getY() * 100);

		mMousing = true;
		return true;
	}

	public boolean Mousing()
	{
		return mMousing;
	}

	public XYf MousePos()
	{
		mMousing = false;
		return mMousePos;
	}


	@Override
	public boolean onTouchEvent(MotionEvent pMotionEvent)
	{
		this.mMotionDetector.onTouchEvent(pMotionEvent);
		return super.onTouchEvent(pMotionEvent);
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
		float velocityY)
	{
		mTouchState.SetState(TouchState.FLING, e1, e2);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		mTouchState.SetState(TouchState.LONG_TOUCH, e, null);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
		float distanceY)
	{
		AnimatedView.sOnly.mDebugString1 = distanceY + "";

		//if (Math.abs(distanceY) > 1.5f)
		mTouchState.SetState(TouchState.SCROLL, e1, e2, distanceY, distanceX);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		mTouchState.SetState(TouchState.SINGLE_TAP, e, null);
		return false;
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


	/**
	 * TouchState is an inner class that holds state information about touch
	 * events.
	 * It is designed to be polled instead of event-driven.
	 *
	 * @author Christopher R. Tooley
	 *
	 */
	public class TouchState
	{
		public static final int SINGLE_TAP = 1;
		public static final int DOUBLE_TAP = 2; //these have been removed for performance reasons
		public static final int FLING = 4;
		public static final int SCROLL = 8; //Think "drag"
		public static final int LONG_TOUCH = 16;

		int mState;

		MotionEvent mMainMotionEvent;
		MotionEvent mSecondaryMotionEvent;
		float mYScrollDist; //holds either the velocity of the fling or the distance of the scroll
		float mXScrollDist; //holds either the velocity of the fling or the distance of the scroll

		TouchState()
		{
			Clear();
		}

		public void SetState(int pState, MotionEvent pMainMotionEvent,
			MotionEvent pSecondaryMotionEvent, float pYScrollDist,
			float pXScrollDist)
		{
			SetState(pState, pMainMotionEvent, pSecondaryMotionEvent);
			mXScrollDist = pXScrollDist;
			mYScrollDist = pYScrollDist;
		}

		public void SetState(int pState, MotionEvent pMainMotionEvent,
			MotionEvent pSecondaryMotionEvent)
		{
			synchronized(this)
			{
				mState |= pState;
				mMainMotionEvent = pMainMotionEvent;
				mSecondaryMotionEvent = pSecondaryMotionEvent;
			}
		}

		public void Clear()
		{
			synchronized(this)
			{
				mState = 0;
			}
		}

		private void Clear(boolean pInternalCall)
		{
			synchronized(this)
			{
				mState = 0;
			}
		}

		public XYf TouchPos()
		{
			if(mMainMotionEvent == null)
				return new XYf(0, 0);

			return new XYf(mMainMotionEvent.getX(), mMainMotionEvent.getY());
		}

		public XYf SecondaryTouchPos()
		{
			if(mSecondaryMotionEvent == null)
				return new XYf(0, 0);

			return new XYf(mSecondaryMotionEvent.getX() /
							AnimatedView.sOnly.mPreScaler,
				mSecondaryMotionEvent.getY() / AnimatedView.sOnly.mPreScaler);
		}

		public float GetXScrollDist()
		{
			return mXScrollDist;
		}

		public float GetYScrollDist()
		{
			return mYScrollDist;
		}

		public float GetMainX()
		{
			if(mMainMotionEvent == null)
				return 0;

			float tX = 0;
			synchronized(this)
			{
				tX = mMainMotionEvent.getX() / AnimatedView.sOnly.mPreScaler;
			}
			return tX;
		}

		public float GetMainY()
		{
			if(mMainMotionEvent == null)
				return 0;

			float tY = 0;
			synchronized(this)
			{
				tY = mMainMotionEvent.getY() / AnimatedView.sOnly.mPreScaler;
			}
			return tY;
		}

		public float GetSecondaryX()
		{
			if(mSecondaryMotionEvent == null)
				return 0;

			float tX = 0;
			synchronized(this)
			{
				tX =
					mSecondaryMotionEvent.getX() /
						AnimatedView.sOnly.mPreScaler;
			}
			return tX;
		}

		public float GetSecondaryY()
		{
			if(mSecondaryMotionEvent == null)
				return 0;

			float tY = 0;
			synchronized(this)
			{
				tY =
					mSecondaryMotionEvent.getY() /
						AnimatedView.sOnly.mPreScaler;
			}
			return tY;
		}

		public boolean Is(int pState)
		{
			synchronized(this)
			{
				return 0 != (mTouchState.mState & pState);
			}
		}
	}

}