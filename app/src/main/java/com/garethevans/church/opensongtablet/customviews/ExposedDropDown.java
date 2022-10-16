package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExposedDropDown extends TextInputLayout {

    private AutoCompleteTextView autoCompleteTextView;
    private TextInputLayout textInputLayout;

    public ExposedDropDown(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_exposed_dropdown,this);

        identifyViews();

        int[] set = new int[]{android.R.attr.text, android.R.attr.hint};
        TypedArray a = context.obtainStyledAttributes(attrs, set);

        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);

        autoCompleteTextView.setSingleLine(true);
        // The popup background is set in styles, but it seems to require programmatic setting!
        autoCompleteTextView.setDropDownBackgroundResource(R.drawable.popup_bg);
        if (text!=null) {
            autoCompleteTextView.setText(text);
        }
        if (hint!=null) {
            textInputLayout.setHint(hint);
        }
        textInputLayout.setBoxBackgroundColor(getResources().getColor(R.color.transparent));
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setPadding(0,0,0,0);
        a.recycle();
    }

    private void identifyViews() {
        autoCompleteTextView = findViewById(R.id.textView);
        textInputLayout = findViewById(R.id.textLayout);

        autoCompleteTextView.setId(View.generateViewId());
        textInputLayout.setId(View.generateViewId());
    }

    public Editable getText() {
        // Check for null
        if (autoCompleteTextView.getText()==null) {
            autoCompleteTextView.setText("");
        }
        return autoCompleteTextView.getText();
    }
    public CharSequence getHint() {
        if (textInputLayout.getHint()==null) {
            textInputLayout.setHint("");
        }
        return textInputLayout.getHint();
    }

    public void setText(String text) {
        autoCompleteTextView.setText(text);
    }
    public void setHint(String hint) {
        textInputLayout.setHint(hint);
    }
    public void setAdapter(ExposedDropDownArrayAdapter arrayAdapter) {
        autoCompleteTextView.setAdapter(arrayAdapter);
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        autoCompleteTextView.addTextChangedListener(textWatcher);
    }

    public TextInputLayout getTextInputLayout() {
        return textInputLayout;
    }

    public AutoCompleteTextView getAutoCompleteTextView() {
        return autoCompleteTextView;
    }

    public void setPopupSize(MainActivityInterface mainActivityInterface) {
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            try {
                setPopupSize(mainActivityInterface);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                boolean largePopups = mainActivityInterface.getPreferences().getMyPreferenceBoolean("largePopups",false);
                if (largePopups) {
                    try {
                        if (autoCompleteTextView!=null) {
                            // Get the location of the popup position and negatively offset this minus the toolbar height
                            int[] location = new int[2];

                            autoCompleteTextView.getLocationOnScreen(location);
                            int y = location[1] + autoCompleteTextView.getHeight() - mainActivityInterface.getToolbar().getActionBarHeight(true);
                            autoCompleteTextView.setDropDownVerticalOffset(-y);

                            Log.d("ed","location[1]:"+location[1]);
                            int height = mainActivityInterface.getDisplayMetrics()[1] - (mainActivityInterface.getToolbar().getActionBarHeight(false)*2);
                            autoCompleteTextView.setDropDownHeight(height);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });

    }
}
