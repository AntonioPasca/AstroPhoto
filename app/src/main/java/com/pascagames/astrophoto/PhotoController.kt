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
// Date:        October 2026
//
// Module:      VideoConfig.kt
// --------------------------------------------------------------------------
package com.pascagames.astrophoto

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat

// -------------------------------------------------------------------------
// PhotoController
// -------------------------------------------------------------------------
class PhotoController(
    private val context: Context,
    private val cameraId: String = "0"   // back camera
) {
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private lateinit var previewSurface: Surface
    private lateinit var imageReader: ImageReader

    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    // TextureView for preview
    var textureView: TextureView? = null

    // -------------------------------------------------------------------------
    // startPreview
    // -------------------------------------------------------------------------
    fun startPreview() {

        val tv = textureView ?: throw IllegalStateException("TextureView not set")

        if (!tv.isAvailable) {
            tv.surfaceTextureListener = surfaceListener
        } else {
            openCamera()
        }
    }

    // -------------------------------------------------------------------------
    // capture
    // -------------------------------------------------------------------------
    fun capture(onSaved: (ByteArray) -> Unit = {}) {
        val device = cameraDevice ?: return

        val captureBuilder =
            device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(imageReader.surface)
                set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            image.close()
            onSaved(bytes)
        }, null)

        captureSession?.capture(captureBuilder.build(), null, null)
    }

    // Private

    // -------------------------------------------------------------------------
    // openCamera
    // -------------------------------------------------------------------------
    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Camera permission not granted")
        }

        cameraManager.openCamera(cameraId, cameraStateCallback, null)
    }

    // -------------------------------------------------------------------------
    // cameraStateCallback
    // -------------------------------------------------------------------------
    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(device: CameraDevice) {
            cameraDevice = device
            createPreviewSession()
        }

        override fun onDisconnected(device: CameraDevice) {
            device.close()
            cameraDevice = null
        }

        override fun onError(device: CameraDevice, error: Int) {
            device.close()
            cameraDevice = null
        }
    }

    // -------------------------------------------------------------------------
    // createPreviewSession
    // -------------------------------------------------------------------------
    private fun createPreviewSession() {
        val tv = textureView ?: return
        val texture = tv.surfaceTexture ?: return

        texture.setDefaultBufferSize(tv.width, tv.height)
        previewSurface = Surface(texture)

        // ImageReader for Jpeg shots
        imageReader = ImageReader.newInstance(
            tv.width,
            tv.height,
            ImageFormat.JPEG,
            2
        )

        previewRequestBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(previewSurface)
            }

        cameraDevice!!.createCaptureSession(
            listOf(previewSurface, imageReader.surface),
            sessionCallback,
            null
        )
    }

    // -------------------------------------------------------------------------
    // surfaceListener
    // -------------------------------------------------------------------------
    private val surfaceListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean = true
        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
    }

    // -------------------------------------------------------------------------
    // sessionCallback
    // -------------------------------------------------------------------------
    private val sessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            captureSession = session

            val request = previewRequestBuilder!!.build()
            session.setRepeatingRequest(request, null, null)
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {}
    }
}
