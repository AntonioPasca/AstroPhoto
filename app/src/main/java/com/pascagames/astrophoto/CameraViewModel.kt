package com.pascagames.astrophoto

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    // State for UI
    var cameraState by mutableStateOf<CameraState>(CameraState.Idle)
        private set

    // Controller Camera2 (lo collegheremo dopo)
    private var photoController: PhotoController? = null
    private var videoController: VideoController? = null

    fun attachPhotoController(controller: PhotoController) {
        photoController = controller
    }

    fun attachVideoController(controller: VideoController) {
        videoController = controller
    }

    // --- Preview -------------------------------------------------------------

    fun startPreview() {
        cameraState = CameraState.Opening

        try {
            photoController?.startPreview()
            cameraState = CameraState.Preview
        } catch (e: Exception) {
            cameraState = CameraState.Error(e.message ?: "Errore preview")
        }
    }

    // --- Photo ----------------------------------------------------------------

    fun capturePhoto() {
        cameraState = CameraState.Capturing

        viewModelScope.launch {
            try {
                photoController?.capture()
                cameraState = CameraState.Preview
            } catch (e: Exception) {
                cameraState = CameraState.Error(e.message ?: "Errore scatto")
            }
        }
    }

    // --- Video ---------------------------------------------------------------

    fun startVideo() {
        cameraState = CameraState.Capturing

        try {
            videoController?.startRecording()
            cameraState = CameraState.Preview
        } catch (e: Exception) {
            cameraState = CameraState.Error(e.message ?: "Errore video")
        }
    }

    fun stopVideo() {
        try {
            videoController?.stopRecording()
            cameraState = CameraState.Preview
        } catch (e: Exception) {
            cameraState = CameraState.Error(e.message ?: "Errore stop video")
        }
    }

    // --- Error ---------------------------------------------------------------

    fun setError(msg: String) {
        cameraState = CameraState.Error(msg)
    }
}
