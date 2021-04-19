package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PrefEditTextView extends LinearLayout {

    private final TextInputEditText editText;

    public PrefEditTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_edittext,this);

        editText = findViewById(R.id.editText);
        editText.setSingleLine(true);
        editText.setImeActionLabel(context.getString(R.string.select), EditorInfo.IME_ACTION_DONE);
        TextInputLayout hint = findViewById(R.id.holderLayout);

        TypedArray s = context.obtainStyledAttributes(attrs,R.styleable.PrefEditTextView);
        editText.setText(s.getString(R.styleable.PrefEditTextView_editTextVal));
        hint.setHint(s.getString(R.styleable.PrefEditTextView_hintTextVal));
        s.recycle();
    }

    public void setMultiLine(int minLines) {
        editText.setSingleLine(false);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(minLines);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
    }
    public TextInputEditText getEditText() {
        return editText;
    }

}