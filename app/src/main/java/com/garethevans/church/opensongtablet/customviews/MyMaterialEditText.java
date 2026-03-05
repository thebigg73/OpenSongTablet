package com.garethevans.church.opensongtablet.customviews;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Field;

public class MyMaterialEditText extends FrameLayout implements View.OnTouchListener {
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyMaterialEditText";
    private final TextInputEditText editText;
    private final TextInputLayout textInputLayout;
    private final boolean restoreState;
    private int endIconMode;
    private Window window;
    private WindowInsetsCompat windowInsetsCompat;
    private boolean isKeyboardVisible = false;
    private final Handler keyboardHandler = new Handler(Looper.getMainLooper());
    private View.OnFocusChangeListener externalFocusChangeListener;
    private Palette palette;

    // By default this is a single line edit text
    // For multiline, the number of lines has to be specified (maxLines/lines)
    // The lines has to be greater than 1

    public MyMaterialEditText(Context context) {
        super(context);
        palette = new Palette(context);
        editText = new TextInputEditText(context);
        textInputLayout = new TextInputLayout(context);
        textInputLayout.addView(editText,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.addView(textInputLayout,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        restoreState = true;

        //editText.setId(View.generateViewId());
        //textInputLayout.setId(View.generateViewId());

        try {
            window = ((Activity) context).getWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //setLongClickKeyboard();
    }
    public MyMaterialEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        palette = new Palette(context);

        // Ensure inflation happens with the current app theme
        //ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, context.getTheme());
        //LayoutInflater.from(themeWrapper).inflate(R.layout.view_material_edittext, this, true);

        inflate(context, R.layout.view_material_edittext, this);

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
                android.R.attr.layout_gravity,
                R.attr.removeStyle};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        CharSequence digits = a.getText(2);
        int lines = a.getInt(3, 1);
        int minLines = a.getInt(4, 1);
        int maxLines = a.getInt(5, 1);
        int imeOptions = a.getInt(6, EditorInfo.IME_ACTION_DONE|EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        int inputType = a.getInt(7, InputType.TYPE_CLASS_TEXT);
        restoreState = a.getBoolean(8, true);
        endIconMode = a.getInt(9, TextInputLayout.END_ICON_NONE);
        boolean useMonospace = a.getBoolean(10, false);
        CharSequence suffixText = a.getText(11);
        CharSequence helperText = a.getText(12);
        int gravity = a.getInt(13, Gravity.TOP);
        boolean removeStyle = a.getBoolean(14, false);

        editText = findViewById(R.id.editText);
        textInputLayout = findViewById(R.id.holderLayout);
        editText.setTextInputLayoutFocusedRectEnabled(false);

        editText.setId(View.generateViewId());
        textInputLayout.setId(View.generateViewId());
        textInputLayout.setFocusable(false);
        textInputLayout.setFocusableInTouchMode(false);

        // Left align
        editText.setGravity(Gravity.START);
        textInputLayout.setGravity(Gravity.START);

        // Set the text
        if (text != null) {
            setText(text.toString());
        }
        if (hint != null) {
            setHint(hint.toString());
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
            imeOptions = imeOptions & (EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                editText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                textInputLayout.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
                // If you have access to the layout container:
                // myView.filenameInputLayout.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            }
        //    editText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        } else if (inputType == InputType.TYPE_CLASS_NUMBER) {
            editText.setInputType(inputType);
        } else {
            editText.setInputType(inputType);
            //editText.setImeOptions(imeOptions);
            editText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Clear focus here from edittext
                    editText.clearFocus();
                }
                return false;
            });
        }


        if (useMonospace) {
            Typeface mono = Typeface.createFromAsset(context.getAssets(), "font/Inconsolata.ttf");
            editText.setTypeface(mono);
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

        setEndIconMode(endIconMode);

        editText.setImeOptions(imeOptions);

        a.recycle();

        try {
            window = ((Activity) unwrap(context)).getWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setPalette();

        // === Keyboard handling fixes (Kindle, pre-Lollipop, etc.) ===
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Small delay to let window settle (important for Fire OS)
                keyboardHandler.postDelayed(this::showKeyboard, 120);
            } else {
                isKeyboardVisible = false;
            }
            if (useMonospace) {
                Typeface mono = Typeface.createFromAsset(context.getAssets(), "font/Inconsolata.ttf");
                editText.setTypeface(mono);
            }
        });

