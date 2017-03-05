// ScreenElement.java
// Updatable active sprite class.
//
// Copyright © 2010-2017 Christopher R. Tooley
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode;

import java.util.Comparator;

import rogue_opcode.containers.LazySortedArray;
import rogue_opcode.geometrics.XYZf;
import rogue_opcode.geometrics.XYf;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;


/** Generic updateable graphical object class.
 * <br /><br />
 * Override {@code Update()} to perform custom movement and animation,
 * or handle user input.
 * @see GraphicResource
 * @see ActionElement
 */
public class ScreenElement extends ActionElement
{
	private static final long serialVersionUID = 8512900123394987036L;

	protected GraphicResource mGR;
	protected int mCurrentGRResourceID;		//Now that GRs can come in and out of scope, mGR is more likely to be null.
											//If this SE is drawing a null mGR it will try to create a new GR based on this ID.

	public boolean mTopmost;				//If true this will be drawn in the
											//mEffectsHookBitmap after all non topmost SEs have been drawn

	public XYZf mPos;
	public XYZf mVel;

	public XYZf mTextOffset;				//offset for text drawing relative to the adjusted mPos TODO - in what coordinates?
											//TODO - add coordinate scaling and accessor methods


	protected boolean mVisible;

	public boolean mSelfGuided;
	XYf mSelfGuidedDestination;
	float mSelfGuidedSpeed;

	protected String mText;

	protected boolean mDrawCentered;
	protected boolean mDrawAbsolute;

	protected Paint mTextPaint;

	// sorted on Z depth
	// XXX: may be inefficient; keep an eye on performance
	public static LazySortedArray<ScreenElement> sAllSEs;
	public static int sActiveSECount = 0;

	static
	{
		try
		{
			if(sAllSEs == null)
				sAllSEs = new LazySortedArray<ScreenElement>(
					new Comparator<ScreenElement>()
					{
						@Override
						public int compare(ScreenElement a, ScreenElement b)
						{
							if(a.mPos.z > b.mPos.z)
								return -1;
							else if(a.mPos.z == b.mPos.z)
									return 0;
							else
								return 1;
						}
					});
		}
		catch(Exception e)
		{
			Log.d(GameProc.TAG, "ScreenElement.Init()", e);
		}
	}

	// c'tors //////////////////////////////////////////////////////////////////

	public ScreenElement(int pResourceID)
	{
		init(pResourceID, null, 0, 0);
	}

	public ScreenElement(String pText)
	{
		init(0, pText, 0, 0);
	}

	public ScreenElement(int pResourceID, int pX, int pY)
	{
		init(pResourceID, null, pX, pY);
	}

	public ScreenElement(String pText, int pX, int pY)
	{
		init(0, pText, pX, pY);
	}

	public ScreenElement(int pResourceID, String pText, int pX, int pY)
	{
		init(pResourceID, pText, pX, pY);
		mDrawCentered = false;
		mDrawAbsolute = false;
	}

