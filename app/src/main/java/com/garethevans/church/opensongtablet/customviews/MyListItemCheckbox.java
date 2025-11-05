package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;


// Don't this is actually used (instead inflated as view holder in recyclerView adapter
public class MyListItemCheckbox extends LinearLayout {

    private CheckBox checkBox;
    private MyMaterialSimpleTextView textView;
    private MyMaterialSimpleTextView extraText;
    private Palette palette;

    public MyListItemCheckbox(Context context) {
        this(context,null);
    }

    public MyListItemCheckbox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    private void setup(Context c) {
        palette = new Palette(c);
        inflate(c, R.layout.view_checkbox_list_item, this);
        checkBox = findViewById(R.id.checkBoxItem);
        textView = findViewById(R.id.itemName);
        extraText = findViewById(R.id.modifiedDate);

        // By default, the extraText for date is hidden
        extraText.setVisibility(View.GONE);

        // Set the palette colours
        setColors();
    }

    private void setColors() {
        textView.setTextColor(palette.textColor);
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
        extraText.setTextColor(palette.hintColor);
    }


    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }
    public boolean getChecked() {
        return checkBox.isChecked();
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setExtraText(String text) {
        if (text==null || text.isEmpty()) {
            extraText.setVisibility(View.GONE);
        } else {
            extraText.setText(text);
            extraText.setVisibility(View.VISIBLE);
        }
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }
}
