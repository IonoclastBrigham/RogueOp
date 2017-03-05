// GraphicElement.java
// Represents a loaded graphics resource (image)
//
// Copyright © 2010-2017 Christopher R. Tooley, Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;


public class GraphicResource implements Serializable
{
	private static final long serialVersionUID = 7533930775077206592L;

	public static Map<Integer, GraphicResource> sAllGRs = new HashMap<Integer, GraphicResource>();

	protected static int sUID = 1000;

	public Bitmap mImage;
	public int mResID;
	public Canvas mBitmapCanvas;

	// these are the original/logical size of the image
	protected int mBaseWidth;
	protected int mBaseHeight;

	public static BitmapFactory.Options sBitmapOptions;
	static
	{
		sBitmapOptions = new BitmapFactory.Options();
		//sBitmapOptions.inPreferredConfig = Bitmap.Config.ALPHA_8;
		sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		sBitmapOptions.inScaled = false;
	}

	//Find the GR with this res id and set its mImage bitmap to null
	public static void Unload(int pResourceID) {
		GraphicResource tGR;
		
		tGR = sAllGRs.get(pResourceID);
		if ((tGR != null) && (tGR.mImage != null)) {
			tGR.mImage.recycle();
			tGR.mImage = null;
		}
	}

	public static void Load(int pResourceID) {
		GraphicResource tGR;
		
		tGR = sAllGRs.get(pResourceID);
		if (tGR != null) {
			if (tGR.mImage != null) {
				tGR.mImage.recycle();
				tGR.mImage = null;
			}
			
			tGR.internalLoad(pResourceID);
		} else {
			new GraphicResource(pResourceID);
		}
	}
	
	//find a GR on disk and load it in - return the resid
	public static int Load(String pFilePath) {
		GraphicResource tGR;
		
		tGR = sAllGRs.get(pFilePath.hashCode());
		if (tGR != null) {
			if (tGR.mImage != null) {
				tGR.mImage.recycle();
				tGR.mImage = null;
			}
			
			tGR.internalLoad(pFilePath);
		} else {
			tGR = new GraphicResource(pFilePath);
		}
		
		return tGR.mResID;
	}
	

	// c'tor ///////////////////////////////////////////////////////////////////


	/**
	 * Construct a {@code GraphicResource} and load the specified image
	 * resource.
	 * 
	 * @param pResID the image's resource ID.
	 */
	public GraphicResource(int pResID)
	{
		mResID = pResID;
		mBitmapCanvas = null;
		internalLoad(pResID);
	}

	public GraphicResource(String pFilePath)
	{
		mResID = pFilePath.hashCode();
		mBitmapCanvas = null;
		internalLoad(pFilePath);
	}

	/**
	 * Construct a {@code GraphicResource} from an existing bitmap.
	 * 
	 * @param pBitmap the Bitmap to clone.
	 */
	public GraphicResource(Bitmap pBitmap)
	{
		mResID = sUID;
		mBitmapCanvas = null;
		mImage = Bitmap.createBitmap(pBitmap);
		mBaseWidth = mImage.getWidth();
		mBaseHeight = mImage.getHeight();
		mImage = Bitmap.createScaledBitmap(mImage,
				(int)(mBaseWidth * AnimatedView.sOnly.mPreScaler),
				(int)(mBaseHeight * AnimatedView.sOnly.mPreScaler), true);
		sAllGRs.put(mResID, this);
		sUID ++;
	}
	
	public GraphicResource(int pWidth, int pHeight) {
		mResID = sUID;
		mImage = Bitmap.createBitmap(pWidth, pHeight, sBitmapOptions.inPreferredConfig);
		mBaseWidth = mImage.getWidth();
		mBaseHeight = mImage.getHeight();
		//TODO - logical width and height not working with mutable GRs
		sAllGRs.put(mResID, this);

		mBitmapCanvas = new Canvas(mImage);
		sUID ++;
	}
	
	private void internalLoad(int pResourceID)
	{
		// load image
		try {
			Resources r = GameProc.sOnly.getResources();
			Bitmap tImage = BitmapFactory.decodeResource(r, pResourceID, sBitmapOptions);
			mBaseWidth = tImage.getWidth();
			mBaseHeight = tImage.getHeight();
	
			//scale image
			if(AnimatedView.sOnly.mPreScaler != 1.0f)
			{
				mImage = Bitmap.createScaledBitmap(tImage,
						(int)(mBaseWidth * AnimatedView.sOnly.mPreScaler),
						(int)(mBaseHeight * AnimatedView.sOnly.mPreScaler), true);
				tImage.recycle();
				tImage = null;
			} else
				mImage = tImage;
	
			// store image
			sAllGRs.put(pResourceID, this);
		} catch (OutOfMemoryError e) {
			//TODO -dOOM!
			//when this error happens we will no longer crash the app - the particular GR will just
			//not be visible.
		}
	}

	private void internalLoad(String pFilePath)
	{
		// load image
		try {
			Bitmap tImage = BitmapFactory.decodeFile(pFilePath, sBitmapOptions);
			
			if (tImage == null)
				throw new OutOfMemoryError("tImage is null.");
			
			mBaseWidth = tImage.getWidth();
			mBaseHeight = tImage.getHeight();
	
			//scale image
			if(AnimatedView.sOnly.mPreScaler != 1.0f)
			{
				mImage = Bitmap.createScaledBitmap(tImage,
						(int)(mBaseWidth * AnimatedView.sOnly.mPreScaler),
						(int)(mBaseHeight * AnimatedView.sOnly.mPreScaler), true);
				tImage.recycle();
				tImage = null;
			} else
				mImage = tImage;
	
			// store image
			sAllGRs.put(pFilePath.hashCode(), this);
		} catch (OutOfMemoryError e) {
			//TODO -dOOM!
			//when this error happens we will no longer crash the app - the particular GR will just
			//not be visible.
		}
	}

	// resource management /////////////////////////////////////////////////////

	public boolean Valid()
	{
		return mImage != null;
	}

	// image properties ////////////////////////////////////////////////////////

	// Returns the width and height in VIRTUAL units (the units used when
	// placing objects on the screen)
	public int VirtualWidth()
	{
		return mBaseWidth;
	}

	public int VirtualHeight()
	{
		return mBaseHeight;
	}

	public int PhysicalWidth()
	{
		if (Valid())
			return mImage.getWidth();
		
		return 0;
	}

	public int PhysicalHeight()
	{
		if (Valid())
			return mImage.getHeight();
		
		return 0;
	}

	// static access ///////////////////////////////////////////////////////////

	public static GraphicResource FindGR(int pResourceID)
	{
		GraphicResource tGR = sAllGRs.get(pResourceID);
		//TODO - hmmm, this makes it difficult to manage memory but it is a nice feature...
		//return (tGR != null ? tGR : new GraphicResource(pResourceID));
		return tGR;
	}

	// serialization protocol //////////////////////////////////////////////////

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeInt(mResID);
	}

	private void readObject(ObjectInputStream pIn) throws IOException,
			ClassNotFoundException
	{
		pIn.defaultReadObject(); // read everything except the Bitmap field
		internalLoad(mResID); // loads the image resource
	}
}
	