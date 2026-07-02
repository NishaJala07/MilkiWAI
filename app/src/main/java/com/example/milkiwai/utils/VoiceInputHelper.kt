package com.example.milkiwai.utils

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class VoiceInputHelper(private val activity: AppCompatActivity) {

    private var speechRecognizer: SpeechRecognizer? = null

    fun attachTo(til: TextInputLayout) {
        til.setEndIconOnClickListener {
            startListening { text ->
                til.editText?.setText(text)
                til.editText?.setSelection(text.length)
            }
        }
    }

    private fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            Toast.makeText(activity, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(activity, "Listening...", Toast.LENGTH_SHORT).show()
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            
            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Recognition error: $error"
                }
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun destroy() {
        speechRecognizer?.destroy()
    }
}
