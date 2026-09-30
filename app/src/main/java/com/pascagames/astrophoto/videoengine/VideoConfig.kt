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
// Module:      VideoConfig.kt
// --------------------------------------------------------------------------
package com.pascagames.astrophoto.videoengine

import android.graphics.ImageFormat
import android.hardware.camera2.CaptureRequest
import android.util.Size

// ----------------------------------------------------------------------------
// class VideoConfig
// ----------------------------------------------------------------------------
data class VideoConfig(

    val cameraId: String,
    val resolution: Size,
    val format: Int = ImageFormat.YUV_420_888,

    // Sensor
    val fps: Int = 30,
    val iso: Int = 800,
    val shutterNs: Long = 10_000_000L, // 10 ms

    // Focus
    val focusMode: Int = CaptureRequest.CONTROL_AF_MODE_OFF,
    val focusDistance: Float = 0f, // infinito

    // Stabilization
    val opticalStabilization: Int = CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF,
    val videoStabilization: Int = CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF,

    // Lock
    val aeLock: Boolean = true,
    val awbLock: Boolean = true
)

