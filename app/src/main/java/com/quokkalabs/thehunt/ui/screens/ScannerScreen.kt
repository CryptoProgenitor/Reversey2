package com.quokkalabs.thehunt.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.quokkalabs.thehunt.R
import com.quokkalabs.thehunt.ui.components.PulsingButton
import com.quokkalabs.thehunt.ui.theme.Bone
import com.quokkalabs.thehunt.ui.theme.Violet
import kotlin.math.min

@Composable
fun ScannerScreen(onResult: (String) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var denied by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        denied = !ok
    }

    when {
        granted -> CameraView(onResult)
        denied -> PermissionMessage(
            text = stringResource(R.string.camera_denied),
            buttonLabel = stringResource(R.string.camera_return),
            onButton = onBack,
        )
        else -> PermissionMessage(
            text = stringResource(R.string.camera_rationale),
            buttonLabel = stringResource(R.string.camera_grant),
            onButton = { launcher.launch(Manifest.permission.CAMERA) },
        )
    }
}

@Composable
private fun PermissionMessage(text: String, buttonLabel: String, onButton: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Bone,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        PulsingButton(text = buttonLabel, onClick = onButton)
    }
}

@Composable
private fun CameraView(onResult: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnResult by rememberUpdatedState(onResult)
    val analyzer = remember { QrAnalyzer { code -> currentOnResult(code) } }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            analyzer.shutdown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val future = ProcessCameraProvider.getInstance(ctx)
                future.addListener({
                    val provider = future.get()
                    cameraProvider = provider
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(ContextCompat.getMainExecutor(ctx), analyzer) }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
        )
        ViewfinderOverlay(Modifier.fillMaxSize())
        Text(
            text = stringResource(R.string.scanner_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Bone.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
        )
    }
}

/** Purple corner brackets + a drifting scanline. */
@Composable
private fun ViewfinderOverlay(modifier: Modifier = Modifier) {
    val scan by rememberInfiniteTransition(label = "scanline")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(2400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "scanY",
        )
    Canvas(modifier) {
        val side = min(size.width, size.height) * 0.64f
        val left = (size.width - side) / 2f
        val top = (size.height - side) / 2f - size.height * 0.05f
        val bracket = side * 0.16f
        val stroke = 3.dp.toPx()
        val corners = listOf(
            // topLeft: origin, then directions for the two arms
            Triple(Offset(left, top), Offset(bracket, 0f), Offset(0f, bracket)),
            Triple(Offset(left + side, top), Offset(-bracket, 0f), Offset(0f, bracket)),
            Triple(Offset(left, top + side), Offset(bracket, 0f), Offset(0f, -bracket)),
            Triple(Offset(left + side, top + side), Offset(-bracket, 0f), Offset(0f, -bracket)),
        )
        corners.forEach { (origin, armA, armB) ->
            drawLine(Violet, origin, origin + armA, strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(Violet, origin, origin + armB, strokeWidth = stroke, cap = StrokeCap.Round)
        }
        // scanline
        val y = top + side * scan
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Violet.copy(alpha = 0.85f), Color.Transparent),
                startX = left,
                endX = left + side,
            ),
            topLeft = Offset(left, y),
            size = Size(side, 2.dp.toPx()),
        )
    }
}

private class QrAnalyzer(private val onCode: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )
    private var delivered = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (delivered || mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                if (!delivered) {
                    barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue?.let { value ->
                        delivered = true
                        onCode(value)
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    fun shutdown() {
        scanner.close()
    }
}
