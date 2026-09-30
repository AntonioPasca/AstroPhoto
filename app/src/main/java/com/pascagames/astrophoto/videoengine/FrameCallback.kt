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
// Module:      FrameCallback.kt
// --------------------------------------------------------------------------
// Public Methods
//      fun start()
//      fun stop()
// --------------------------------------------------------------------------
package com.pascagames.astrophoto.videoengine

import android.media.Image

interface FrameCallback {
    fun onFrame(image: Image, timestampNs: Long)
}
