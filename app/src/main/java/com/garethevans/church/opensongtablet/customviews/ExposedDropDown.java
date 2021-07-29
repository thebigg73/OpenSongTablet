package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textfield.TextInputLayout;

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

        if (text!=null) {
            autoCompleteTextView.setText(text);
        }
        if (hint!=null) {
            textInputLayout.setHint(hint);
        }

        textInputLayout.setPadding(0,0,0,0);
        ((LinearLayout.LayoutParams)textInputLayout.getLayoutParams()).setMargins(0,0,0,getPxFromDp(context, -22));
        textInputLayout.setTranslationY(getPxFromDp(context, -22));
        a.recycle();
    }

    private void identifyViews() {
        autoCompleteTextView = findViewById(R.id.textView);
        textInputLayout = findViewById(R.id.textLayout);
    }

    private int getPxFromDp(Context c, int dp) {
        Resources r = c.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics());
    }
    public Editable getText() {
        // Check for null
        if (autoCompleteTextView.getText()==null) {
            autoCompleteTextView.setText("");
        }
        return autoCompleteTextView.getText();
    }

    public void setText(String text) {
        autoCompleteTextView.setText(text);
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
}
