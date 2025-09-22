package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
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

        //applyThemeColors();
    }

    private void applyThemeColors() {
        /*int textColor = MaterialColors.getColor(this, android.R.attr.textColor, Color.BLACK);
        int hintColor = MaterialColors.getColor(this, android.R.attr.textColorHint, Color.GRAY);

        textView.setTextColor(textColor);
        hintView.setTextColor(hintColor);*/

        // Resolve from current theme
        TypedValue typedValue = new TypedValue();
        Context context = getContext();

        // text color
        int textColor = Color.BLACK;
        if (context.getTheme().resolveAttribute(android.R.attr.textColor, typedValue, true)) {
            textColor = typedValue.data;
        }

        // hint color
        int hintColor = Color.GRAY;
        if (context.getTheme().resolveAttribute(android.R.attr.textColorHint, typedValue, true)) {
            hintColor = typedValue.data;
        }

        textView.setTextColor(textColor);
        hintView.setTextColor(hintColor);
    }

    // Public setters
    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setHint(CharSequence hint) {
        hintView.setText(hint);
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

/*
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
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MyMaterialTextView extends LinearLayout {

    private final TextView textView;
    private final TextView hintView;
    private final ImageView checkMark, imageView;
    private final CheckBox checkBox;
    private final FrameLayout checkBoxHolder;
    private final FloatingActionButton endActionButton;
    private final float xxlarge, xlarge, large, medium, small, xsmall;

    public MyMaterialTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_material_textview, this);

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
        checkBox = findViewById(R.id.checkBox);
        checkBoxHolder = findViewById(R.id.checkBoxHolder);
        endActionButton = findViewById(R.id.endActionButton);

        textView.setId(View.generateViewId());
        hintView.setId(View.generateViewId());
        imageView.setId(View.generateViewId());
        checkMark.setId(View.generateViewId());
        checkBox.setId(View.generateViewId());
        checkBoxHolder.setId(View.generateViewId());
        endActionButton.setId(View.generateViewId());

        int[] set = new int[] {android.R.attr.text, android.R.attr.hint};
        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyMaterialTextView);
        Drawable drawable = a.getDrawable(R.styleable.MyMaterialTextView_mydrawable);
        boolean isChecked = a.getBoolean(R.styleable.MyMaterialTextView_showCheckMark,false);
        boolean isCheckBox = a.getBoolean(R.styleable.MyMaterialTextView_showCheckBox, false);
        boolean endFABVisibility = a.getBoolean(R.styleable.MyMaterialTextView_endFABVisibility, false);
        Drawable endFABDrawable = a.getDrawable(R.styleable.MyMaterialTextView_endFABDrawable);
        String size = a.getString(R.styleable.MyMaterialTextView_size);
        if (size==null) {
            size = "medium";
        }
        setSize(size);

        String mainText = typedArray.getString(0);
        textView.setText(mainText);

        String hintText = typedArray.getString(1);
        hintView.setText(hintText);

        // Default colour is white, but it can be overriden programmatically
        setImageView(drawable, 0xffffffff);

        showCheckMark(isChecked);
        showCheckBox(isCheckBox);
        showEndFAB(endFABVisibility,endFABDrawable);

        typedArray.recycle();
        a.recycle();
    }

    public void setHint(String hintText) {
        hintView.post(() -> {
            if (hintText==null) {
                hintView.setVisibility(View.GONE);
            } else {
                hintView.setVisibility(View.VISIBLE);
            }
            hintView.setText(hintText);
        });
    }

    public void setHintMonospace() {
        hintView.post(() -> hintView.setTypeface(Typeface.MONOSPACE));
    }

    public void setSize(String size) {
        float textSize, hintSize;

        switch(size) {
            case "xxlarge":
                textSize = xxlarge;
                hintSize = xlarge;
                break;
            case "xlarge":
                textSize = xlarge;
                hintSize = large;
                break;
            case "large":
                textSize = large;
                hintSize = medium;
                break;
            case "small":
                textSize = small;
                hintSize = xsmall;
                break;
            case "xsmall":
                textSize = xsmall;
                hintSize = xsmall-1;
                break;
            case "medium":
            default:
                textSize = medium;
                hintSize = small;
                break;
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
        hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX,hintSize);
    }

    public void setHintColor(int color) {
        hintView.post(() -> hintView.setTextColor(color));
    }

    public void setText(String mainText) {
        textView.post(() -> {
            if (mainText==null) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
            }
            textView.setText(mainText);
        });
    }

    public void setTextColor(int color) {
        textView.post(() -> textView.setTextColor(color));
    }

    public void showCheckMark(boolean isChecked) {
        if (isChecked) {
            checkMark.setVisibility(View.VISIBLE);
        } else {
            checkMark.setVisibility(View.GONE);
        }
    }

    public void showCheckBox(boolean isCheckBox) {
        if (isCheckBox) {
            checkBoxHolder.setVisibility(View.VISIBLE);
        } else {
            checkBoxHolder.setVisibility(View.GONE);
        }
    }

    public void showEndFAB(boolean endFABVisibility, Drawable endFABDrawable) {
        endActionButton.setVisibility(endFABVisibility ? View.VISIBLE:View.GONE);
        endActionButton.setImageDrawable(endFABDrawable);
    }

    public void setEndFABDrawable(Drawable drawable) {
        if (drawable!=null) {
            endActionButton.setImageDrawable(drawable);
            endActionButton.setVisibility(View.VISIBLE);
        } else {
            endActionButton.setVisibility(View.GONE);
        }
    }

    public void setCheckBox(boolean checked) {
        checkBox.setChecked(checked);
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public CharSequence getText() {
        return textView.getText();
    }

    public CharSequence getHint() {
        return hintView.getText();
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(Drawable drawable, int tintColor) {
        //RequestOptions requestOptions = new RequestOptions().override(64, 64).centerInside();
        if (drawable!=null) {
            // Clone the drawable
            Drawable cloneDrawable = drawable.mutate();
            // Setup color filter for tinting
            ColorFilter colorFilter = new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
            if (cloneDrawable != null) {
                cloneDrawable.setColorFilter(colorFilter);
                imageView.setMaxWidth(64);
                imageView.setMaxHeight(64);
                imageView.setImageDrawable(cloneDrawable);
            }
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    public void setMyGravity(int gravity) {
        textView.setGravity(gravity);
        hintView.setGravity(gravity);
    }

    public void setHorizontalScroll(boolean horizontalScroll) {
        textView.setHorizontallyScrolling(horizontalScroll);
        hintView.setHorizontallyScrolling(horizontalScroll);
        if (horizontalScroll) {
            textView.setEllipsize(TextUtils.TruncateAt.END);
            hintView.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
    }
}
*/
