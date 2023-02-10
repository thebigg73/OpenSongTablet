package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.TypedValue;
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

public class MyMaterialEditText extends LinearLayout implements View.OnTouchListener {

    private final TextInputEditText editText;
    private final TextInputLayout textInputLayout;
    private final boolean restoreState;
    private final float xxlarge, xlarge, large, medium, small, xsmall;

    // By default this is a single line edit text
    // For multiline, the number of lines has to be specified (maxLines/lines)
    // The lines has to be greater than 1

    public MyMaterialEditText(Context context) {
        super(context);
        editText = new TextInputEditText(context);
        textInputLayout = new TextInputLayout(context);
        restoreState = true;

        xxlarge = context.getResources().getDimension(R.dimen.text_xxlarge);
        xlarge = context.getResources().getDimension(R.dimen.text_xlarge);
        large = context.getResources().getDimension(R.dimen.text_large);
        medium = context.getResources().getDimension(R.dimen.text_medium);
        small = context.getResources().getDimension(R.dimen.text_small);
        xsmall = context.getResources().getDimension(R.dimen.text_xsmall);
    }
    public MyMaterialEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_material_edittext, this);

        xxlarge = context.getResources().getDimension(R.dimen.text_xxlarge);
        xlarge = context.getResources().getDimension(R.dimen.text_xlarge);
        large = context.getResources().getDimension(R.dimen.text_large);
        medium = context.getResources().getDimension(R.dimen.text_medium);
        small = context.getResources().getDimension(R.dimen.text_small);
        xsmall = context.getResources().getDimension(R.dimen.text_xsmall);

        int[] set = new int[]{android.R.attr.text,
                android.R.attr.hint,
                android.R.attr.digits,
                android.R.attr.lines,
                android.R.attr.minLines,
                android.R.attr.maxLines,
                android.R.attr.imeOptions,
                android.R.attr.inputType,
                android.R.attr.saveEnabled,
                com.google.android.material.R.attr.endIconMode,
                R.attr.useMonospace,
                com.google.android.material.R.attr.suffixText,
                com.google.android.material.R.attr.helperText,
                android.R.attr.layout_gravity};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        CharSequence digits = a.getText(2);
        int lines = a.getInt(3, 1);
        int minLines = a.getInt(4, 1);
        int maxLines = a.getInt(5, 1);
        int imeOptions = a.getInt(6, EditorInfo.IME_ACTION_DONE);
        int inputType = a.getInt(7, InputType.TYPE_CLASS_TEXT);
        restoreState = a.getBoolean(8, true);
        int endIconMode = a.getInt(9, TextInputLayout.END_ICON_NONE);
        boolean useMonospace = a.getBoolean(10, false);
        CharSequence suffixText = a.getText(11);
        CharSequence helperText = a.getText(12);
        int gravity = a.getInt(13, Gravity.TOP);

        editText = findViewById(R.id.editText);
        textInputLayout = findViewById(R.id.holderLayout);

        editText.setId(View.generateViewId());
        textInputLayout.setId(View.generateViewId());

        // Left align
        editText.setGravity(Gravity.START);
        textInputLayout.setGravity(Gravity.START);

        // Set the text
        if (text != null) {
            editText.setText(text);
        }
        if (hint != null) {
            textInputLayout.setHint(hint);
        }
        if (digits != null) {
            editText.setKeyListener(DigitsKeyListener.getInstance(digits.toString()));
        }

        if (lines > minLines) {
            minLines = lines;
        } else if (minLines > lines) {
            lines = minLines;
        }
        editText.setLines(lines);
        editText.setMinLines(minLines);
        if (maxLines > 1) {
            editText.setMaxLines(maxLines);
        }

        // Now figure out the inputType to use
        if (inputType == InputType.TYPE_TEXT_FLAG_MULTI_LINE) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            editText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        } else if (inputType == InputType.TYPE_CLASS_NUMBER) {
            editText.setInputType(inputType);
        } else {
            editText.setInputType(inputType);
            editText.setImeOptions(imeOptions);
            editText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Clear focus here from edittext
                    editText.clearFocus();
                }
                return false;
            });
        }


        if (useMonospace) {
            editText.setTypeface(Typeface.MONOSPACE);
        }
        if (suffixText != null) {
            textInputLayout.setSuffixText(suffixText);
        }
        if (helperText != null) {
            textInputLayout.setHelperTextEnabled(true);
            textInputLayout.setHelperText(helperText);
        }

        // By default restore the state/temp text for rotating, etc.
        // Can override if a fragment is reused
        editText.setSaveEnabled(restoreState);

        editText.setGravity(gravity);

        textInputLayout.setEndIconMode(endIconMode);
        a.recycle();
    }

    public void setText(String text) {
        if (text == null || text.isEmpty()) {
            editText.setText(null);
        }
        editText.setText(text);
    }

    public Editable getText() {
        if (editText.getText() == null) {
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
    public int getSelectionEnd() {
        return editText.getSelectionEnd();
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

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!restoreState) {
            editText.setText("");
        }
        super.onRestoreInstanceState(state);
    }

    public int getLines() {
        return editText.getLineCount();
    }

    public int getMinLines() {
        return editText.getMinLines();
    }

    public int getInputType() {
        return editText.getInputType();
    }

    public void setInputType(int inputType) {
        editText.setInputType(inputType);
    }

    public void setImeOptions(int imeOptions) {
        editText.setImeOptions(imeOptions);
    }

    public void setHorizontallyScrolling(boolean horizontallyScrolling) {
        editText.setHorizontallyScrolling(horizontallyScrolling);
    }

    public void setAutoSizeTextTypeUniformWithConfiguration(int minTextSize, int maxTextSize, int stepSize) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editText.setAutoSizeTextTypeUniformWithConfiguration(minTextSize, maxTextSize, stepSize, TypedValue.COMPLEX_UNIT_SP);
        }
    }

    public void setTextSize(float textSize) {
        editText.setTextSize(textSize);
    }

    public void setSelected(boolean selected) {
        editText.setSelected(selected);
    }

    public void setSelection(int start, int end) {
        editText.setSelection(start,end);
    }

    public void setEnabled(boolean enabled) {
        editText.setEnabled(enabled);
    }

    public void setFocusable(boolean focusable) {
        editText.setClickable(!focusable);
        editText.setFocusable(focusable);
        if (!focusable) {
            editText.setInputType(InputType.TYPE_NULL);
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        textInputLayout.setOnClickListener(onClickListener);
        editText.setOnClickListener(onClickListener);
    }

    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        editText.setOnFocusChangeListener(onFocusChangeListener);
    }

    public void setTypeface(Typeface typeface) {
        editText.setTypeface(typeface);
    }
}