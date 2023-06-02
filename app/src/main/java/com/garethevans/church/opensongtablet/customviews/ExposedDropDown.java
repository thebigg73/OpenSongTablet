package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;

public class ExposedDropDown extends TextInputLayout {

    private AutoCompleteTextView autoCompleteTextView;
    private TextInputLayout textInputLayout;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ExposedDropDown";
    private Context c;
    private boolean largePopups;
    private ArrayList<String> arrayList = null;
    private WindowInsetsCompat windowInsetsCompat;
    private final WindowInsetsControllerCompat windowInsetsControllerCompat;
    private Window window;

    @SuppressLint("ClickableViewAccessibility")
    public ExposedDropDown(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.c = context;

        try {
            window = ((Activity) unwrap(context)).getWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }

        inflate(context, R.layout.view_exposed_dropdown,this);
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            windowInsetsCompat = insets;
            return insets;
                });
        windowInsetsControllerCompat = WindowCompat.getInsetsController(window, window.getDecorView());



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
        autoCompleteTextView.setOnTouchListener(new MyTouchListener());
        textInputLayout.setEndIconOnClickListener(v -> autoCompleteTextView.post(this::doClickAction));

        autoCompleteTextView.setOnClickListener(view -> doClickAction());
    }

    private void doClickAction() {
        if (windowInsetsCompat != null && windowInsetsCompat.isVisible(WindowInsetsCompat.Type.ime()) &&
                windowInsetsControllerCompat != null) {
            windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.ime());
        }

        setPopupSize();
        if (autoCompleteTextView.isPopupShowing()) {
            autoCompleteTextView.dismissDropDown();
        } else {
            autoCompleteTextView.dismissDropDown();
            // Delay the showing..
            autoCompleteTextView.postDelayed(() -> {
                autoCompleteTextView.showDropDown();
                keepPosition();
            },100);
        }
    }
    private class MyTouchListener implements OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (windowInsetsCompat != null && windowInsetsCompat.isVisible(WindowInsetsCompat.Type.ime()) &&
                    windowInsetsControllerCompat != null) {
                windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.ime());
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN ||
                    event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                setPopupSize();
            }
            return false;
        }
    }

    private void keepPosition() {
        if (arrayList!=null && arrayList.size()>0) {
            autoCompleteTextView.setListSelection(arrayList.indexOf(getText().toString()));
        }
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
        autoCompleteTextView.clearFocus();
    }
    public void setHint(String hint) {
        textInputLayout.setHint(hint);
        textInputLayout.clearFocus();
    }
    public void setAdapter(ExposedDropDownArrayAdapter arrayAdapter) {
        autoCompleteTextView.setAdapter(arrayAdapter);
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        autoCompleteTextView.addTextChangedListener(textWatcher);
    }

    public void setArray(Context c, String[] stringArray) {
        this.c = c;
        largePopups = ((MainActivityInterface) c).getPreferences().getMyPreferenceBoolean("largePopups",true);
        arrayList = new ArrayList<>();
        Collections.addAll(arrayList, stringArray);
    }

    public void setArray(Context c, ArrayList<String> objects) {
        this.c = c;
        largePopups = ((MainActivityInterface) c).getPreferences().getMyPreferenceBoolean("largePopups",true);
        arrayList = objects;
    }

    private Context unwrap(Context context) {
        while (!(context instanceof Activity) && context != null) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        return context;
    }
    public void setPopupSize() {
        MainActivityInterface mainActivityInterface = null;
        try {
            mainActivityInterface = (MainActivityInterface) unwrap(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (autoCompleteTextView != null) {
            if (largePopups && mainActivityInterface != null) {
                try {
                    // Get the location of the popup position and negatively offset this minus the toolbar height
                    int[] location = new int[2];
                    autoCompleteTextView.getLocationOnScreen(location);
                    int height = mainActivityInterface.getDisplayMetrics()[1] - (mainActivityInterface.getToolbar().getActionBarHeight(false) * 2);
                    int y = location[1];
                    autoCompleteTextView.setDropDownVerticalOffset(-y);
                    autoCompleteTextView.setDropDownHeight(height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    autoCompleteTextView.setDropDownVerticalOffset(-autoCompleteTextView.getHeight());
                    int newHeight = (int) getContext().getResources().getDimension(R.dimen.exposed_dropdown_height);
                    autoCompleteTextView.setDropDownHeight(newHeight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