        // Track IME visibility
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            windowInsetsCompat = insets;
            return insets;
        });

    }

    public void showKeyboard() {
        Log.d(TAG, "Manually forcing keyboard (using editText)");
        editText.requestFocus();
        post(() -> {
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    public void setEndIconMode(int endIconMode) {
        this.endIconMode = endIconMode;
        textInputLayout.setEndIconMode(endIconMode);
    }

    private Context unwrap(Context context) {
        int depth = 0;
        while (context instanceof ContextWrapper && !(context instanceof Activity) && depth < 10) {
            context = ((ContextWrapper) context).getBaseContext();
            depth++;
        }
        return context;
    }

    public void setText(String text) {
        if (text!=null && text.isEmpty()) {
            text = null;
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
        textInputLayout.setHintTextColor(ColorStateList.valueOf(palette.hintColor));
    }

    public void setOnEditorActionListener(MyMaterialSimpleTextView.OnEditorActionListener editorActionListener) {
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

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        this.externalFocusChangeListener = listener;
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            handleFocusChange(hasFocus);
            if (externalFocusChangeListener != null) {
                externalFocusChangeListener.onFocusChange(v, hasFocus);
            }
        });
    }

    private void handleFocusChange(boolean hasFocus) {
        if (hasFocus) {
            keyboardHandler.postDelayed(this::showKeyboard, 120);
        } else {
            isKeyboardVisible = false;
        }
    }

    public void setTypeface(Typeface typeface) {
        editText.setTypeface(typeface);
    }

    public Layout getLayout() {
        return editText.getLayout();
    }

    public void setErrorEnabled(boolean errorEnabled) {
        textInputLayout.setErrorEnabled(errorEnabled);
    }
    public void setError(String error) {
        textInputLayout.setError(error);
    }

    public void setTextColor(int color) {
        editText.setTextColor(color);
    }
    public void setHintColor(int color) {
        textInputLayout.setHintTextColor(ColorStateList.valueOf(color));
    }

    public void setGravity(int gravity) {
        editText.setGravity(gravity);
    }

    public void setPalette() {
        textInputLayout.setHintTextColor(ColorStateList.valueOf(palette.hintColor));
        editText.setHintTextColor(palette.hintColor);
        editText.setHintTextColor(ColorStateList.valueOf(palette.hintColor));
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(palette.hintColor));
        textInputLayout.setBoxStrokeWidth(2);
        textInputLayout.setBoxStrokeColor(palette.hintColor);
        editText.setTextColor(palette.textColor);
        editText.setHighlightColor(palette.secondary);
        tintDrawables();
    }

    private void tintDrawables() {
        if (Build.MANUFACTURER.toLowerCase().contains("amazon")) {
            textInputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
            Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.keyboard);
            if (drawable!=null) {
                DrawableCompat.setTint(drawable,palette.textColor);
            }
            textInputLayout.setEndIconDrawable(drawable); // your keyboard icon
            textInputLayout.setEndIconOnClickListener(v -> {
                Log.d(TAG, "End icon pressed: showing keyboard");
                showKeyboard();
            });
        }

        tintEditTextCursorAndHandles(palette);
    }

    private void tintEditTextCursorAndHandles(Palette palette) {
        try {
            // Load and mutate drawables
            Drawable handleLeft = AppCompatResources.getDrawable(editText.getContext(), R.drawable.text_select_left_handle);
            Drawable handleRight = AppCompatResources.getDrawable(editText.getContext(), R.drawable.text_select_right_handle);
            Drawable handleMiddle = AppCompatResources.getDrawable(editText.getContext(), R.drawable.text_select_handle_middle);
            Drawable cursor = AppCompatResources.getDrawable(editText.getContext(), R.drawable.text_cursor);

            if (handleLeft != null) {
                handleLeft = handleLeft.mutate();
            }
            if (handleRight != null) {
                handleRight = handleRight.mutate();
            }
            if (handleMiddle != null) {
                handleMiddle = handleMiddle.mutate();
            }
            if (cursor != null) {
                cursor = cursor.mutate();
            }

            // Apply palette colors
            if (handleLeft != null) {
                handleLeft.setColorFilter(palette.secondary, PorterDuff.Mode.SRC_IN);
            }
            if (handleRight != null) {
                handleRight.setColorFilter(palette.secondary, PorterDuff.Mode.SRC_IN);
            }
            if (handleMiddle != null) {
                handleMiddle.setColorFilter(palette.secondary, PorterDuff.Mode.SRC_IN);
            }
            if (cursor != null) {
                cursor.setColorFilter(palette.hintColor, PorterDuff.Mode.SRC_IN);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Direct API
                if (handleLeft != null) {
                    editText.setTextSelectHandleLeft(handleLeft);
                }
                if (handleRight != null) {
                    editText.setTextSelectHandleRight(handleRight);
                }
                if (handleMiddle != null) {
                    editText.setTextSelectHandle(handleMiddle);
                }
                if (cursor != null) {
                    editText.setTextCursorDrawable(cursor);
                }
            } else {
                // Reflection for older versions
                Field fEditor = editText.getClass().getDeclaredField("mEditor");
                fEditor.setAccessible(true);
                Object editor = fEditor.get(editText);

                if (editor != null) {
                    Field fCursorDrawable = editor.getClass().getDeclaredField("mCursorDrawable");
                    fCursorDrawable.setAccessible(true);
                    if (cursor != null) {
                        Drawable[] cursors = {cursor, cursor};
                        fCursorDrawable.set(editor, cursors);
                    }

                    Field fSelectHandleLeft = editor.getClass().getDeclaredField("mSelectHandleLeft");
                    Field fSelectHandleRight = editor.getClass().getDeclaredField("mSelectHandleRight");
                    Field fSelectHandleCenter = editor.getClass().getDeclaredField("mSelectHandleCenter");

                    fSelectHandleLeft.setAccessible(true);
                    fSelectHandleRight.setAccessible(true);
                    fSelectHandleCenter.setAccessible(true);

                    if (handleLeft != null) {
                        fSelectHandleLeft.set(editor, handleLeft);
                    }
                    if (handleRight != null) {
                        fSelectHandleRight.set(editor, handleRight);
                    }
                    if (handleMiddle != null) {
                        fSelectHandleCenter.set(editor, handleMiddle);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
