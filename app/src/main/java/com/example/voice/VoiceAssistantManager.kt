package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantManager(private val context: Context) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        initSpeechRecognizer()
        initTextToSpeech()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceAssistantManager)
            }
        }
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                isTtsInitialized = true
            }
        }
    }

    fun startListening() {
        if (speechRecognizer == null) initSpeechRecognizer()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Prefer offline recognition
        }
        _isListening.value = true
        _recognizedText.value = ""
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        _isListening.value = false
        speechRecognizer?.stopListening()
    }

    fun speak(text: String) {
        if (isTtsInitialized && text.isNotBlank()) {
            _isSpeaking.value = true
            // Strip markdown symbols for natural speech
            val cleanText = text.replace(Regex("""```[a-zA-Z]*"""), "")
                .replace(Regex("""[*_#`>]"""), "")
                .take(500)
            textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
            _isSpeaking.value = false
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    fun release() {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""
        _recognizedText.value = text
        _isListening.value = false
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.firstOrNull()?.let { _recognizedText.value = it }
    }

    override fun onError(error: Int) {
        _isListening.value = false
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() { _isListening.value = false }
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
