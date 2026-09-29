/*
 *   Copyright (c) 2026 Willian Santos
 *
 *   This work is licensed under the Creative Commons
 *   Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 *
 *   You are free to:
 *     - Share  — copy and redistribute the material in any medium or format
 *     - Adapt  — remix, transform, and build upon the material
 *
 *   Under the following terms:
 *     - Attribution    — You must give appropriate credit, provide a link to
 *                        the license, and indicate if changes were made.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.uisystem.components.core.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureAutoFocusRangeRestrictionNear
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset1920x1080
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.autoFocusRangeRestriction
import platform.AVFoundation.exposureMode
import platform.AVFoundation.focusMode
import platform.AVFoundation.isAutoFocusRangeRestrictionSupported
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.isFocusModeSupported
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BarcodeScannerCameraView(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier,
) {
    val device = remember { AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) }

    if (device == null) {
        Box(
            modifier = modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            MinhaDespensaText(
                text = "Câmera indisponível no Simulador.\nExecute em um iPhone físico para ler códigos de barras.",
                color = Color.White,
                fontStyle = MinhaDespensaTheme.typography.bodySmall,
                modifier = Modifier.padding(24.dp),
            )
        }
        return
    }

    val scannerController = remember {
        IosBarcodeScannerController(
            device = device,
            onBarcodeScanned = onBarcodeScanned,
        )
    }

    val cameraPreviewView = remember {
        CameraPreviewUIView(scannerController.session)
    }

    DisposableEffect(scannerController) {
        scannerController.start()
        onDispose {
            scannerController.stop()
        }
    }

    UIKitView(
        factory = { cameraPreviewView },
        modifier = modifier.fillMaxSize(),
        onRelease = {
            scannerController.stop()
        },
    )
}


@OptIn(ExperimentalForeignApi::class)
private class IosBarcodeScannerController(
    private val device: AVCaptureDevice,
    private val onBarcodeScanned: (String) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    val session = AVCaptureSession()
    private var isScanned = false
    private val scannerQueue = dispatch_queue_create("com.bitlabbr.minhadespensa.barcodescanner.queue", null)

    init {
        setupCamera()
    }

    private fun setupCamera() {
        val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        if (authStatus == AVAuthorizationStatusAuthorized) {
            configureSession()
        } else {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                if (granted) {
                    dispatch_async(dispatch_get_main_queue()) {
                        configureSession()
                        start()
                    }
                }
            }
        }
    }

    private fun configureSession() {
        session.beginConfiguration()

        if (session.canSetSessionPreset(AVCaptureSessionPreset1920x1080)) {
            session.sessionPreset = AVCaptureSessionPreset1920x1080
        } else if (session.canSetSessionPreset(AVCaptureSessionPresetHigh)) {
            session.sessionPreset = AVCaptureSessionPresetHigh
        }

        if (device.lockForConfiguration(null)) {
            if (device.isFocusModeSupported(AVCaptureFocusModeContinuousAutoFocus)) {
                device.focusMode = AVCaptureFocusModeContinuousAutoFocus
            }
            if (device.isExposureModeSupported(AVCaptureExposureModeContinuousAutoExposure)) {
                device.exposureMode = AVCaptureExposureModeContinuousAutoExposure
            }
            if (device.isAutoFocusRangeRestrictionSupported()) {
                device.autoFocusRangeRestriction = AVCaptureAutoFocusRangeRestrictionNear
            }
            device.unlockForConfiguration()
        }

        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
        }

        val metadataOutput = AVCaptureMetadataOutput()
        if (session.canAddOutput(metadataOutput)) {
            session.addOutput(metadataOutput)
            metadataOutput.setMetadataObjectsDelegate(this, queue = scannerQueue)

            val available = metadataOutput.availableMetadataObjectTypes
            val desiredTypes = listOf(
                AVMetadataObjectTypeEAN13Code,
                AVMetadataObjectTypeEAN8Code,
                AVMetadataObjectTypeUPCECode,
                AVMetadataObjectTypeQRCode,
                AVMetadataObjectTypeCode128Code,
                AVMetadataObjectTypeCode39Code,
            )

            metadataOutput.metadataObjectTypes = desiredTypes.filter { type ->
                available.contains(type)
            }
        }

        session.commitConfiguration()
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        if (isScanned) return

        for (item in didOutputMetadataObjects) {
            val readableObject = item as? AVMetadataMachineReadableCodeObject ?: continue
            val barcode = readableObject.stringValue

            if (!barcode.isNullOrBlank()) {
                isScanned = true
                dispatch_async(dispatch_get_main_queue()) {
                    session.stopRunning()
                    onBarcodeScanned(barcode)
                }
                break
            }
        }
    }

    fun start() {
        if (!session.isRunning()) {
            dispatch_async(scannerQueue) {
                this@IosBarcodeScannerController.session.startRunning()
            }
        }
    }

    fun stop() {
        if (session.isRunning()) {
            dispatch_async(scannerQueue) {
                this@IosBarcodeScannerController.session.stopRunning()
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class CameraPreviewUIView(
    session: AVCaptureSession,
) : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {

    private val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.setFrame(bounds)
    }
}