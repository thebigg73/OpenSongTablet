package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;

public class MyMaterialTextView extends LinearLayout {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyMaterialTextView";
    private MyMaterialSimpleTextView textView;
    private MyMaterialSimpleTextView hintView;
    private ImageView checkMark, imageView;
    private CheckBox checkBox;
    private FrameLayout checkBoxHolder;
    private MyFloatingActionButton endActionButton;
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
        inflate(context,R.layout.view_material_textview, this);

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
            // Look for android:textColor in the XML explicitly
            String textValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
            String hintValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android","hint");
            if (textValue != null) {
                // Attribute exists in XML, safe to read
                TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.text});
                textView.setText(a.getString(0));
                a.recycle();
            }
            if (hintValue != null) {
                // Attribute exists in XML, safe to read
                TypedArray a2 = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.hint});
                hintView.setText(a2.getString(0));
                a2.recycle();
            }

            // Read custom attrs (your attrs.xml defines these)
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyMaterialTextView, defStyleAttr, 0);

            Drawable startIcon = ta.getDrawable(R.styleable.MyMaterialTextView_startIcon);
            if (startIcon != null) {
                imageView.setImageDrawable(startIcon);
                imageView.setVisibility(VISIBLE);
            }

            boolean showCheckbox = ta.getBoolean(R.styleable.MyMaterialTextView_showCheckBox, false);
            if (showCheckbox) {
                checkBoxHolder.setVisibility(VISIBLE);
            } else {
                checkBoxHolder.setVisibility(GONE);
            }
            ta.recycle();

        } else {
            textView.setVisibility(View.GONE);
            hintView.setVisibility(View.GONE);
        }

        /*if (attrs != null) {
            // Read framework attrs (android:text, android:hint)
            int[] baseAttrs = new int[]{android.R.attr.text, android.R.attr.hint};
            TypedArray taBase = context.obtainStyledAttributes(attrs, baseAttrs, defStyleAttr, 0);

            String mainText = taBase.getString(0);
            //setText(mainText);
            *//*if (mainText != null) {
                textView.setText(mainText);
            } else {
                textView.setVisibility(View.GONE);
            }*//*

            String hintText = taBase.getString(1);
            setHint(hintText);
            *//*if (hintText != null) hintView.setText(hintText);
*//*
            taBase.recycle();


        } else {

        }*/

        setPalette(new Palette(context));
    }

    // Public setters
    public void setText(String text) {
        if (textView!=null) {
            textView.post(() -> {
                textView.setVisibility(text == null || text.isEmpty() ? GONE : VISIBLE);
                textView.setText(text);
            });
        }
    }
    public void setHint(String text) {
        if (hintView!=null) {
            hintView.post(() -> {
                hintView.setVisibility(text == null || text.isEmpty() ? GONE : VISIBLE);
                hintView.setText(text);
            });
        }
    }

    public void setText(CharSequence text) {
        if (textView!=null) {
            textView.post(() -> {
                setText(text==null ? "" : String.valueOf(text));
            });
        }
    }

    public void setHint(CharSequence hint) {
        if (hintView!=null) {
            hintView.post(() -> {
                setHint(hint==null ? "" : String.valueOf(hint));
            });
            /*hintView.post(() -> {
                hintView.setVisibility(hint!=null ? VISIBLE : GONE);
                try {
                    hintView.setText(String.valueOf(hint==null ? "":hint));
                } catch (Exception e) {
                    Log.d(TAG, "Couldn't set hint:" + hint);
                }
            });*/
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


    public MyMaterialSimpleTextView getTextView() {
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

    public void setHintMonospace(Context c) {
        Typeface mono = Typeface.createFromAsset(c.getAssets(), "font/Inconsolata.ttf");
        hintView.setTypeface(Typeface.MONOSPACE);
    }

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

    public void setPalette(Palette palette) {
        if (palette != null) {
            textView.setTextColor(palette.textColor);
            hintView.setTextColor(palette.hintColor);
            checkMark.setColorFilter(palette.textColor);

            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked}, // checked
                            new int[]{-android.R.attr.state_checked}  // unchecked
                    },
                    new int[]{
                            palette.textColor, // Checked color
                            palette.hintColor  // Unchecked color
                    }
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                checkBox.setButtonTintList(colorStateList);
            } else {
                CompoundButtonCompat.setButtonTintList(checkBox, colorStateList);
            }
        }
    }


}
