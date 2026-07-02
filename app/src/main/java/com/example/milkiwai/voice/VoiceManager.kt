package com.example.milkiwai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.*

class VoiceManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val parser = VoiceCommandParser()

    interface VoiceCallback {
        fun onCommandParsed(command: ParsedCommand)
        fun onError(error: String)
    }

    fun startListening(callback: VoiceCallback) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callback.onError("Speech recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            
            override fun onError(error: Int) {
                callback.onError("Recognition error: $error")
                destroy()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val command = parser.parse(matches[0])
                    callback.onCommandParsed(command)
                }
                destroy()
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
