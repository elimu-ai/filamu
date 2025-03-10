package ai.elimu.filamu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import ai.elimu.filamu.ui.VideosActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(getClass().getName(), "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Verify that the content-provider APK has been installed
        // TODO
    }

    @Override
    protected void onStart() {
        Log.i(getClass().getName(), "onStart");
        super.onStart();

        Intent intent = new Intent(this, VideosActivity.class);
        startActivity(intent);

        finish();
    }
}