package com.garethevans.church.opensongtablet;

import android.inputmethodservice.InputMethodService;

public class SoftKeyboardForce extends InputMethodService {

	public boolean onEvaluateInputViewShown() {
		try {
			super.onEvaluateInputViewShown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return true;
	}
}
