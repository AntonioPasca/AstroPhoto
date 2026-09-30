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
// Module:      Utils.kt
// --------------------------------------------------------------------------
// Utils functions
//      fun beep(volume: Int, duration: Int)
//      fun createSessionDirectory(): File
//      fun formatTime(seconds: Int): String
//      fun getAudioPermission(): Boolean
//      fun getCameraPermission(): Boolean
// --------------------------------------------------------------------------
package com.pascagames.astrophoto

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
/*import android.os.Environment
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale*/

// ----------------------------------------------------------------------
// beep
// ----------------------------------------------------------------------
fun beep(volume: Int, duration: Int) {

    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, volume)
    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, duration)
}

// ----------------------------------------------------------------------
// createSessionDirectory
// ----------------------------------------------------------------------
// Creates a new folder in the format <Month><DD>_<HHMMSS> starting from
// Pictures/AstroPhoto
// Output
//      the created relative path
// ----------------------------------------------------------------------
/*fun createSessionDirectory(pathPrefix: String): File {

    val baseDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES
    )

    val astroDir = File(baseDir, "AstroPhoto")
    if (!astroDir.exists()) astroDir.mkdirs()

    val formatter = DateTimeFormatter.ofPattern("MMMdd_HHMMSS", Locale.US)
    val sessionName = pathPrefix + "_" + LocalDateTime.now().format(formatter)

    val sessionDir = File(astroDir, sessionName)
    if (!sessionDir.exists()) sessionDir.mkdirs()

    val relativePath = File(Settings.photoPath, sessionName)

    return relativePath
}*/

// ----------------------------------------------------------------------
// getAudioPermission
// ----------------------------------------------------------------------
@Composable
fun getAudioPermission(): Boolean {

    var result = false
    val context = LocalContext.current
    val permission = Manifest.permission.RECORD_AUDIO

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            result = true
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(permission)
        }
    }
    return result
}

// ----------------------------------------------------------------------
// getCameraPermission
// ----------------------------------------------------------------------
@Composable
fun getCameraPermission(): Boolean {

    var result = false
    val context = LocalContext.current
    val permission = Manifest.permission.CAMERA

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            result = true
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(permission)
        }
    }
    return result
}

// ----------------------------------------------------------------------
// formatTime
// ----------------------------------------------------------------------
@SuppressLint("DefaultLocale")
fun formatTime(seconds: Int): String {

    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
