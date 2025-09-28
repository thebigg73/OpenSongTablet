package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

public class MyMaterialTextView extends LinearLayout {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyMaterialTextView";
    private MaterialTextView textView;
    private MaterialTextView hintView;
    private ImageView checkMark, imageView;
    private CheckBox checkBox;
    private FrameLayout checkBoxHolder;
    private FloatingActionButton endActionButton;
    private float xxlarge, xlarge, large, medium, small, xsmall;

    public MyMaterialTextView(Context context) {
        this(context, null);
    }

    public MyMaterialTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyMaterialTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs, defStyleAttr);
    }

    private void initialise(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, context.getTheme());
        LayoutInflater.from(contextWrapper).inflate(R.layout.view_material_textview, this, true);

        xxlarge = context.getResources().getDimension(R.dimen.text_xxlarge);
        xlarge = context.getResources().getDimension(R.dimen.text_xlarge);
        large = context.getResources().getDimension(R.dimen.text_large);
        medium = context.getResources().getDimension(R.dimen.text_medium);
        small = context.getResources().getDimension(R.dimen.text_small);
        xsmall = context.getResources().getDimension(R.dimen.text_xsmall);

        textView = findViewById(R.id.textView);
        hintView = findViewById(R.id.hintView);
        imageView = findViewById(R.id.imageView);
        checkMark = findViewById(R.id.checkMark);
        checkBoxHolder = findViewById(R.id.checkBoxHolder);
        checkBox = findViewById(R.id.checkBox);

        if (attrs != null) {
            // Read framework attrs (android:text, android:hint)
            int[] baseAttrs = new int[]{android.R.attr.text, android.R.attr.hint};
            TypedArray taBase = context.obtainStyledAttributes(attrs, baseAttrs, defStyleAttr, 0);

            String mainText = taBase.getString(0);
            if (mainText != null) textView.setText(mainText);

            String hintText = taBase.getString(1);
            if (hintText != null) hintView.setText(hintText);

            taBase.recycle();

            // Read custom attrs (your attrs.xml defines these)
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyMaterialTextView, defStyleAttr, 0);

            Drawable startIcon = ta.getDrawable(R.styleable.MyMaterialTextView_startIcon);
            if (startIcon != null) {
                imageView.setImageDrawable(startIcon);
                imageView.setVisibility(VISIBLE);
            }

            int showCheckbox = ta.getInt(R.styleable.MyMaterialTextView_showCheckBox, 2);
            if (showCheckbox == 1) {
                checkBoxHolder.setVisibility(VISIBLE);
            } else if (showCheckbox == 0) {
                checkBoxHolder.setVisibility(INVISIBLE);
            } else if (showCheckbox == 2 ){
                checkBoxHolder.setVisibility(GONE);
            }

            ta.recycle();
        }
    }

    // Public setters
    public void setText(CharSequence text) {
        if (textView!=null) {
            try {
                textView.setText(text);
            } catch (Exception e) {
                Log.d(TAG, "Couldn't set text:" + text);
            }
        }
    }

    public void setHint(CharSequence hint) {
        if (hintView!=null) {
            try {
                hintView.setText(hint);
            } catch (Exception e) {
                Log.d(TAG, "Couldn't set hint:" + hint);
            }
        }
    }

    public void setStartIcon(Drawable icon) {
        if (icon != null) {
            imageView.setImageDrawable(icon);
            imageView.setVisibility(VISIBLE);
        } else {
            imageView.setVisibility(GONE);
        }
    }

    // The checkmark is an image tick, not a checkbox that is selectable
    public void showCheckmark(boolean show) {
        checkMark.setVisibility(show ? VISIBLE : GONE);
    }

    // The checkbox is a checkbox option at the end of the view
    public void showCheckbox(boolean show) {
        checkBoxHolder.setVisibility(show ? VISIBLE : GONE);
    }


    public MaterialTextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setCheckBox(boolean checked) { checkBox.setChecked(checked); }
    public boolean isChecked() { return checkBox.isChecked(); }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        checkBox.setOnCheckedChangeListener(listener);
    }

    public CharSequence getText() { return textView.getText(); }
    public CharSequence getHint() { return hintView.getText(); }

    // Gravity
    public void setMyGravity(int gravity) {
        textView.setGravity(gravity);
        hintView.setGravity(gravity);
    }

    // Helpers
    public void setHorizontalScroll(boolean horizontalScroll) {
        textView.setHorizontallyScrolling(horizontalScroll);
        hintView.setHorizontallyScrolling(horizontalScroll);
        if (horizontalScroll) {
            textView.setEllipsize(TextUtils.TruncateAt.END);
            hintView.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    // Checkmark / checkbox / FAB
    public void showEndFAB(boolean visible, Drawable drawable) {
        endActionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        endActionButton.setImageDrawable(drawable);
    }

    // Colors
    public void setTextColor(int color) { textView.setTextColor(color); }
    public void setHintColor(int color) { hintView.setTextColor(color); }

    public void setHintMonospace() { hintView.setTypeface(Typeface.MONOSPACE); }

    // Size
    public void setSize(String size) {
        float textSize, hintSize;
        switch (size) {
            case "xxlarge": textSize = xxlarge; hintSize = xlarge; break;
            case "xlarge": textSize = xlarge; hintSize = large; break;
            case "large": textSize = large; hintSize = medium; break;
            case "small": textSize = small; hintSize = xsmall; break;
            case "xsmall": textSize = xsmall; hintSize = xsmall-1; break;
            case "medium": default: textSize = medium; hintSize = small; break;
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, hintSize);
    }


    // Image
    public void setImageView(Drawable drawable, int tintColor) {
        if (drawable != null) {
            Drawable clone = drawable.mutate();
            ColorFilter filter = new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
            clone.setColorFilter(filter);
            imageView.setImageDrawable(clone);
            imageView.setVisibility(View.VISIBLE);
        } else imageView.setVisibility(View.GONE);
    }
}
