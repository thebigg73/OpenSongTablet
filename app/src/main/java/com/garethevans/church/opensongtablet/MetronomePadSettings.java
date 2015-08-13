package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class MetronomePadSettings extends Activity {

	public SeekBar metronomeVol;
	public SeekBar metronomePan;

	public SeekBar padVol;
	public SeekBar padPan;

	public Button close;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the user preferences
		Preferences.loadPreferences();
        
		// Set the screen and title
		setContentView(R.layout.metronomepadsettings);
		getActionBar().setTitle(getResources().getString(R.string.metronomepadsettings));
		metronomeVol = (SeekBar) findViewById(R.id.metronome_vol_control);
		metronomePan = (SeekBar) findViewById(R.id.metronome_pan_control);
		padVol = (SeekBar) findViewById(R.id.pad_vol_control);
		padPan = (SeekBar) findViewById(R.id.pad_pan_control);
		metronomeVol.setOnSeekBarChangeListener(new seekbarListener());
		metronomePan.setOnSeekBarChangeListener(new seekbarListener());
		padVol.setOnSeekBarChangeListener(new seekbarListener());
		padPan.setOnSeekBarChangeListener(new seekbarListener());
		close = (Button) findViewById(R.id.closeMetronomePadSettings);

		metronomeVol.setProgress((int) (100* FullscreenActivity.metronomevol));
		padVol.setProgress((int) (100* FullscreenActivity.padvol));

		switch (FullscreenActivity.metronomepan) {
			case "left":
				metronomePan.setProgress(0);
				break;
			case "right":
				metronomePan.setProgress(2);
				break;
			default:
				metronomePan.setProgress(1);
				break;
		}

		switch (FullscreenActivity.padpan) {
			case "left":
				padPan.setProgress(0);
				break;
			case "right":
				padPan.setProgress(2);
				break;
			default:
				padPan.setProgress(1);
				break;
		}

	}
	
	
	@Override
	public void onBackPressed() {
		doClose(close);
	}
	
	public void doClose (View view) {
    	FullscreenActivity.metronomevol = (float) metronomeVol.getProgress()/100;
    	FullscreenActivity.padvol = (float) padVol.getProgress()/100;
    	if (metronomePan.getProgress()==0) {
    		FullscreenActivity.metronomepan="left";
    		} else if (metronomePan.getProgress()==2) {
    		FullscreenActivity.metronomepan="right";
    		} else {
    		FullscreenActivity.metronomepan="both";
    		}
    	if (padPan.getProgress()==0) {
    		FullscreenActivity.padpan="left";
    		} else if (padPan.getProgress()==2) {
    		FullscreenActivity.padpan="right";
    		} else {
    		FullscreenActivity.padpan="both";
    		}
    	// Save preferences
		Preferences.savePreferences();
		Intent viewsong = new Intent(this, FullscreenActivity.class);
		viewsong.setClass(MetronomePadSettings.this, FullscreenActivity.class);			
		startActivity(viewsong);
		this.finish();		
	}
			
	
	private class seekbarListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

	}
}
