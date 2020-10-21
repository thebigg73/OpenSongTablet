package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PrefEditTextView extends LinearLayout {

    TextInputEditText editText;

    public PrefEditTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_edittext,this);

        editText = findViewById(R.id.editText);
        TextInputLayout hint = findViewById(R.id.holderLayout);

        TypedArray s = context.obtainStyledAttributes(attrs,R.styleable.PrefEditTextView);
        editText.setText(s.getString(R.styleable.PrefEditTextView_editTextVal));
        hint.setHint(s.getString(R.styleable.PrefEditTextView_hintTextVal));
        s.recycle();
    }

    public TextInputEditText getEditText() {
        return editText;
    }
    public void setEditText(TextInputEditText editText) {
        this.editText = editText;
    }

}