	protected void init(int pResourceID, String pText, int pX, int pY)
	{
		if(pResourceID != 0)
			mGR = GraphicResource.FindGR(pResourceID);
		else
			mGR = null;

		mCurrentGRResourceID = pResourceID;

		mPos = new XYZf(pX, pY, 100);
		mVel = new XYZf();

		mTextOffset = new XYZf(0,0,0);
		mText = pText;

		mDrawCentered = true;
		mVisible = true;
		mSelfGuided = false;
		mTopmost = false;

		mSelfGuidedDestination = new XYf();

		try
		{
			sAllSEs.Append(this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//In theory we can call unload on an SE to remove it's reference from the list of all SEs
	//and the list of all AEs.  The goal is to remove all references to the SE so that the garbage
	//collector can get rid of them.
	//An example scenario could be a situation where you have a dynamic menu structure that uses SEs
	//to draw itself.  These SEs are generated (new) automatically as menu items are added.  We want
	//to be able to call RemoveMenuItem() and have the associated SE go away permanently (not just invisible).
	public void Unload() {//iterate over array to find and then remove
		for (int i=0;i<sAllSEs.size;i++)
			if (sAllSEs.At(i) == this) {
				sAllSEs.Remove(i);
				break;
			}

		for (int i=0;i<sAllAEs.size;i++)
			if (sAllAEs.At(i) == this) {
				sAllAEs.Remove(i);
				break;
			}

		//TODO - MUST ENSURE THAT THIS IS REALLY WORKING OR WE WILL "LEAK" ALL OVER THE PLACE!
}

	// public interfaces ///////////////////////////////////////////////////////

	public void Hibernate()
	{
		Visible(false); // Turn off drawing
		mActive = false; // stop calling update function
	}

	public void Wake()
	{
		Visible(true);
		mActive = true;
	}

	public boolean Visible()
	{
		return mVisible;
	}

	public void Visible(boolean pVisible)
	{
		mVisible = pVisible;
	}

	public boolean Topmost()
	{
		return mTopmost;
	}

	public void Topmost(boolean pTopmost)
	{
		mTopmost = pTopmost;
	}

	public void SetCurrentGR(int pResourceID)
	{
		mCurrentGRResourceID = pResourceID;
		mGR = GraphicResource.FindGR(pResourceID);
	}

	public GraphicResource getCurrentGR()
	{
		return mGR;
	}

	public Boolean WithinRange(ScreenElement pTargetSE, float pRadius)
	{
		XYf tXYf = new XYf(pTargetSE.mPos);

		if (!pTargetSE.mDrawCentered) {
			tXYf.x -= (pTargetSE.Width() / 2);
			tXYf.y -= (pTargetSE.Height() / 2);
		}

		return WithinRange(tXYf, pRadius);
	}

	public Boolean WithinRange(XYf pTarget, float pRadius)
	{
		return WithinRange(pTarget, pRadius, pRadius);
	}

	public Boolean WithinRange(XYf pTarget, float pXRadius, float pYRadius)
	{
		if (!Visible())
			return false;

		XYf tXYf = new XYf(mPos);

		if (!mDrawCentered) {
			tXYf.x += (Width() / 2);
			tXYf.y += (Height() / 2);
		}

		if( (tXYf.x < (pTarget.x + pXRadius)) &&
				(tXYf.x > (pTarget.x - pXRadius)) &&
				(tXYf.y < (pTarget.y + pYRadius)) &&
				(tXYf.y > (pTarget.y - pYRadius)) )
			return true;
		return false;
	}

	public void DrawCentered(boolean pDrawCentered)
	{
		mDrawCentered = pDrawCentered;
	}

	public void DrawAbsolute(boolean pDrawAbsolute)
	{
		mDrawAbsolute = pDrawAbsolute;
	}

	public int Width()
	{
		return (mGR != null) ? mGR.VirtualWidth() : 0;
	}

	public int Height()
	{
		return (mGR != null) ? mGR.VirtualHeight() : 0;
	}

	public String Text()
	{
		return mText;
	}

	public void Text(String pText)
	{
		mText = pText;
	}

	public XYf Pos()
	{
		return mPos;
	}

	public void Pos(float pX, float pY)
	{
		mPos.x = pX;
		mPos.y = pY;
	}

	public float ZDepth()
	{
		return mPos.z;
	}

	public void ZDepth(float pZ)
	{
		mPos.z = pZ;
		sAllSEs.mDirty = true;
	}

	public void moveTo(float pSpeed, XYf pDestination) {
		mSelfGuided = true;

		mSelfGuidedDestination.x = pDestination.x;
		mSelfGuidedDestination.y = pDestination.y;

		mSelfGuidedSpeed = pSpeed;
	}

	//Query if a self-guided SE has reached its destination
	public boolean HasReachedDestination() {
		return !mSelfGuided;
	}

	public void setTextPaint(Paint pPaint) {
		mTextPaint = pPaint;
	}

	/**
	 * AnimatedView will call this repeatedly during the program's lifetime
	 * automatically. Override in your derived class to do something exciting.
	 *
	 * @see rogue_opcode.ActionElement#Update()
	 */
	@Override
	public void Update()
	{
		if(mSelfGuided) {
			double xDist = mSelfGuidedDestination.x - mPos.x;
			double yDist = mSelfGuidedDestination.y - mPos.y;

			double tDistance = Math.sqrt(Math.pow(xDist,2) + Math.pow(yDist,2));

			if (tDistance != 0) {
				mVel.x = (float)(xDist / tDistance) * mSelfGuidedSpeed;
				mVel.y = (float)(yDist / tDistance) * mSelfGuidedSpeed;
			}

			mPos.add(mVel);

			if (tDistance <= mSelfGuidedSpeed) {
				mSelfGuided = false;
				mPos.x = mSelfGuidedDestination.x;
				mPos.y = mSelfGuidedDestination.y;
			}
		}
	}

	/**
	 * AnimatedView will call this interface at draw time. Override in your
	 * derived class to do something more than draw the currentGR at it's
	 * current {@code mPos} coordinates.
	 */
	public void Draw()
	{
		Canvas tCanvas = AnimatedView.sCurrentCanvas;
		float tX = mPos.x * AnimatedView.sOnly.mPreScaler;
		float tY = mPos.y * AnimatedView.sOnly.mPreScaler;
		if((mGR != null) && mGR.Valid()) {
			if (mDrawCentered)
			{
				tX -= ((mGR.PhysicalWidth()) / 2);
				tY -= ((mGR.PhysicalHeight()) / 2);
			}
			tCanvas.drawBitmap(mGR.mImage, tX, tY, null);
		} else {
			//We are trying to draw an empty GR - lets see if it has recently been loaded
			SetCurrentGR(mCurrentGRResourceID);
		}
		if(mText != null && mText.length() > 0) {
			Paint tPaint = AnimatedView.sOnly.mPaint;
			if (mTextPaint != null)
				tPaint = mTextPaint;
			tCanvas.drawText(mText, tX + mTextOffset.x, tY + mTextOffset.y, tPaint);
		}
	}
}

