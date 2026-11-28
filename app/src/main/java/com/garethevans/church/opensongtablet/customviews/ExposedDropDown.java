package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public class ExposedDropDown extends FrameLayout {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "ExposedDropDown";

    private Context c;
    private boolean largePopups = false;
    private boolean sizeSet = false;
    private boolean dealingWithAlready = false;
    private ArrayList<String> arrayList = null;
    private Palette palette;

    private boolean userEditing = true;
    private boolean programmaticChange = false;

    private final AutoCompleteTextView autoCompleteTextView;
    private final TextInputLayout textInputLayout;

    public ExposedDropDown(@NonNull Context context) {
        this(context, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public ExposedDropDown(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_exposed_dropdown, this);

        //window = ((Activity) context).getWindow();
        this.c = context;

        textInputLayout = findViewById(R.id.holderView);
        autoCompleteTextView = findViewById(R.id.textView);

        autoCompleteTextView.setDropDownBackgroundResource(R.drawable.popup_bg);

        // Read XML attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExposedDropDown);
        try {
            String text = a.getString(R.styleable.ExposedDropDown_android_text); // In preview
            autoCompleteTextView.setText(text,false);                                  // For inflation
            String hint = a.getString(R.styleable.ExposedDropDown_android_hint); // In preview
            textInputLayout.setHint(hint);                                       // For inflation
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }

        autoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_medium));
        //autoCompleteTextView.setOnTouchListener(new MyTouchListener());
        autoCompleteTextView.setOnTouchListener(null); // also removes Material's ACTION_UP listener
        autoCompleteTextView.setOnClickListener(v -> doClickAction());
        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);
        textInputLayout.setEndIconOnClickListener(v -> doClickAction());

        if (!isInEditMode()) {
            palette = new Palette(context);
            setPalette();
        }
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

            hideKeyboardIfShowing();

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

            hideKeyboardIfShowing();

            setPopupSize();

            setPopupSize();
            setPopupWidth();

            if (autoCompleteTextView.isPopupShowing()) {
                autoCompleteTextView.dismissDropDown();
                dealingWithAlready = false;
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
    public AutoCompleteTextView getAutoCompleteTextView() {
        return autoCompleteTextView;
    }

    public Editable getText() {
        if (autoCompleteTextView.getText() == null) autoCompleteTextView.setText("",false);
        return autoCompleteTextView.getText();
    }

    public void setText(String text) {
        if (autoCompleteTextView != null) {
            autoCompleteTextView.post(() -> {
                try {
                    autoCompleteTextView.setText(text,false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void setText(String text, boolean filter) {
        if (autoCompleteTextView != null) {
            autoCompleteTextView.post(() -> {
                try {
                    autoCompleteTextView.setText(text, filter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void setHint(String hint) {
        if (textInputLayout != null) {
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

    public boolean getUserEditing() {
        return userEditing;
    }

    public void setUserEditing(boolean userEditing) {
        this.userEditing = userEditing;
    }

    public boolean getProgrammaticChange() {
        return programmaticChange;
    }

    public void setProgrammaticChange(boolean programmaticChange) {
        this.programmaticChange = programmaticChange;
    }

    public void setAdapter(ExposedDropDownArrayAdapter arrayAdapter) {
        autoCompleteTextView.setAdapter(arrayAdapter);
    }

    public void setPalette() {

        autoCompleteTextView.setTextColor(palette.textColor);
        // Tint the popup background
        Drawable drawable = DrawableCompat.wrap(autoCompleteTextView.getDropDownBackground()).mutate();
        DrawableCompat.setTint(drawable, palette.secondary);
        autoCompleteTextView.setDropDownBackgroundDrawable(drawable);
        autoCompleteTextView.invalidate();


        textInputLayout.setHintTextColor(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{palette.hintColor}
        ));
        textInputLayout.setDefaultHintTextColor(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{palette.hintColor}
        ));
        textInputLayout.setEndIconTintList(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{palette.textColor}
        ));

        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxStrokeWidth(2);

        // Focused stroke color
        textInputLayout.setBoxStrokeColor(palette.hintColor);

        // Default (unfocused) stroke color
        try {
            Field defaultStrokeField = TextInputLayout.class.getDeclaredField("defaultStrokeColor");
            defaultStrokeField.setAccessible(true);
            defaultStrokeField.setInt(textInputLayout, palette.hintColor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Force redraw of the outline
        textInputLayout.invalidate();

    }

    public void setPalette(Palette palette) {
        this.palette = palette;
        setPalette();
    }

    private int measureContentWidth() {
        android.widget.ListAdapter adapter = autoCompleteTextView.getAdapter();
        if (adapter == null || adapter.getCount() == 0) return 0;
        int unspec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        FrameLayout parent = new FrameLayout(getContext());
        // Only measure the longest labels, so huge lists stay cheap
        Integer[] pos = new Integer[adapter.getCount()];
        for (int i = 0; i < pos.length; i++) pos[i] = i;
        java.util.Arrays.sort(pos, (a, b) ->
                String.valueOf(adapter.getItem(b)).length() - String.valueOf(adapter.getItem(a)).length());
        int max = 0;
        View recycled = null;
        for (int i = 0; i < Math.min(pos.length, 15); i++) {
            recycled = adapter.getView(pos[i], recycled, parent);
            recycled.measure(unspec, unspec);
            max = Math.max(max, recycled.getMeasuredWidth());
        }
        Drawable bg = autoCompleteTextView.getDropDownBackground();
        if (bg != null) {
            android.graphics.Rect r = new android.graphics.Rect();
            bg.getPadding(r);
            max += r.left + r.right;
        }
        return max;
    }

    public void setPopupWidth() {
        int content = measureContentWidth();
        if (content <= 0) return;
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        int maxWidth = getResources().getDisplayMetrics().widthPixels - 2 * margin;
        // Never narrower than the field; use Math.min(content, maxWidth) if you want it narrower too
        autoCompleteTextView.setDropDownWidth(Math.min(Math.max(content, autoCompleteTextView.getWidth()), maxWidth));
    }

    private void hideKeyboardIfShowing() {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(this);
        if (insets != null && insets.isVisible(WindowInsetsCompat.Type.ime())) {
            Context ctx = unwrap(getContext());
            if (ctx instanceof Activity) {
                WindowCompat.getInsetsController(((Activity) ctx).getWindow(), this)
                        .hide(WindowInsetsCompat.Type.ime());
            }
        }
    }

}
