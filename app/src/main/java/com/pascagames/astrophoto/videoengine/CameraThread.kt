// --------------------------------------------------------------------------
// Language:    Kotlin
//
// Framework:   Google Jetpack Compose
//
// Package:     com.pascagames.astrophoto
//
// Author:      Antonio Pascarella
//
// Version:     Rel. 0.2.0
//
// Date:        September 2026
//
// Module:      CameraThread.kt
// --------------------------------------------------------------------------
// Public Methods
//      fun start()
//      fun stop()
// --------------------------------------------------------------------------
package com.pascagames.astrophoto.videoengine

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

// ----------------------------------------------------------------------------
// class CameraThread
// ----------------------------------------------------------------------------
class CameraThread(private val threadName: String) {

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    val handlerSafe: Handler
        get() = handler ?: throw IllegalStateException("CameraThread not started")

    // ----------------------------------------------------------------------------
    // start
    // ----------------------------------------------------------------------------
    fun start() {

        if (handlerThread != null) return  // started already

        handlerThread = HandlerThread(threadName).apply { start() }
        handler = Handler(handlerThread!!.looper)
    }

    // ----------------------------------------------------------------------------
    // stop
    // ----------------------------------------------------------------------------
    fun stop() {

        handlerThread?.quitSafely()
        handlerThread = null
        handler = null
    }
}
