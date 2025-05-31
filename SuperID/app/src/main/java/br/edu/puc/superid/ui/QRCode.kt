package br.edu.puc.superid.ui

import android.Manifest
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import br.edu.puc.superid.ui.theme.SuperIDTheme
import br.edu.puc.superid.ui.scannerConfig.WithPermission
import br.edu.puc.superid.ui.scannerConfig.scanBarcodes

@Composable
fun a (navController: NavController){

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        WithPermission(
            modifier = Modifier.padding(innerPadding),
            permission = Manifest.permission.CAMERA
        ) {
        CameraAppScreen()
        }
    }
}


@Composable
fun CameraAppScreen() {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }

    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }


    Box {
        CameraPreview(
            lensFacing = lensFacing,
            zoomLevel = 0f,
            imageCaptureUseCase = imageCaptureUseCase
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Row {
                IconButton (onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                }) {

                    Icon(Icons.Default.Cameraswitch,contentDescription = "Trocar a camera")
                }
            }



        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    zoomLevel: Float,
    imageCaptureUseCase: ImageCapture
) {
    val previewUseCase = remember { androidx.camera.core.Preview.Builder().build() }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    val localContext = LocalContext.current

    fun rebindCameraProvider() {
        cameraProvider?.let { cameraProvider ->
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                localContext as LifecycleOwner,
                cameraSelector,
                previewUseCase, imageCaptureUseCase
            )
            cameraControl = camera.cameraControl
        }
    }

    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider.awaitInstance(localContext)
        rebindCameraProvider()
    }

    LaunchedEffect(lensFacing) {
        rebindCameraProvider()
    }

    LaunchedEffect(zoomLevel) {
        cameraControl?.setLinearZoom(zoomLevel)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).also {
                previewUseCase.surfaceProvider = it.surfaceProvider
                rebindCameraProvider()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    SuperIDTheme {
        CameraAppScreen()
    }
}


