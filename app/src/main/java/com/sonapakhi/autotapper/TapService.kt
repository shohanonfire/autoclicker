package com.sonapakhi.autotapper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class TapService : AccessibilityService() {

    companion object {
        private var instance: TapService? = null
        private val running = AtomicBoolean(false)
        private var worker: Thread? = null

        fun isEnabled(ctx: Context): Boolean {
            // Basic heuristic: if service instance is non-null, it's enabled
            return instance != null
        }

        fun startLoop(configJson: JSONObject, ctx: Context) {
            if (running.get()) return
            instance?.let { svc ->
                running.set(true)
                worker = thread(name = "TapRunner") {
                    try {
                        val cycles = configJson.optInt("numberOfCycles", 1)
                        val targets = configJson.getJSONArray("targets")
                        for (c in 0 until cycles) {
                            if (!running.get()) break
                            for (i in 0 until targets.length()) {
                                if (!running.get()) break
                                val t = targets.getJSONObject(i)
                                val x = t.getInt("xPos")
                                val y = t.getInt("yPos")
                                val delay = t.optLong("delayValue", 0L)
                                svc.tap(x, y)
                                if (delay > 0) Thread.sleep(delay)
                            }
                        }
                    } catch (_: Exception) {}
                    running.set(false)
                }
            }
        }

        fun stopLoop() {
            running.set(false)
            worker?.interrupt()
            worker = null
        }
    }

    override fun onServiceConnected() {
        instance = this
        super.onServiceConnected()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    private fun tap(x: Int, y: Int) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 1)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
}
