package com.example.milkiwai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.AlphaAnimation
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.milkiwai.R
import com.example.milkiwai.voice.ParsedCommand
import com.example.milkiwai.voice.VoiceAction
import com.example.milkiwai.voice.VoiceCommandParser
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class VoiceAssistantActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private val parser = VoiceCommandParser()
    private lateinit var tvTranscript: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnMic: FloatingActionButton
    private lateinit var wave1: View
    private lateinit var wave2: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_assistant)

        tvTranscript = findViewById(R.id.tvTranscript)
        tvStatus = findViewById(R.id.tvStatus)
        btnMic = findViewById(R.id.btnMic)
        wave1 = findViewById(R.id.wave1)
        wave2 = findViewById(R.id.wave2)

        findViewById<ImageButton>(R.id.btnClose).setOnClickListener { finish() }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        setupSpeechListener()

        btnMic.setOnClickListener {
            checkPermissionAndStart()
        }

        setupSuggestions()
        
        // Auto-start listening
        checkPermissionAndStart()
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        } else {
            startListening()
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.startListening(intent)
        tvStatus.text = "I'm listening..."
        tvTranscript.text = "..."
        startWaveAnimation()
    }

    private fun setupSpeechListener() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                stopWaveAnimation()
                tvStatus.text = "Analyzing..."
            }

            override fun onError(error: Int) {
                stopWaveAnimation()
                tvStatus.text = "Tap to retry"
                // No toast to avoid interrupting the futuristic vibe, unless critical
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    tvTranscript.text = "\"$text\""
                    val command = parser.parse(text)
                    handleCommand(command)
                } else {
                    tvStatus.text = "Tap to Speak"
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    tvTranscript.text = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun handleCommand(command: ParsedCommand) {
        if (command.action == VoiceAction.UNKNOWN) {
            tvStatus.text = "Command not recognized"
            return
        }

        val intent = Intent().apply {
            putExtra("VOICE_ACTION", command.action.name)
            command.data.forEach { (key, value) -> putExtra("DATA_$key", value) }
        }
        setResult(RESULT_OK, intent)
        
        tvStatus.text = "Executing command..."
        tvStatus.postDelayed({ finish() }, 1200)
    }

    private fun setupSuggestions() {
        findViewById<Chip>(R.id.suggestion1).setOnClickListener { startSuggestion("Add customer Rahul") }
        findViewById<Chip>(R.id.suggestion2).setOnClickListener { startSuggestion("Generate bills") }
        findViewById<Chip>(R.id.suggestion3).setOnClickListener { startSuggestion("Show pending payments") }
        findViewById<Chip>(R.id.suggestion4).setOnClickListener { startSuggestion("Milk requirement") }
    }

    private fun startSuggestion(text: String) {
        tvTranscript.text = text
        handleCommand(parser.parse(text))
    }

    private fun startWaveAnimation() {
        wave1.visibility = View.VISIBLE
        wave2.visibility = View.VISIBLE
        
        fun getWaveAnim(delay: Long): AnimationSet {
            val scale = ScaleAnimation(0.3f, 1.5f, 0.3f, 1.5f, 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            val alpha = AlphaAnimation(1.0f, 0.0f)
            
            return AnimationSet(true).apply {
                addAnimation(scale)
                addAnimation(alpha)
                duration = 2000
                repeatCount = Animation.INFINITE
                startOffset = delay
            }
        }
        
        wave1.startAnimation(getWaveAnim(0))
        wave2.startAnimation(getWaveAnim(1000))
    }

    private fun stopWaveAnimation() {
        wave1.clearAnimation()
        wave2.clearAnimation()
        wave1.visibility = View.INVISIBLE
        wave2.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
