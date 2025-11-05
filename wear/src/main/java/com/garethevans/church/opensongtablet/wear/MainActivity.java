package com.garethevans.church.opensongtablet.wear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Minimal UI; just show a label
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.logo);
        iv.setPadding(16,16,16,16);
        setContentView(iv);

        // FOR TESTING ONLY
        Intent intent = new Intent(this, MetronomeListenerService.class);
        intent.putExtra("test_beat", "/metronome/beat/tick");
        startService(intent);

        //Intent intent = new Intent(this, MetronomeListenerService.class);
        //startService(intent);
    }
}
