package com.pillbox.laporbox.presentation.ui.screens.lapor

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun LaporScreen(
    navController: NavController,
    viewModel: LaporViewModel = koinViewModel(),
    resepId: String
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val imageCapture = remember { ImageCapture.Builder().build() }
    var hasCamPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(state.status) {
        when (state.status) {
            LaporStatus.QUEUED -> {
                Toast.makeText(context, "Laporan berhasil divalidasi dan akan diunggah.", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
                viewModel.resetStatus()
            }
            LaporStatus.VALIDATION_FAILED, LaporStatus.ERROR -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetStatus()
            }
            else -> {}
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (state.status) {
                LaporStatus.PHOTO_TAKEN, LaporStatus.VALIDATING -> {
                    ImagePreview(
                        imagePath = state.imagePath!!,
                        isLoading = state.status == LaporStatus.VALIDATING,
                        onKirimClick = { viewModel.validateAndQueueUpload(resepId) },
                        onUlangiClick = { viewModel.retakePhoto() }
                    )
                }
                else -> {
                    if (hasCamPermission) {
                        CameraView(
                            imageCapture = imageCapture,
                            onTakePhoto = {
                                viewModel.takePhoto(imageCapture, ContextCompat.getMainExecutor(context))
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Izin kamera diperlukan untuk fitur ini.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraView(imageCapture: ImageCapture, onTakePhoto: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(modifier = Modifier.fillMaxSize(), imageCapture = imageCapture)
        Button(
            onClick = onTakePhoto,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Default.Camera, contentDescription = "Ambil Foto")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ambil Foto Laporan")
        }
    }
}

@Composable
fun ImagePreview(
    imagePath: String,
    isLoading: Boolean,
    onKirimClick: () -> Unit,
    onUlangiClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = rememberAsyncImagePainter(File(imagePath)),
            contentDescription = "Preview Laporan",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = onUlangiClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Ulangi")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ulangi")
                }
                Button(onClick = onKirimClick) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim")
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Gagal bind lifecycle", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}