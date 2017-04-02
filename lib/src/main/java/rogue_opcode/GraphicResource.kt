// GraphicElement.kt
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


package rogue_opcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*


class GraphicResource : Serializable {
	companion object {
		private const val serialVersionUID = 7533930775077206592L

		var sAllGRs: MutableMap<Int, GraphicResource> = HashMap()

		protected var sUID = 1000

		var sBitmapOptions = BitmapFactory.Options()

		init {
			sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
			sBitmapOptions.inScaled = false
		}

		//Find the GR with this res id and set its mImage bitmap to null
		fun Unload(pResourceID: Int) {
			val tGR: GraphicResource?

			tGR = sAllGRs[pResourceID]
			if(tGR != null && tGR.mImage != null) {
				tGR.mImage!!.recycle()
				tGR.mImage = null
			}
		}

		fun Load(pResourceID: Int) {
			val tGR: GraphicResource?

			tGR = sAllGRs[pResourceID]
			if(tGR != null) {
				if(tGR.mImage != null) {
					tGR.mImage!!.recycle()
					tGR.mImage = null
				}

				tGR.internalLoad(pResourceID)
			} else {
				GraphicResource(pResourceID)
			}
		}

		//find a GR on disk and load it in - return the resid
		fun Load(pFilePath: String): Int {
			var tGR: GraphicResource?

			tGR = sAllGRs[pFilePath.hashCode()]
			if(tGR != null) {
				if(tGR.mImage != null) {
					tGR.mImage!!.recycle()
					tGR.mImage = null
				}

				tGR.internalLoad(pFilePath)
			} else {
				tGR = GraphicResource(pFilePath)
			}

			return tGR.mResID
		}

		// static access ///////////////////////////////////////////////////////////

		fun FindGR(pResourceID: Int): GraphicResource? {
			val tGR = sAllGRs[pResourceID]
			//TODO - hmmm, this makes it difficult to manage memory but it is a nice feature...
			//return (tGR != null ? tGR : new GraphicResource(pResourceID));
			return tGR
		}
	}


	var mImage: Bitmap? = null
	var mResID = 0
	var mBitmapCanvas: Canvas? = null

	// these are the original/logical mSize of the image
	protected var mBaseWidth: Int = 0
	protected var mBaseHeight: Int = 0


	// c'tor ///////////////////////////////////////////////////////////////////


	/**
	 * Construct a `GraphicResource` and load the specified image
	 * resource.

	 * @param pResID the image's resource ID.
	 */
	constructor(pResID: Int) {
		mResID = pResID
		mBitmapCanvas = null
		internalLoad(pResID)
	}

	constructor(pFilePath: String) {
		mResID = pFilePath.hashCode()
		mBitmapCanvas = null
		internalLoad(pFilePath)
	}

	/**
	 * Construct a `GraphicResource` from an existing bitmap.

	 * @param pBitmap the Bitmap to clone.
	 */
	constructor(pBitmap: Bitmap) {
		mResID = sUID
		mBitmapCanvas = null
		mImage = Bitmap.createBitmap(pBitmap)
		mBaseWidth = mImage!!.width
		mBaseHeight = mImage!!.height
		mImage = Bitmap.createScaledBitmap(mImage,
				(mBaseWidth * AnimatedView.sOnly.mPreScaler).toInt(),
				(mBaseHeight * AnimatedView.sOnly.mPreScaler).toInt(), true)
		sAllGRs.put(mResID, this)
		sUID++
	}

	constructor(pWidth: Int, pHeight: Int) {
		mResID = sUID
		mImage = Bitmap.createBitmap(pWidth, pHeight, sBitmapOptions.inPreferredConfig)
		mBaseWidth = mImage!!.width
		mBaseHeight = mImage!!.height
		//TODO - logical width and height not working with mutable GRs
		sAllGRs.put(mResID, this)

		mBitmapCanvas = Canvas(mImage!!)
		sUID++
	}

	private fun internalLoad(pResourceID: Int) {
		// load image
		try {
			val r = GameProc.sOnly.resources
			var tImage: Bitmap? = BitmapFactory.decodeResource(r, pResourceID, sBitmapOptions)
			mBaseWidth = tImage!!.width
			mBaseHeight = tImage.height

			//scale image
			if(AnimatedView.sOnly.mPreScaler != 1.0f) {
				mImage = Bitmap.createScaledBitmap(tImage,
						(mBaseWidth * AnimatedView.sOnly.mPreScaler).toInt(),
						(mBaseHeight * AnimatedView.sOnly.mPreScaler).toInt(), true)
				tImage.recycle()
				tImage = null
			} else
				mImage = tImage

			// store image
			sAllGRs.put(pResourceID, this)
		} catch(e: OutOfMemoryError) {
			//TODO -dOOM!
			//when this error happens we will no longer crash the app - the particular GR will just
			//not be visible.
		}

	}

	private fun internalLoad(pFilePath: String) {
		// load image
		try {
			val tImage: Bitmap = BitmapFactory.decodeFile(pFilePath, sBitmapOptions) ?: throw OutOfMemoryError("tImage is null.")

			mBaseWidth = tImage.width
			mBaseHeight = tImage.height

			//scale image
			if(AnimatedView.sOnly.mPreScaler != 1.0f) {
				mImage = Bitmap.createScaledBitmap(tImage,
						(mBaseWidth * AnimatedView.sOnly.mPreScaler).toInt(),
						(mBaseHeight * AnimatedView.sOnly.mPreScaler).toInt(), true)
				tImage.recycle()
			} else {
				mImage = tImage
			}

			// store image
			sAllGRs.put(pFilePath.hashCode(), this)
		} catch(e: OutOfMemoryError) {
			//TODO -dOOM!
			//when this error happens we will no longer crash the app - the particular GR will just
			//not be visible.
		}
	}

	// resource management /////////////////////////////////////////////////////

	fun Valid(): Boolean {
		return mImage != null
	}

	// image properties ////////////////////////////////////////////////////////

	// Returns the width and height in VIRTUAL units (the units used when
	// placing objects on the screen)
	fun VirtualWidth(): Int {
		return mBaseWidth
	}

	fun VirtualHeight(): Int {
		return mBaseHeight
	}

	fun PhysicalWidth(): Int {
		if(Valid())
			return mImage!!.width

		return 0
	}

	fun PhysicalHeight(): Int {
		if(Valid())
			return mImage!!.height

		return 0
	}

	// serialization protocol //////////////////////////////////////////////////

	@Throws(IOException::class)
	private fun writeObject(out: ObjectOutputStream) {
		out.writeInt(mResID)
	}

	@Throws(IOException::class, ClassNotFoundException::class)
	private fun readObject(pIn: ObjectInputStream) {
		pIn.defaultReadObject() // read everything except the Bitmap field
		internalLoad(mResID) // loads the image resource
	}
}
	