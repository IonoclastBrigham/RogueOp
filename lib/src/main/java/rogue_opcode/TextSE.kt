// TextSE.kt
// Text-centric ScreenElement subclass.
//
// Copyright © 2012-2017 Christopher R. Tooley
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <https://github.com/IonoclastBrigham/RogueOp/blob/master/LICENSE.md>
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

	override var Visible = false
		set(pVisible) {
			super.Visible = pVisible

			if(mInEditMode) {
				GameProc.sOnly.HideTextEditor(this)
				mInEditMode = false
			}
		}

	override fun Update() {
		if(!mEditable) return

		val tProc = GameProc.sOnly
		if(mInEditMode) {
			if(tProc.mEditTextParams?.mCurrentTE === this) {
				set_text(tProc.mEditTextParams?.mEdit?.text.toString())
			}

			if(TouchState.Is(TouchState.SINGLE_TAP)) {
				if(!WithinRange(TouchState.MainTouchPos())) {
					mInEditMode = false
					Dirty(true)
					tProc.HideTextEditor(this)
				}
			}
		} else if(TouchState.Is(TouchState.SINGLE_TAP) && Visible) {
			if (WithinRange(TouchState.MainTouchPos())) {
				mInEditMode = true
				tProc.ShowTextEditor(this, mPos, mWidth, mHeight)
			}
		}
	}


	override fun Draw(pCanvas: Canvas, pPreScaler: Float, pDefaultPaint: Paint) {
		val tX = mPos.x// * pPreScaler;
		val tY = mPos.y// * pPreScaler;

		pCanvas.save()
		pCanvas.scale(pPreScaler, pPreScaler)

		pCanvas.translate(tX, tY)
		pCanvas.drawRoundRect(mDisplayRect, 10f, 10f, sFillPaint)
		pCanvas.drawText(Title(), 10f, -2f, sLabelTextPaint)
		pCanvas.translate((sTextMargin + sTextMargin + mIconWidth).toFloat(),
		                  sTextMargin.toFloat())
		pCanvas.clipRect(mTextClipRect, Region.Op.REPLACE)
		mTextDL.draw(pCanvas)
		pCanvas.restore()

		pCanvas.save()
		val tIconOffset = (pPreScaler * (mIconWidth / 2 + sTextMargin)).toInt()
		pCanvas.translate(tIconOffset.toFloat(), tIconOffset.toFloat())
		super.Draw(pCanvas, pPreScaler, pDefaultPaint)
		pCanvas.restore()
	}
}
