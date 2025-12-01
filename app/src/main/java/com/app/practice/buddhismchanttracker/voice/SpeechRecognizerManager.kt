package com.app.practice.buddhismchanttracker.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var sr: SpeechRecognizer? = null
    private var lastHitAt = 0L

    private val _listening = MutableStateFlow(false)
    val listening = _listening.asStateFlow()

    private val _lastHeardText = MutableStateFlow("")
    val lastHeardText = _lastHeardText.asStateFlow()

    // 연속 인식 유지용 플래그
    @Volatile
    private var keepListening = false

    fun start(
        keywords: List<String>,
        onHit: () -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e("SRManager", "Speech recognition not available")
            return
        }

        if (sr == null) {
            sr = SpeechRecognizer.createSpeechRecognizer(context)
        }

        keepListening = true
        _listening.value = true

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        val lowerKeywords = keywords.map { it.lowercase(Locale.KOREAN) }

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SRManager", "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SRManager", "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("SRManager", "RMS: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) { }

            override fun onEndOfSpeech() {
                Log.d("SRManager", "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                Log.e("SRManager", "onError: $error")
                // 6: TIMEOUT, 7: NO_MATCH → 다시 듣기
                if (_listening.value &&
                    (error == SpeechRecognizer.ERROR_NO_MATCH ||
                            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
                ) {
                    Log.d("SRManager", "retry listening after error=$error")
                    sr?.cancel()
                    sr?.startListening(intent)
                } else {
                    _listening.value = false
                }
            }

            override fun onResults(results: Bundle?) {
                val texts = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.toList()
                    ?: emptyList()

                Log.d("SRManager", "onResults: $texts")

                handleTexts(texts, lowerKeywords, onHit)

                if (keepListening) {
                    try {
                        sr?.startListening(intent)
                    } catch (t: Throwable) {
                        Log.e("SRManager", "restart failed: ${t.message}", t)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val texts = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.toList()
                    ?: emptyList()

                Log.d("SRManager", "onPartialResults: $texts")
                handleTexts(texts, lowerKeywords, onHit)
            }

            override fun onEvent(eventType: Int, params: Bundle?) { }
        }

        sr?.setRecognitionListener(listener)

        Log.d("SRManager", "startListening with keywords=$keywords")
        try {
            sr?.startListening(intent)
        } catch (t: Throwable) {
            Log.e("SRManager", "startListening failed: ${t.message}", t)
        }
    }

    private fun handleTexts(
        texts: List<String>,
        lowerKeywords: List<String>,
        onHit: () -> Unit
    ) {
        if (texts.isEmpty()) return

        _lastHeardText.value = texts.joinToString(" / ")

        val now = System.currentTimeMillis()
        if (now - lastHitAt < 1000L) return

        val lowerHeard = texts.joinToString(" ").lowercase(Locale.KOREAN)
        Log.d("SRManager", "heard: [$lowerHeard]")

        val matched = lowerKeywords.any { key ->
            key.isNotBlank() && lowerHeard.contains(key)
        }

        if (matched) {
            Log.d("SRManager", "keyword HIT!")
            lastHitAt = now
            onHit()
        }
    }


    fun stop() {
        Log.d("SRManager", "stop() called")
        keepListening = false
        _listening.value = false
        try {
            sr?.stopListening()
        } catch (t: Throwable) {
            Log.e("SRManager", "stopListening error: ${t.message}", t)
        }
        sr?.destroy()
        sr = null
    }
}
