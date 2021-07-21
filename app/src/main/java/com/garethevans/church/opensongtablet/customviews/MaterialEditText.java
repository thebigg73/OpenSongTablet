package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
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
                android.R.attr.typeface};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        CharSequence digits = a.getText(2);
        int lines = a.getInteger(3,0);
        int minLines = a.getInteger(4,0);
        int maxLines = a.getInteger(5,0);
        CharSequence imeOptions = a.getText(6);
        CharSequence inputType = a.getText(7);
        CharSequence typeface = a.getText(8);

        editText = findViewById(R.id.editText);
        editText.setImeActionLabel(context.getString(R.string.select), EditorInfo.IME_ACTION_DONE);
        textInputLayout = findViewById(R.id.holderLayout);

        if (text != null) {
            editText.setText(text);
        }
        if (hint != null) {
            textInputLayout.setHint(hint);
        }
        if (digits != null) {
            editText.setKeyListener(DigitsKeyListener.getInstance(digits.toString()));
        }
        if (inputType != null) {
            int type = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
            if (inputType.equals("textMultiline")) {
                type = InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE;
            }
            editText.setInputType(type);
        }
        if (lines!=0) {
            editText.setLines(lines);
            allowScrolling();
        } else {
            editText.setLines(1);
        }
        if (minLines!=0) {
            editText.setMinLines(minLines);
            allowScrolling();
        }
        if (maxLines>0) {
            editText.setMaxLines(maxLines);
            allowScrolling();
        } else {
            editText.setMaxLines(1);
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
        }
        if (typeface != null && typeface.equals("monospace")) {
            editText.setTypeface(Typeface.MONOSPACE);
        }

        a.recycle();
    }

    public void setText(String text) {
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
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        view.getParent().requestDisallowInterceptTouchEvent(true);
        if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            view.getParent().requestDisallowInterceptTouchEvent(false);
        }
        return false;
    }
}