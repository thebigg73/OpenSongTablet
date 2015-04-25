package com.garethevans.church.opensongtablet;

import android.inputmethodservice.InputMethodService;

public class SoftKeyboardForce extends InputMethodService {

	public boolean onEvaluateInputViewShown() {
/*	     Configuration config = getResources().getConfiguration();
	     return config.keyboard == Configuration.KEYBOARD_NOKEYS
	             || config.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES;
*/
	return true;	
	}
}
