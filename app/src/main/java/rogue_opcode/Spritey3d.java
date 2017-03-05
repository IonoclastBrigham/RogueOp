// Spritey3d.java
// Spritey class extended with pseudo-3D perspective
//
// Copyright © 2010-2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode;


import rogue_opcode.geometrics.XYZf;
import android.graphics.Canvas;
import android.graphics.RectF;


/**
 * Extends the Spritey paradigm to allow for pseudo-3d perspective correction.
 * This is achieved by simply scaling the size and XY drift of sprites based on
 * how far from the "camera" they are along the the Z axis.
 * <br /><br />
 * Some important differences from a standard {@code Spritey} to note: this
 * class treats the center of the screen as (0,0,0), positive Y is toward the
 * top of the screen, and positive Z into the screen. Additionally, sprites are
 * drawn centered, rather than from the top-left corner.
 * <br /><br />
 * <b>Example:</b> Drawing a sprite at (0,0,0) will draw it centered on the
 * screen with a scaling factor of 1.0.
 *
 * @see Spritey
 * @author Brigham Toskin
 */
public class Spritey3d extends Spritey
{
	private static final long serialVersionUID = 7198684330563604332L;

	public static float sCameraW, sCameraH;
	public static float sNormalZ;
	public static float sFarZ;
	static
	{
		sCameraW = 2f;
		sCameraH = 3f;
		float tRatio = (float)AnimatedView.sOnly.mScreenWidth
				/ (float)AnimatedView.sOnly.mScreenHeight;
		if(AnimatedView.sOnly.mBaseAspectRatio < tRatio)
			sNormalZ = AnimatedView.sOnly.mScreenWidth / sCameraW;
		else
			sNormalZ = AnimatedView.sOnly.mScreenHeight / sCameraH;
		sFarZ = sNormalZ * 4;
	}

	protected RectF mDrawDest;

	// c'tor ///////////////////////////////////////////////////////////////////

	/**
	 * @param pResourceID image resource ID of sprite strip graphic.
	 * @param pFrameCount number of frames in the sprite strip.
	 */
	public Spritey3d(int pResourceID, int pFrameCount)
	{
		super(pResourceID, pFrameCount);
		mDrawDest = new RectF();
	}

	/**
	 * @param pResourceID image resource ID of sprite strip graphic.
	 * @param pFrameCount number of frames in the sprite strip.
	 * @param pX initial X position.
	 * @param pY initial Y position.
	 * @param pZ initial Z position.
	 */
	public Spritey3d(int pResourceID, int pFrameCount, int pX, int pY, int pZ)
	{
		super(pResourceID, pFrameCount, pX, pY);
		mDrawDest = new RectF();
		ZDepth(pZ);
	}

	// game loop callbacks /////////////////////////////////////////////////////

	/**
	 * Draws the object at it's specified position, scaled for pseudo-3D
	 * perspective based on Z distance.
	 *
	 * @see rogue_opcode.Spritey#Draw()
	 */
	@Override
	public void Draw()
	{
		Canvas tCanvas = AnimatedView.sCurrentCanvas;
		if(tCanvas == null || mPos.z > sFarZ) // not ready or far clip
			return;

		XYZf tPos = mPos;
		float tScreenW = AnimatedView.sOnly.ScreenWidth();
		float tScreenH = AnimatedView.sOnly.ScreenHeight();

		float tScaleFactor = sNormalZ / tPos.z;
		float tLeft = (tPos.x - mFrameWidth / 2) * tScaleFactor + tScreenW / 2;
		float tTop = tScreenH / 2 - (tPos.y - mFrameHeight / 2) * tScaleFactor;
		RectF tDrawDest = mDrawDest;
		tDrawDest.left = tLeft;
		tDrawDest.top = tTop;
		tDrawDest.right = tLeft + mFrameWidth * tScaleFactor;
		tDrawDest.bottom = tTop + mFrameHeight * tScaleFactor;
		tCanvas.drawBitmap(mGR.mImage, mFrame.toRect(), tDrawDest, null);
	}
}
