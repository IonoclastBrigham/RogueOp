// Spritey.java
// Sprite class with sheet/frame animation and collision
//
// Copyright © 2009-2017 Brigham Toskin, Leea Harlan
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode;

import android.graphics.Canvas;
import android.graphics.RectF;

import rogue_opcode.geometrics.Rectangle;
import rogue_opcode.geometrics.Rectanglef;
import rogue_opcode.geometrics.XYf;


/**
 * Extends {@code ScreenElement} with sprite strip animation, and collision.
 *
 * @see ScreenElement
 * @author Brigham Toskin
 */
public class Spritey extends ScreenElement
{
	private static final long serialVersionUID = -5463549117882033160L;

	protected Rectangle mFrame;
	protected int mFrameWidth;
	protected int mFrameHeight;
	protected int mFrameCount;
	public int msecsPerFrame = 33;	// ~30 FPS
	public int timeSinceLastFrame = 0;

	public Rectanglef boundingBox;
	public int bbOffset = 0;

	// c'tor ///////////////////////////////////////////////////////////////////

	/**
	 * Constructs a Spritey to load the specified resource.
	 * 
	 * @param pResourceID resource to load.
	 * @param pFrameCount number of frames in the sprite strip.
	 */
	public Spritey(int pResourceID, int pFrameCount)
	{
		super(pResourceID);
		init(pFrameCount);
	}

	/**
	 * Constructs a Spritey to load the specified resource at specified logical
	 * screen coordinates.
	 * 
	 * @param pResourceID resource to load.
	 * @param pFrameCount number of frames in the sprite strip.
	 * @param pX horizontal position of new Spritey.
	 * @param pY vertical position of new Spritey.
	 */
	public Spritey(int pResourceID, int pFrameCount, int pX, int pY)
	{
		super(pResourceID, pX, pY);
		init(pFrameCount);
	}

	protected void init(int pFrameCount)
	{
		Hibernate();
		mFrameCount = pFrameCount;
		mFrameWidth = mGR.PhysicalWidth() / pFrameCount;
		mFrameHeight = mGR.PhysicalHeight();
		mFrame = new Rectangle(0, 0, mFrameWidth, mFrameHeight);
	}

	// game loop callbacks /////////////////////////////////////////////////////

	/**
	 * Handles frame animation and velocity-based displacement updates.
	 *
	 * @see rogue_opcode.ScreenElement#Update()
	 */
	@Override
	public void Update()
	{
		Rectangle tFrame = mFrame;
		timeSinceLastFrame += GameProc.UPDATE_PERIOD;
		while(timeSinceLastFrame > msecsPerFrame)
		{
			timeSinceLastFrame -= msecsPerFrame;
			tFrame.X(mFrameWidth);
			if(tFrame.X() >= mGR.PhysicalWidth())
				// right side is off the end; reset
				resetFrame();
		}
		super.Update();
	}

	@Override
	public void Draw()
	{
		Canvas tCanvas = AnimatedView.sCurrentCanvas;
		if(tCanvas != null)
		{
			XYf tPos = mPos;
			RectF tDest = new RectF(tPos.x, tPos.y, tPos.x + mFrameWidth,
					tPos.y + mGR.PhysicalHeight());
			tCanvas.drawBitmap(mGR.mImage, mFrame.toRect(), tDest, null);
		}
	}

	protected void resetFrame()
	{
		mFrame.X(0);
	}

	/**
	 * Checks for collision between two {@code Spritey}s.
	 *
	 * @param pSpritey {@code Spritey} to check against
	 * @return {@code true} on collision, {@code false} otherwise
	 */
	public boolean Collide(Spritey pSpritey)
	{
		return Collide(pSpritey.boundingBox);
	}

	/**
	 * Checks for collision with an arbitrary {@link Rectangle}.
	 *
	 * @param pBBox bounding box of {@code Spritey} to check against
	 * @return {@code true} on collision, {@code false} otherwise
	 */
	public boolean Collide(Rectanglef pBBox)
	{
		return boundingBox.toRectF().intersect(pBBox.toRectF());
	}

	// Spritey properties //////////////////////////////////////////////////////

	// Translates between frames per second and delay between frames.
	public int FrameRate()
	{
		return 1000 / msecsPerFrame;
	}

	public void FrameRate(int value)
	{
		msecsPerFrame = 1000 / value;
	}

	// Gets or sets the collision bounding box offset
	public int Offset()
	{
		return bbOffset;
	}

	public void Offset(int value)
	{
		bbOffset = value;
		boundingBox.X((int)mPos.x + bbOffset);
		boundingBox.Y((int)mPos.y + bbOffset);
		boundingBox.W(mFrameWidth - bbOffset * 2); // shrink in from both sides
		boundingBox.H(mFrameHeight - bbOffset * 2);
	}


	/**
	 * Gets the current bounding box.
	 *
	 * @return the current bounding box.
	 */
	public Rectanglef BoundingBox()
	{
		return boundingBox;
	}

	public void BoundingBox(Rectanglef box)
	{
		boundingBox = box;
	}
}
