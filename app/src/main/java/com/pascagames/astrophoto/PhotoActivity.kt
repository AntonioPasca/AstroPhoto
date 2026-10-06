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
// Module:      PhotoActivity.kt
// --------------------------------------------------------------------------
package com.pascagames.astrophoto

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pascagames.astrophoto.ui.theme.AstroPhotoTheme

// --------------------------------------------------------------------------
// CLASS PhotoActivity
// --------------------------------------------------------------------------
class PhotoActivity : ComponentActivity() {

    private var backToCaller: (Unit) -> Unit = { back() }
    enum class AstroDestination { SingleShot, Stacking, Alignment, Library }

    // ----------------------------------------------------------------------
    // onCreate
    // ----------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val actions = listOf(
            TopBarAction.Settings { settings() }
        )

        setContent {
            AstroPhotoTheme {
                Scaffold(topBar = { TopBarEx("Photo", actions,backToCaller) }) {
                        innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    // onResume
    // ----------------------------------------------------------------------
    override fun onResume() {

        super.onResume()
    }

    // --------------------------------------------------------------------------
    // onDestroy
    // --------------------------------------------------------------------------
    override fun onDestroy() {

        super.onDestroy()
    }

    // --------------------------------------------------------------------------
    // back
    // --------------------------------------------------------------------------
    fun back() {
        finish()
    }

    // ----------------------------------------------------------------------
    // settings
    // ----------------------------------------------------------------------
    fun settings() {

        val intent = Intent(this@PhotoActivity, SettingsActivity::class.java)
        val bundle = Bundle()
        bundle.putInt("SETTINGS_INDEX", SETTINGS_PHOTO_INDEX)
        intent.putExtra("activity_data", bundle)
        startActivity(intent)
    }

    // ----------------------------------------------------------------------
    // MainScreen
    // ----------------------------------------------------------------------
    @Composable
    fun MainScreen(modifier: Modifier = Modifier) {

        // 1. ViewModel
        val viewModel: CameraViewModel = viewModel()
        val cameraState = viewModel.cameraState

        // 2. Controller Camera2
        val context = LocalContext.current
        val photoController = remember { PhotoController(context) }

        // 3. Link controller → ViewModel + start preview
        LaunchedEffect(Unit) {
            viewModel.attachPhotoController(photoController)
            viewModel.startPreview()
        }

        // 4. Main UI
        PhotoScreen(
            cameraState = cameraState,
            onCapture = { viewModel.capturePhoto() },
            onNavigate = { /* TODO navigation */ },
            photoController = photoController
        )
    }

    // ----------------------------------------------------------------------
    // PhotoScreen
    // ----------------------------------------------------------------------
    @Composable
    fun PhotoScreen(
        cameraState: CameraState,
        onCapture: () -> Unit,
        onNavigate: (AstroDestination) -> Unit,
        photoController: PhotoController
    ) {
        AstroTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {

                // Camera Preview
                CameraPreview(controller = photoController)

                // Capture Button
                CaptureButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onCapture = onCapture
                )

                // Bottom Bar
                PhotoBottomBar(
                    onCapture
                )
            }
        }
    }

    // ----------------------------------------------------------------------
    // CameraPreview
    // ----------------------------------------------------------------------
    @Composable
    fun CameraPreview(controller: PhotoController) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    controller.textureView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Log.v(TAG, "CamPreview")
    }

    // ----------------------------------------------------------------------
    // PhotoBottomBar
    // ----------------------------------------------------------------------
    @Composable
    fun PhotoBottomBar(
        onPhoto: () -> Unit) {

        NavigationBar {

            NavigationBarItem(
                selected = true,
                onClick = onPhoto,
                icon = {
                    Icon(
                        painterResource(id = R.drawable.photo),
                        contentDescription = null
                    )
                },
                label = { Text("Single Photo") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Green,
                    unselectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }

    // ----------------------------------------------------------------------
    // CaptureButton
    // ----------------------------------------------------------------------
    @Composable
    fun CaptureButton(
        modifier: Modifier = Modifier,
        onCapture: () -> Unit
    ) {
        Box(modifier = modifier.padding(bottom = 80.dp)) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .clickable(onClick = onCapture),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}


class FocusPeakingView(context: Context) : View(context) {
    var edges: Bitmap? = null

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        edges?.let {
            canvas.drawBitmap(it, null, Rect(0, 0, width, height), null)
        }
    }
}

sealed class CameraState {
    object Idle : CameraState()
    object Opening : CameraState()
    object Preview : CameraState()
    object Capturing : CameraState()
    data class Error(val message: String) : CameraState()
}

