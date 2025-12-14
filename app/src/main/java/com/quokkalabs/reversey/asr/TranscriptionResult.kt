package com.quokkalabs.reversey.asr

data class TranscriptionResult(
    val isSuccess: Boolean,
    val text: String? = null,
    val confidence: Float = 0f,
    val errorMessage: String? = null
) {
    companion object {
        fun success(text: String, confidence: Float = 1.0f) = TranscriptionResult(
            isSuccess = true,
            text = text,
            confidence = confidence
        )

        fun error(message: String) = TranscriptionResult(
            isSuccess = false,
            errorMessage = message
        )
    }
}