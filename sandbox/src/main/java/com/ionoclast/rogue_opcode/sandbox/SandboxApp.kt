package com.ionoclast.rogue_opcode.sandbox

import android.app.Application
import android.os.StrictMode
import com.squareup.leakcanary.LeakCanary


/**
 * @author btoskin &lt;brigham@ionoclast.com&gt; Ionoclast Laboratories, LLC.
 */
class SandboxApp : Application() {
	override fun onCreate() {
		super.onCreate()

		if(BuildConfig.DEBUG) {
			if(LeakCanary.isInAnalyzerProcess(this)) return

			LeakCanary.install(this)
			StrictMode.enableDefaults()
		}
	}
}