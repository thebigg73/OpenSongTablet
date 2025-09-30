package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;

public class ExposedDropDown extends FrameLayout {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ExposedDropDown";

    private Context c;
    private boolean largePopups = false;
    private boolean sizeSet = false;
    private boolean dealingWithAlready = false;
    private ArrayList<String> arrayList = null;

    private WindowInsetsCompat windowInsetsCompat;
    private WindowInsetsControllerCompat windowInsetsControllerCompat;
    private Window window;

    private boolean userEditing = true;

    private final AutoCompleteTextView autoCompleteTextView;
    private final TextInputLayout textInputLayout;
    public ExposedDropDown(@NonNull Context context) {
        this(context, null);
    }

    public ExposedDropDown(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.textInputStyle);
    }

    @SuppressLint("ClickableViewAccessibility")
    public ExposedDropDown(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, context.getTheme());
        LayoutInflater.from(themeWrapper).inflate(R.layout.view_exposed_dropdown, this, true);

        this.c = context;

        textInputLayout = findViewById(R.id.holderView);
        autoCompleteTextView = findViewById(R.id.textView);

        autoCompleteTextView.setDropDownBackgroundResource(R.drawable.popup_bg);

        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            windowInsetsCompat = insets;
            return insets;
        });

        if (window != null && window.getDecorView() != null) {
            windowInsetsControllerCompat = WindowCompat.getInsetsController(window, window.getDecorView());
        }

        // Read XML attributes
        int[] set = new int[]{android.R.attr.text, android.R.attr.hint};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        a.recycle();

        if (text != null) {
            setText(text.toString());
        }
        if (hint != null) {
            setHint(hint.toString());
        }

        autoCompleteTextView.setOnTouchListener(new MyTouchListener());
        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);

        textInputLayout.setEndIconTintList(ColorStateList.valueOf(MaterialColors.getColor(autoCompleteTextView, com.google.android.material.R.attr.colorOnPrimary)));
        textInputLayout.setEndIconOnClickListener(v -> doClickAction());

    }

    private Context unwrap(Context context) {
        while (!(context instanceof Activity) && context != null) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        return context;
    }

    private class MyTouchListener implements OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if ((event.getAction() == MotionEvent.ACTION_DOWN ||
                    event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) &&
                    largePopups && !sizeSet) {
                setPopupSize();
            }

            if (windowInsetsCompat != null && windowInsetsCompat.isVisible(WindowInsetsCompat.Type.ime()) &&
                    windowInsetsControllerCompat != null) {
                windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.ime());
            }

            if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
                postDelayed(ExposedDropDown.this::doClickAction, 100);
            }
            return true;
        }
    }

    private void doClickAction() {
        if (!dealingWithAlready) {
            dealingWithAlready = true;

            if (windowInsetsCompat != null && windowInsetsCompat.isVisible(WindowInsetsCompat.Type.ime()) &&
                    windowInsetsControllerCompat != null) {
                windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.ime());
            }

            setPopupSize();
            if (autoCompleteTextView.isPopupShowing()) {
                autoCompleteTextView.dismissDropDown();
            } else {
                autoCompleteTextView.postDelayed(() -> {
                    autoCompleteTextView.showDropDown();
                    keepPosition();
                    dealingWithAlready = false;
                }, 100);
            }
        }
    }

    private void keepPosition() {
        if (arrayList != null && !arrayList.isEmpty()) {
            autoCompleteTextView.setListSelection(arrayList.indexOf(getText().toString()));
        }
    }

    public void setArray(Context c, ArrayList<String> objects) {
        this.c = c;
        largePopups = ((MainActivityInterface) c).getPreferences().getMyPreferenceBoolean("largePopups", true);
        arrayList = objects;
    }

    public void setArray(Context c, String[] stringArray) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, stringArray);
        setArray(c, list);
    }

    public void setPopupSize() {
        if (autoCompleteTextView == null || !largePopups) return;
        try {
            MainActivityInterface mainActivityInterface = (MainActivityInterface) unwrap(c);
            int[] location = new int[2];
            autoCompleteTextView.getLocationOnScreen(location);
            autoCompleteTextView.setDropDownVerticalOffset(-getHeight());
            int maxHeight = mainActivityInterface.getDisplayMetrics()[1];
            int heightLeft = maxHeight - location[1];
            int minHeight = (int) getResources().getDimension(R.dimen.exposed_dropdown_height);
            autoCompleteTextView.setDropDownHeight(Math.max(heightLeft, minHeight));
            sizeSet = true;
        } catch (Exception e) {
            sizeSet = false;
            e.printStackTrace();
        }
    }

    // Convenience accessors
    public AutoCompleteTextView getAutoCompleteTextView() { return autoCompleteTextView; }

    public Editable getText() {
        if (autoCompleteTextView.getText() == null) autoCompleteTextView.setText("");
        return autoCompleteTextView.getText();
    }

    public void setText(String text) {
        if (autoCompleteTextView != null) {
            autoCompleteTextView.post(() -> {
                try {
                    autoCompleteTextView.setText(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void setHint(String hint) {
        if (textInputLayout!=null) {
            textInputLayout.post(() -> {
                try {
                    textInputLayout.setHint(hint);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        autoCompleteTextView.addTextChangedListener(textWatcher);
    }

    public boolean getUserEditing() { return userEditing; }

    public void setUserEditing(boolean userEditing) { this.userEditing = userEditing; }

    public void setAdapter(ExposedDropDownArrayAdapter arrayAdapter) {
        autoCompleteTextView.setAdapter(arrayAdapter);
    }

}
