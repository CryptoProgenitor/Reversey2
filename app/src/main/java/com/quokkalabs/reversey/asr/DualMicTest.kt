package com.quokkalabs.reversey.asr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Quick test: Can AudioRecord and SpeechRecognizer run simultaneously?
 *
 * Call from a button or init block:
 *   DualMicTest(context).runTest()
 */
class DualMicTest(private val context: Context) {

    private val TAG = "DUAL_MIC_TEST"


    fun runSpeechOnlyTest() {
        Log.d(TAG, "========== SPEECH ONLY TEST ==========")

        // NO AudioRecord - just SpeechRecognizer
        startSpeechRecognizer()

        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            Log.d(TAG, "========== SPEECH ONLY COMPLETE ==========")
        }
    }

    fun runTest() {
        Log.d(TAG, "========== STARTING DUAL MIC TEST ==========")

        // Start both simultaneously
        startAudioRecord()
        startSpeechRecognizer()

        // Run for 5 seconds then stop
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            Log.d(TAG, "========== TEST COMPLETE - CHECK LOGS ==========")
        }
    }

    private fun startAudioRecord(): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "❌ AudioRecord: No permission")
                return@launch
            }

            val sampleRate = 44100
            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                val buffer = ShortArray(bufferSize / 2)
                audioRecord.startRecording()
                Log.d(TAG, "✅ AudioRecord: STARTED")

                var samplesRead = 0
                repeat(50) { // ~5 seconds
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        samplesRead += read
                    }
                    delay(100)
                }

                audioRecord.stop()
                audioRecord.release()
                Log.d(TAG, "✅ AudioRecord: STOPPED - read $samplesRead samples")

            } catch (e: Exception) {
                Log.e(TAG, "❌ AudioRecord: FAILED - ${e.message}")
            }
        }
    }

    private fun startSpeechRecognizer(): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.e(TAG, "❌ SpeechRecognizer: Not available")
                return@launch
            }

            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "✅ SpeechRecognizer: READY")
                }
                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "✅ SpeechRecognizer: HEARING SPEECH")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d(TAG, "✅ SpeechRecognizer: RESULT = '${matches?.firstOrNull()}'")
                }
                override fun onError(error: Int) {
                    val msg = when(error) {
                        SpeechRecognizer.ERROR_AUDIO -> "AUDIO_ERROR"
                        SpeechRecognizer.ERROR_NETWORK -> "NETWORK"
                        SpeechRecognizer.ERROR_NO_MATCH -> "NO_MATCH"
                        7 -> "BUSY"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "NO_PERMISSION"
                        else -> "ERROR_$error"
                    }
                    Log.e(TAG, "❌ SpeechRecognizer: $msg")
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    Log.d(TAG, "✅ SpeechRecognizer: END OF SPEECH")
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d(TAG, "✅ SpeechRecognizer: PARTIAL = '${partial?.firstOrNull()}'")
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            Log.d(TAG, "✅ SpeechRecognizer: STARTING...")
            recognizer.startListening(intent)

            delay(5000)
            recognizer.stopListening()
            recognizer.destroy()
            Log.d(TAG, "✅ SpeechRecognizer: STOPPED")
        }
    }
}