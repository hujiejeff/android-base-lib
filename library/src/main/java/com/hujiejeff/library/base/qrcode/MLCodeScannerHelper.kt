package com.hujiejeff.library.base.qrcode

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

object MLCodeScannerHelper {
    private val scanner: BarcodeScanner

    init {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()
        scanner = BarcodeScanning.getClient(options)
    }

    @JvmStatic
    fun parseCode(bitmap: Bitmap, callback: (List<String>) -> Unit) {

        val image = InputImage.fromBitmap(bitmap, 0)

        val result = scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val results: List<String> = barcodes.map {
                    it.rawValue ?: ""
                }
                callback.invoke(results)
            }
            .addOnFailureListener {
                callback.invoke(emptyList())
            }
    }
}