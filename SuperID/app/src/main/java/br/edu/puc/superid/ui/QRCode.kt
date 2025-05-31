package br.edu.puc.superid.ui

import android.Manifest
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import br.edu.puc.superid.R
import br.edu.puc.superid.ui.scannerConfig.WithPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

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


@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraAppScreen() {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }
    var scannedValue by remember { mutableStateOf<String?>(null) }
    var flashligt by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showScreen1 by remember { mutableStateOf(true) }

    val firestore = remember { FirebaseFirestore.getInstance() }
    val conta = FirebaseAuth.getInstance().currentUser?: return

    /*fun fire(qrcode:String){
       val qrc = firestore.collection("logins").document(qrcode)
            qrc.get().addOnSuccessListener { document ->
                if(document.data != null) {
                    val api = document.data!!.get("API")
                    showScreen1 = false
                    return@addOnSuccessListener api
                }
            }.addOnFailureListener {
                    return@addOnFailureListener
            }
    }
    fun res(res:String,qrcode:String){
        val qrC = firestore.collection("logins").document(qrcode)
        if (res == "yes") {
            val uid = conta.uid
            qrC.update("UserID", uid)
        } else {
            qrC.update(qrcode, FieldValue.increment(-1))
        }
    }*/


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
                .align(Alignment.Center)
                .background(Color.Black)
        ) {
            // Top blue header with logo
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .background(Color(0xFF72CEF2)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo), // Replace with your drawable
                    contentDescription = "Joker logo with red and black jester hat and white face",
                    modifier = Modifier.size(40.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Background image
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    lensFacing = lensFacing,
                    zoomLevel = 0f,
                    imageCaptureUseCase = imageCaptureUseCase,
                    onQRCodeScanned = { value -> scannedValue = value },
                    flash = flashligt,
                    onFlashToggle = {
                        enabled -> flashligt = enabled
                    }
                );
                if (showScreen1) {
                // Screen 1: scanning frame and bottom icons
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Bottom icons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = {
                                if (flashligt == true){
                                    flashligt = false
                                }else {
                                    flashligt =true
                                }
                            }) {
                            Icon(
                                Icons.Default.FlashlightOn , // Replace with your icon
                                contentDescription = "Flashlight button",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            }) {
                                Icon(
                                    Icons.Default.FlipCameraAndroid, // Replace with your icon
                                    contentDescription = "Gallery button",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Screen 2: confirmation overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Confirme o Login",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "Logar em:",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "puc campinas",
                            fontSize = 12.sp,
                            color = Color(0xFFB0B0B0),
                            fontWeight = FontWeight.Light
                        )
                        Button(
                            onClick = { /* Confirm action */ },
                            colors = ButtonDefaults.buttonColors(Color(0xFFD40000)),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .width(160.dp)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Confirmar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Site errado:",
                                fontSize = 12.sp,
                                color = Color(0xFFB0B0B0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF5DF0FF), shape = RoundedCornerShape(4.dp))
                                    .clickable { /* Edit action */ }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Editar",
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
            }

                // Show scanned QR code value if exists
                scannedValue?.let {
                    showScreen1 = false
                    Text(
                        text = "QR Code: $it",
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(8.dp)
                    )

                }
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
        lensFacing: Int,
        zoomLevel: Float,
        imageCaptureUseCase: ImageCapture,
        onQRCodeScanned: (String) -> Unit,
        flash : Boolean,
        onFlashToggle : (Boolean) -> Unit
    ) {
        val previewUseCase = remember { Preview.Builder().build() }
        val imageAnalysisUseCase = remember {
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        }
        var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
        var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
        val localContext = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // Remember barcode scanner client
        val barcodeScanner = remember { BarcodeScanning.getClient() }

        // Analyzer for imageAnalysisUseCase
        DisposableEffect(barcodeScanner) {
            val analyzer = ImageAnalysis.Analyzer { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val inputImage =
                        InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    barcodeScanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                barcode.rawValue?.let { rawValue ->
                                    onQRCodeScanned(rawValue)
                                }
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                        .addOnFailureListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }
            imageAnalysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(localContext), analyzer)

            onDispose {
                imageAnalysisUseCase.clearAnalyzer()
            }
        }

        fun bindCamera() {
            val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            cameraProvider?.unbindAll()
            val camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                selector,
                previewUseCase,
                imageCaptureUseCase,
                imageAnalysisUseCase
            )
            cameraControl = camera?.cameraControl
        }

        LaunchedEffect(lensFacing) {
            cameraProvider = ProcessCameraProvider.getInstance(localContext).get()
            bindCamera()
        }

        LaunchedEffect(flash) {
            cameraControl?.enableTorch(flash)
            onFlashToggle(flash)
        }

        LaunchedEffect(zoomLevel) {
            cameraControl?.setLinearZoom(zoomLevel)
        }

        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).also {
                    previewUseCase.setSurfaceProvider(it.surfaceProvider)
                }
            }
        )
    }

