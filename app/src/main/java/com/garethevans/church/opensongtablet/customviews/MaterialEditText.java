package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MaterialEditText extends LinearLayout implements View.OnTouchListener {

    private final TextInputEditText editText;
    private final TextInputLayout textInputLayout;
    private final boolean restoreState;

    // By default this is a single line edit text
    // For multiline, the number of lines has to be specified (maxLines/lines)
    // The lines has to be greater than 1

    public MaterialEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_edittext, this);

        int[] set = new int[]{android.R.attr.text,
                android.R.attr.hint,
                android.R.attr.digits,
                android.R.attr.lines,
                android.R.attr.minLines,
                android.R.attr.maxLines,
                android.R.attr.imeOptions,
                android.R.attr.inputType,
                android.R.attr.saveEnabled,
                R.attr.endIconMode,
                R.attr.useMonospace};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        CharSequence digits = a.getText(2);
        int lines = a.getInteger(3,1);
        int minLines = a.getInteger(4,1);
        int maxLines = a.getInteger(5,1);
        CharSequence imeOptions = a.getText(6);
        int inputType = a.getInt(7,InputType.TYPE_CLASS_TEXT);
        restoreState = a.getBoolean(8,true);
        int endIconMode = a.getInt(9, TextInputLayout.END_ICON_NONE);
        boolean useMonospace = a.getBoolean(10, false);

        editText = findViewById(R.id.editText);
        textInputLayout = findViewById(R.id.holderLayout);

        editText.setId(View.generateViewId());
        textInputLayout.setId(View.generateViewId());

        if (text != null) {
            editText.setText(text);
        }
        if (hint != null) {
            textInputLayout.setHint(hint);
        }
        if (digits != null) {
            editText.setKeyListener(DigitsKeyListener.getInstance(digits.toString()));
        }
        editText.setInputType(inputType);
        if (inputType == InputType.TYPE_TEXT_FLAG_MULTI_LINE) {
            editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        }
        if (lines>1) {
            allowScrolling();
            editText.setLines(lines);
        }
        if (minLines>1) {
            allowScrolling();
            editText.setMinLines(minLines);
        }
        if (maxLines>1) {
            allowScrolling();
            editText.setMaxLines(maxLines);
        }
        if (imeOptions != null) {
            int option = EditorInfo.IME_ACTION_UNSPECIFIED;
            switch (imeOptions.toString()) {
                case "actionDone":
                    option = EditorInfo.IME_ACTION_DONE;
                    break;
                case "actionGo":
                    option = EditorInfo.IME_ACTION_GO;
                    break;
                case "actionNext":
                    option = EditorInfo.IME_ACTION_NEXT;
                    break;
            }
            editText.setImeOptions(option);
        } else {
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        if (useMonospace) {
            editText.setTypeface(Typeface.MONOSPACE);
        }

        // By default restore the state/temp text for rotating, etc.
        // Can override if a fragment is reused
        editText.setSaveEnabled(restoreState);

        textInputLayout.setEndIconMode(endIconMode);
        Log.d("MaterialEditText","endIconMode="+endIconMode);
        a.recycle();
    }

    public void setText(String text) {
        if (text.isEmpty()) {
            editText.setText(null);
        }
        editText.setText(text);
    }

    public Editable getText() {
        if (editText.getText()==null) {
            editText.setText("");
        }
        return editText.getText();
    }

    public void setHint(String hintText) {
        textInputLayout.setHint(hintText);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener editorActionListener) {
        editText.setOnEditorActionListener(editorActionListener);
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }

    public void setLines(int lines) {
        editText.setLines(lines);
        allowScrolling();
    }

    public void setMaxLines(int maxLines) {
        editText.setMaxLines(maxLines);
        allowScrolling();
    }

    public void setMinLines(int minLines) {
        editText.setMinLines(minLines);
        allowScrolling();
    }

    public void setDigits(String digits) {
        editText.setKeyListener(DigitsKeyListener.getInstance(digits));
    }

    public int getSelectionStart() {
        return editText.getSelectionStart();
    }

    public void setSelection(int position) {
        editText.setSelection(position);
    }

    private void allowScrolling() {
        editText.setScrollContainer(true);
        editText.setVerticalScrollBarEnabled(true);
        editText.setScrollbarFadingEnabled(false);
        editText.setGravity(Gravity.TOP);
        int type = InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE;
        editText.setInputType(type);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        view.getParent().requestDisallowInterceptTouchEvent(true);
        if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            view.getParent().requestDisallowInterceptTouchEvent(false);
        }
        return false;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!restoreState) {
            editText.setText("");
        }
        super.onRestoreInstanceState(state);
    }

}