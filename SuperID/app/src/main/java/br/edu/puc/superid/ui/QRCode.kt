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
import com.google.firebase.firestore.FieldValue
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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import br.edu.puc.superid.R
import br.edu.puc.superid.ui.scannerConfig.WithPermission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun QRCodeScreen(navController: NavController) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        WithPermission(
            modifier = Modifier.padding(innerPadding),
            permission = Manifest.permission.CAMERA
        ) {
            CameraContent(navController)
        }
    }
}

@Composable
private fun CameraContent(navController: NavController) {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }
    var scannedValue by remember { mutableStateOf<String?>(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    var partnerName by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    val firestore = Firebase.firestore
    val auth = Firebase.auth

    LaunchedEffect(scannedValue) {
        scannedValue?.let { qrCode ->
            // Adição: Verificação de autenticação
            val user = auth.currentUser
            if (user == null) {
                partnerName = "Autenticação necessária"
                showConfirmation = true
                scannedValue = null
                return@let
            }

            try {
                val document = firestore.collection("logins").document(qrCode).get().await()
                if (document.exists() && document.getString("status") != "completed") {
                    val apiKey = document.getString("API") ?: ""

                    firestore.collection("partners")
                        .whereEqualTo("apiKey", apiKey)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val partnerDoc = querySnapshot.documents[0]
                                partnerName = partnerDoc.getString("nome") ?: "Site Parceiro"
                            }
                            showConfirmation = true
                        }
                }
            } catch (e: Exception) {
                Log.e("QRCode", "Erro ao processar QR Code", e)
            }
        }
    }

    fun confirmLogin() {
        val user = auth.currentUser ?: return
        scannedValue?.let { qrCode ->
            val updates = hashMapOf<String, Any>(
                "UserID" to user.uid,
                "status" to "Completed",
                "email" to (user.email ?: ""),
            )

            firestore.collection("logins").document(qrCode)
                .update(updates)
                .addOnSuccessListener {
                    showSuccess = true
                    showConfirmation = false
                }
                .addOnFailureListener { e ->
                    Log.e("QRCode", "Erro ao confirmar login", e)
                }
        }
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(2000)
            navController.navigate("home") {
                popUpTo("qrCode") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            lensFacing = lensFacing,
            zoomLevel = 0f,
            imageCaptureUseCase = imageCaptureUseCase,
            onQRCodeScanned = { value ->
                if (value != null && scannedValue == null) {
                    scannedValue = value
                    partnerName = value
                }
            },
            flash = flashEnabled,
            onFlashToggle = { enabled -> flashEnabled = enabled }
        )

        if (showConfirmation) {
            ConfirmationDialog(
                partnerName = partnerName,
                onConfirm = {
                    if (partnerName == "Autenticação necessária") {
                        navController.navigate("login")
                    } else {
                        confirmLogin()
                    }
                },
                onCancel = {
                    scannedValue?.let {
                        firestore.collection("logins").document(it)
                            .update("tentativas", FieldValue.increment(-1))
                    }
                    showConfirmation = false
                    scannedValue = null
                }
            )
        }

        if (showSuccess) {
            SuccessDialog()
        }

        if (!showConfirmation && !showSuccess) {
            CameraControls(
                flashEnabled = flashEnabled,
                onFlashToggle = { flashEnabled = !flashEnabled },
                onFlipCamera = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                }
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(
    partnerName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
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
                text = if (partnerName == "Autenticação necessária") "Autenticação Requerida" else "Confirme o Login",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal
            )

            if (partnerName != "Autenticação necessária") {
                Text(
                    text = "Logar em:",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
            }

            Text(
                text = partnerName,
                fontSize = 12.sp,
                color = Color(0xFFB0B0B0),
                fontWeight = FontWeight.Light
            )

            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    if (partnerName == "Autenticação necessária") Color(0xFF007AFF) else Color(0xFFD40000)
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(160.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = if (partnerName == "Autenticação necessária") "Fazer Login" else "Confirmar",
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
                    text = when {
                        partnerName == "Autenticação necessária" -> "Deseja continuar?"
                        else -> "Site errado?"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF5DF0FF), shape = RoundedCornerShape(4.dp))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (partnerName == "Autenticação necessária") "Cancelar" else "Cancelar",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessDialog() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(160.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.Green,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Login realizado com sucesso!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CameraControls(
    flashEnabled: Boolean,
    onFlashToggle: () -> Unit,
    onFlipCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(250.dp)
                .background(Color.Transparent)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onFlashToggle,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            ) {
                Icon(
                    Icons.Default.FlashlightOn,
                    contentDescription = "Flash",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            ) {
                Icon(
                    Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip Camera",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
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
    onQRCodeScanned: (String?) -> Unit,
    flash: Boolean,
    onFlashToggle: (Boolean) -> Unit
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

    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val executor = remember { Executors.newSingleThreadExecutor() }

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
            } else {
                imageProxy.close()
            }
        }
        imageAnalysisUseCase.setAnalyzer(executor, analyzer)

        onDispose {
            imageAnalysisUseCase.clearAnalyzer()
            executor.shutdown()
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
        val provider = ProcessCameraProvider.getInstance(localContext).get()
        cameraProvider = provider
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