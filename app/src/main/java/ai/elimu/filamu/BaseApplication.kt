package ai.elimu.filamu

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@HiltAndroidApp
class BaseApplication : Application() {
    var executor: Executor = Executors.newSingleThreadExecutor()

    var tTS: TextToSpeech? = null
        private set

    override fun onCreate() {
        Log.i(javaClass.name, "onCreate")
        super.onCreate()

        // Initialize the Text-to-Speech (TTS) engine
        tTS = TextToSpeech(applicationContext, object : OnInitListener {
            override fun onInit(status: Int) {
                Log.i(javaClass.name, "onInit")

                // Fetch the chosen language from the Appstore
                // TODO
//                tts.setLanguage(new Locale("hin"));
                tTS!!.setSpeechRate(SPEECH_RATE)
            }
        })
    }

    companion object {
        const val SPEECH_RATE: Float = 0.5f
    }
}
