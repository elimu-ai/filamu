package ai.elimu.filamu

import ai.elimu.filamu.ui.VideosActivity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.name
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).i("onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Verify that the content-provider APK has been installed
        // TODO
    }

    override fun onStart() {
        Timber.tag(TAG).i("onStart")
        super.onStart()

        val intent = Intent(this, VideosActivity::class.java)
        startActivity(intent)

        finish()
    }
}