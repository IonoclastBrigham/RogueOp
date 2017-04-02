// TextSE.kt
// Text-centric ScreenElement subclass.
//
// Copyright © 2012-2017 Christopher R. Tooley
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode

import android.graphics.*
import android.text.DynamicLayout
import android.text.Layout
import android.text.TextPaint
import rogue_opcode.geometrics.XYf


class TextSE(pText: String, pX: Float, pY: Float, pWidth: Int, pHeight: Int = -1)
	: ScreenElement(0) {

	companion object {
		private val serialVersionUID = 1L

		private var sLabelTextPaint = TextPaint().apply {
			color = Color.argb(0xff, 0x8a, 0xe2, 0x34)
			isAntiAlias = true
			textSize = 16f
			textSkewX = -.25f
			textAlign = Paint.Align.LEFT
		}
		private var sSmallTextPaint = TextPaint().apply {
			//TODO - should be a call from outside of rogue-op
			typeface = Typeface.createFromAsset(GameProc.sOnly.assets,
			                                    "fonts/aispec.ttf")
			color = Color.BLACK
			alpha = 0xff
			isAntiAlias = true
			textSize = 16f
			textAlign = Paint.Align.LEFT
		}
		private var sFillPaint = TextPaint().apply {
			color = Color.GRAY
			alpha = 0xbb
			isAntiAlias = true
			shader = LinearGradient(0f, 0f, 0f, 20f,
			                        Color.WHITE, Color.GRAY,
			                        Shader.TileMode.CLAMP)
		}
		private var sTextMargin = 10
	}

	private lateinit var mTextDL: DynamicLayout
	private var mTitle: String? = null
	private var mWidth: Int = 0 //overall width - includes icon width
	private var mIconWidth: Int = 0
	private var mHeight: Int = 0
	private var mTextBottomMargin: Int = 0
	private var mFixedHeight: Boolean = false
	private lateinit var mDisplayRect: RectF
	private lateinit var mTextClipRect: RectF

	private var mInEditMode: Boolean = false
	private var mEditable: Boolean = false
	private var mDirtyFlag: Boolean = false //set only if USER edited the text

	init {
		mHeight = pHeight
		mFixedHeight = (mHeight != -1)

		mWidth = pWidth
		mIconWidth = 0
		mTextBottomMargin = 0

		mPos.x = pX
		mPos.y = pY

		mTextPaint = sSmallTextPaint

		set_text(pText)

		mInEditMode = false
		mEditable = false
		mDirtyFlag = false

		ZDepth(100f)
	}

	fun Dirty(): Boolean {
		val tDirty = mDirtyFlag
		Dirty(false)

		return tDirty
	}

	internal fun Dirty(pDirty: Boolean) {
		mDirtyFlag = pDirty
	}

	//Virtual width (not Physical width)
	override fun Width(): Int {
		return mWidth
	}

	override fun Height(): Int {
		return mHeight
	}

	fun AddIcon(pGRID: Int, pIconWidth: Int) {
		mIconWidth = pIconWidth
		SetCurrentGR(pGRID)
		set_text(mText ?: "")
	}

	fun SetTextBottomMargin(pMargin: Int) {
		mTextBottomMargin = pMargin
		set_text(mText ?: "")
	}

	override fun Text(pText: String) {
		set_text(pText)
	}

	fun Title() = mTitle ?: ""

	fun Title(pTitle: String) {
		mTitle = pTitle
	}

	fun SetTextPaint(pTextPaint: TextPaint) {
		mTextPaint = pTextPaint

		set_text(mText ?: "")
	}

	private fun set_text(pText: String) {
		mText = pText

		try {
			mTextDL = DynamicLayout(mText, mTextPaint as TextPaint,
					mWidth - sTextMargin * 2 - (sTextMargin + mIconWidth),
					Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
		} catch(e: Exception) {
			//The text and icon don't fit into this mWidth
		}

		if(!mFixedHeight) {
			mHeight = mTextDL.getLineTop(mTextDL.lineCount) + sTextMargin * 2

			if(mHeight < sTextMargin * 2 + mIconWidth)
				mHeight = sTextMargin * 2 + mIconWidth
		}

		mDisplayRect = RectF(0f, 0f, mWidth.toFloat(), mHeight.toFloat())
		mTextClipRect = RectF(0f, 0f, mWidth.toFloat(), (mHeight - mTextBottomMargin).toFloat())
	}

	fun WithinRange(pPoint: XYf): Boolean {
		return pPoint.x > mPos.x && pPoint.x < mPos.x + mWidth &&
				pPoint.y > mPos.y && pPoint.y < mPos.y + mHeight
	}

	fun Editable(pEditable: Boolean) {
		mEditable = pEditable
	}

	override fun Visible(pVisible: Boolean) {
		super.Visible(pVisible)

		if(mInEditMode) {
			GameProc.sOnly.HideTextEditor(this)
			mInEditMode = false
		}
	}

	override fun Update() {
		if(!mEditable) return

		val tProc = GameProc.sOnly
		val tTouch = tProc.mTouchState
		try {
			if(mInEditMode) {
				if(tProc.mEditTextParams?.mCurrentTE === this) {
					set_text(tProc.mEditTextParams?.mEdit?.text.toString())
				}

				if(tTouch.Is(TouchState.SINGLE_TAP)) {
					if(!WithinRange(tTouch.MainTouchPos())) {
						mInEditMode = false
						Dirty(true)
						tProc.HideTextEditor(this)
					}
				}
			} else if(tTouch.Is(TouchState.SINGLE_TAP) && Visible()) {
				if (WithinRange(tTouch.MainTouchPos())) {
					mInEditMode = true
					tProc.ShowTextEditor(this, mPos, mWidth, mHeight)
				}
			}
		} catch(e: Exception) {
		}
	}


	override fun Draw() {
		val tCanvas = AnimatedView.sCurrentCanvas
		val tX = mPos.x// * AnimatedView.sOnly.mPreScaler;
		val tY = mPos.y// * AnimatedView.sOnly.mPreScaler;

		tCanvas!!.save()
		tCanvas.scale(AnimatedView.sOnly.mPreScaler,
				AnimatedView.sOnly.mPreScaler)

		tCanvas.translate(tX, tY)
		tCanvas.drawRoundRect(mDisplayRect, 10f, 10f, sFillPaint!!)
		tCanvas.drawText(Title(), 10f, -2f, sLabelTextPaint!!)
		tCanvas.translate((sTextMargin + sTextMargin + mIconWidth).toFloat(), sTextMargin.toFloat())
		tCanvas.clipRect(mTextClipRect, Region.Op.REPLACE)
		mTextDL.draw(tCanvas)
		tCanvas.restore()

		tCanvas.save()
		val tIconOffset = (AnimatedView.sOnly.mPreScaler * (mIconWidth / 2 + sTextMargin)).toInt()
		tCanvas.translate(tIconOffset.toFloat(), tIconOffset.toFloat())
		super.Draw()
		tCanvas.restore()
	}
}
