package com.app.practice.buddhismchanttracker.voice

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognizerManager(private val app: Application) {

    private var sr: SpeechRecognizer? = null
    private var lastHitAt = 0L

    private val _listening = MutableStateFlow(false)
    val listening = _listening.asStateFlow()

    fun start(keywords: List<String>, onHit: () -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(app)) return
        if (sr == null) sr = SpeechRecognizer.createSpeechRecognizer(app)
        _listening.value = true

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toLanguageTag()) // ko-KR
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        sr?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { restart(intent) }
            override fun onEvent(eventType: Int, params: Bundle?) {}

            private fun parse(results: List<String>?) {
                val now = System.currentTimeMillis()
                val text = results?.joinToString(" ")?.lowercase(Locale.KOREAN) ?: return
                if (now - lastHitAt < 1000) return
                if (keywords.any { text.contains(it.lowercase(Locale.KOREAN)) }) {
                    lastHitAt = now
                    onHit()
                }
            }

            override fun onResults(results: Bundle?) {
                parse(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
                restart(intent)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                parse(partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
            }

            private fun restart(i: Intent) { if (_listening.value) sr?.startListening(i) }
        })

        sr?.startListening(intent)
    }

    fun stop() {
        _listening.value = false
        sr?.stopListening()
        sr?.destroy()
        sr = null
    }
}