// SandboxActivity.kt
// Main activity for simple, vaguely game-like app to test framework.
//
// Copyright © 2017 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE.md. If not, see:
// <https://github.com/IonoclastBrigham/RogueOp/blob/master/LICENSE.md>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package com.ionoclast.rogue_opcode.sandbox

import rogue_opcode.*
import rogue_opcode.geometrics.XYZf
import java.util.*


class SandboxActivity : GameProc() {
	private lateinit var mAudio: AudioResource

	private var mBoopers = ArrayList<ScreenElement>()

	inner class BooperSpawner : ActionElement() {
		override fun Update() {
			if(touchState.Is(TouchState.SINGLE_TAP)) {
				Spawn(touchState.GetMainX().toInt(),
				      touchState.GetMainY().toInt())
			}
		}

		fun Spawn(pXPos: Int, pYPos: Int) {
			ScreenElement(R.mipmap.ic_launcher_round, pXPos, pYPos).run {
				mVel = XYZf(rand(), rand())
				onPostUpdate = {
					if (mPos.x < 0f || mPos.x > AnimatedView.sOnly.ScreenWidth()) {
						mVel.x = -mVel.x
						mAudio.Play()
					}
					if (mPos.y < 0f || mPos.y > AnimatedView.sOnly.ScreenHeight()) {
						mVel.y = -mVel.y
						mAudio.Play()
					}
				}
				Wake()
				mBoopers.add(this)
			}
		}

		private fun rand() = ((Math.random() * 50) - 25).toFloat()
	}

	override fun InitializeOnce() {
		mAudio = AudioResource.ICanHas(R.raw.impact, AudioResource.AudioType.EFFECT)
		BooperSpawner().run {
			Spawn(100, 100)
			Active(true)
		}
	}

	override fun InitializeOnResume() {
		//
	}

	override fun Shutdown() {
		mAudio.Stop()
	}
}
