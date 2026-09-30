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
// Module:      VideoEngine.kt
// --------------------------------------------------------------------------
// Public Methods
//      fun start()
//      fun stop()
// --------------------------------------------------------------------------
package com.pascagames.astrophoto.videoengine

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.util.Range

// --------------------------------------------------------------------------------------------
// VideoEngine
// --------------------------------------------------------------------------------------------
class VideoEngine(
    private val context: Context,
    private val config: VideoConfig,
    private val callback: FrameCallback
) {

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    private val cameraThread = CameraThread("VideoEngineThread")

    // --------------------------------------------------------------------------------------------
    // start
    // --------------------------------------------------------------------------------------------
    fun start() {

        cameraThread.start()

        //val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val manager = context.getSystemService(CameraManager::class.java)

        manager.openCamera(
            config.cameraId,
            object : CameraDevice.StateCallback() {

                override fun onOpened(device: CameraDevice) {
                    cameraDevice = device
                    createSession()
                }

                override fun onDisconnected(device: CameraDevice) {
                    stop()
                }

                override fun onError(device: CameraDevice, error: Int) {
                    stop()
                }
            },
            cameraThread.handlerSafe
        )
    }



    // --------------------------------------------------------------------------------------------
    // stop
    // --------------------------------------------------------------------------------------------
    fun stop() {
        try {
            captureSession?.stopRepeating()
        } catch (_: Exception) {}

        try {
            captureSession?.abortCaptures()
        } catch (_: Exception) {}

        captureSession?.close()
        cameraDevice?.close()
        imageReader?.close()

        captureSession = null
        cameraDevice = null
        imageReader = null

        cameraThread.stop()
    }

    // Private methods

    // --------------------------------------------------------------------------------------------
    // createSession
    // --------------------------------------------------------------------------------------------
    private fun createSession() {
        val size = config.resolution

        imageReader = ImageReader.newInstance(
            size.width,
            size.height,
            config.format,
            16      // large buffer grande for astrophoto
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage() ?: return@setOnImageAvailableListener
            callback.onFrame(image, image.timestamp)
            image.close()
        }, cameraThread.handlerSafe)

        val surfaces = listOf(imageReader!!.surface)

        cameraDevice?.createCaptureSession(
            surfaces,
            object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    startCapture()
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    stop()
                }
            },
            cameraThread.handlerSafe
        )
    }

    // --------------------------------------------------------------------------------------------
    // startCapture
    // --------------------------------------------------------------------------------------------
    private fun startCapture() {

        val requestBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)

        requestBuilder.addTarget(imageReader!!.surface)

        // ISO manual
        requestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, config.iso)

        // shutter manual
        requestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, config.shutterNs)

        // focus manual infinito
        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, config.focusMode)
        requestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, config.focusDistance)

        // stabilization OFF
        requestBuilder.set(
            CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
            config.opticalStabilization
        )
        requestBuilder.set(
            CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
            config.videoStabilization
        )

        // AE/AWB lock
        requestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, config.aeLock)
        requestBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, config.awbLock)

        // FPS
        requestBuilder.set(
            CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
            Range(config.fps, config.fps)
        )

        val request = requestBuilder.build()

        captureSession?.setRepeatingRequest(
            request,
            null,
            cameraThread.handlerSafe
        )
    }
}


