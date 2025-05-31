package br.edu.puc.superid.ui.scannerConfig


import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage




private class YourImageAnalyzer : ImageAnalysis.Analyzer {

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API
            // ...
        }
    }
}



fun scanBarcodes(image: InputImage) {
        // [START set_detector_options]
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(options)


        // [START run_detector]
        val result = scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Task completed successfully
                // [START_EXCLUDE]
                // [START get_barcodes]
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints

                    val rawValue = barcode.rawValue

                    val valueType = barcode.valueType
                    // See API reference for complete list of supported types
                    when (valueType) {
                        Barcode.TYPE_WIFI -> {
                            val ssid = barcode.wifi!!.ssid
                            val password = barcode.wifi!!.password
                            val type = barcode.wifi!!.encryptionType
                        }
                        Barcode.TYPE_URL -> {
                            val title = barcode.url!!.title
                            val url = barcode.url!!.url
                        }
                    }
                }
                // [END get_barcodes]
                // [END_EXCLUDE]
            }
            .addOnFailureListener {
                // Task failed with an exception
                // ...
            }
        // [END run_detector]
    }
