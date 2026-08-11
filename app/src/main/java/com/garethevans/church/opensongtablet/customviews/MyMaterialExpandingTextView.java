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
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;

public class MyMaterialExpandingTextView extends LinearLayout {

    private final String TAG = "MyMaterialExpandingTextView";
    private MyMaterialSimpleTextView textView;
    private MyMaterialSimpleTextView hintView;
    private ImageView checkMark, imageView, expandArrow;
    private CheckBox checkBox;
    private FrameLayout checkBoxHolder;
    private MyFloatingActionButton endActionButton;

    private float xxlarge, xlarge, large, medium, small, xsmall;
    private boolean isExpanded = false;

    public MyMaterialExpandingTextView(Context context) {
        this(context, null);
    }

    public MyMaterialExpandingTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyMaterialExpandingTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs, defStyleAttr);
    }

    private void initialise(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.view_material_expanding_textview, this);

        xxlarge = context.getResources().getDimension(R.dimen.text_xxlarge);
        xlarge = context.getResources().getDimension(R.dimen.text_xlarge);
        large = context.getResources().getDimension(R.dimen.text_large);
        medium = context.getResources().getDimension(R.dimen.text_medium);
        small = context.getResources().getDimension(R.dimen.text_small);
        xsmall = context.getResources().getDimension(R.dimen.text_xsmall);

        textView = findViewById(R.id.textView);
        hintView = findViewById(R.id.hintView);
        imageView = findViewById(R.id.imageView);
        expandArrow = findViewById(R.id.expandArrow);
        checkMark = findViewById(R.id.checkMark);
        checkBoxHolder = findViewById(R.id.checkBoxHolder);
        checkBox = findViewById(R.id.checkBox);

        // Handle Expand/Collapse toggle logic
        expandArrow.setOnClickListener(v -> toggleExpand());

        if (attrs != null) {
            String textValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
            String hintValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "hint");
            if (textValue != null) {
                TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.text});
                textView.setText(a.getString(0));
                a.recycle();
            }
            if (hintValue != null) {
                TypedArray a2 = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.hint});
                setHint(a2.getString(0));
                a2.recycle();
            }

            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyMaterialTextView, defStyleAttr, 0);

            Drawable startIcon = ta.getDrawable(R.styleable.MyMaterialTextView_startIcon);
            if (startIcon != null) {
                imageView.setImageDrawable(startIcon);
                imageView.setVisibility(VISIBLE);
            }

            boolean showCheckbox = ta.getBoolean(R.styleable.MyMaterialTextView_showCheckBox, false);
            checkBoxHolder.setVisibility(showCheckbox ? VISIBLE : GONE);
            ta.recycle();
        } else {
            textView.setVisibility(View.GONE);
            hintView.setVisibility(View.GONE);
            expandArrow.setVisibility(View.GONE);
        }

        setPalette(new Palette(context));
    }

    private void toggleExpand() {
        isExpanded = !isExpanded;
        if (isExpanded) {
            hintView.setMaxLines(Integer.MAX_VALUE);
            hintView.setEllipsize(null);
            expandArrow.setImageResource(android.R.drawable.arrow_up_float);
        } else {
            hintView.setMaxLines(1);
            hintView.setEllipsize(TextUtils.TruncateAt.END);
            expandArrow.setImageResource(android.R.drawable.arrow_down_float);
        }
    }

    private void checkHintLines(CharSequence hintText) {
        if (hintText == null || hintText.toString().isEmpty()) {
            hintView.setVisibility(GONE);
            expandArrow.setVisibility(GONE);
            return;
        }

        hintView.setVisibility(VISIBLE);
        hintView.setText(hintText);

        // Check layout line count post-render to determine if truncation is needed
        hintView.post(() -> {
            if (hintView.getLayout() != null) {
                int lines = hintView.getLayout().getLineCount();
                if (lines > 1 && !isExpanded) {
                    expandArrow.setVisibility(VISIBLE);
                } else if (!isExpanded) {
                    expandArrow.setVisibility(GONE);
                }
            }
        });
    }

    // Public setters
    public void setText(String text) {
        if (textView != null) {
            textView.post(() -> {
                textView.setVisibility(text == null || text.isEmpty() ? GONE : VISIBLE);
                textView.setText(text);
            });
        }
    }

    public void setHint(String text) {
        if (hintView != null) {
            hintView.post(() -> checkHintLines(text));
        }
    }

    public void setText(CharSequence text) {
        setText(text == null ? "" : String.valueOf(text));
    }

    public void setHint(CharSequence hint) {
        setHint(hint == null ? "" : String.valueOf(hint));
    }

    public void setStartIcon(Drawable icon) {
        if (icon != null) {
            imageView.setImageDrawable(icon);
            imageView.setVisibility(VISIBLE);
        } else {
            imageView.setVisibility(GONE);
        }
    }

    public void showCheckmark(boolean show) {
        checkMark.setVisibility(show ? VISIBLE : GONE);
    }

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

    public void setMyGravity(int gravity) {
        textView.setGravity(gravity);
        hintView.setGravity(gravity);
    }

    public void showEndFAB(boolean visible, Drawable drawable) {
        endActionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        endActionButton.setImageDrawable(drawable);
    }

    public void setTextColor(int color) { textView.setTextColor(color); }
    public void setHintColor(int color) { hintView.setTextColor(color); }

    public void setHintMonospace(Context c) {
        hintView.setTypeface(Typeface.MONOSPACE);
    }

    public void setSize(String size) {
        float textSize, hintSize;
        switch (size) {
            case "xxlarge": textSize = xxlarge; hintSize = xlarge; break;
            case "xlarge": textSize = xlarge; hintSize = large; break;
            case "large": textSize = large; hintSize = medium; break;
            case "small": textSize = small; hintSize = xsmall; break;
            case "xsmall": textSize = xsmall; hintSize = xsmall - 1; break;
            case "medium": default: textSize = medium; hintSize = small; break;
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, hintSize);
    }

    public void setPalette(Palette palette) {
        if (palette != null) {
            textView.setTextColor(palette.textColor);
            hintView.setTextColor(palette.hintColor);
            checkMark.setColorFilter(palette.textColor);

            // Tint expand arrow to match the hint/text theme color
            if (expandArrow != null) {
                expandArrow.setColorFilter(palette.hintColor, PorterDuff.Mode.SRC_IN);
            }

            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{-android.R.attr.state_checked}
                    },
                    new int[]{
                            palette.textColor,
                            palette.hintColor
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