package com.garethevans.church.opensongtablet.customviews;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

public abstract class BottomSheetCommon extends BottomSheetDialogFragment {

    /**
     * Override this to provide your manual day/night mode.
     * Return true for dark mode, false for light mode.
     */

    protected boolean dark = true;
    private MainActivityInterface mainActivityInterface;
    @SuppressWarnings("FieldCanBeLocal")
    private final String PREF_NAME="theme_choice", DARK="dark";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mainActivityInterface = (MainActivityInterface) getContext();
        if (getContext() != null) {
            dark = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .getBoolean(DARK, false);
        }
        int themeRes = dark
                ? R.style.MyManualBottomSheetTheme_Dark
                : R.style.MyManualBottomSheetTheme_Light;

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), themeRes);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            //FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null && getContext()!=null) {

                // Create a rounded MaterialShapeDrawable
                float radius = getResources().getDimension(R.dimen.bottom_sheet_corner)*2;

                MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable();
                shapeDrawable.setShapeAppearanceModel(
                        shapeDrawable.getShapeAppearanceModel()
                                .toBuilder()
                                .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                                .build()
                );

                // Set fill color to match the theme
                shapeDrawable.setFillColor(ColorStateList.valueOf(mainActivityInterface.getPalette().primaryVariant));

                // Apply it
                //ViewCompat.setBackground(bottomSheet, shapeDrawable);
                bottomSheet.setBackground(shapeDrawable);

                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setDraggable(false);
                behavior.setFitToContents(true);
            }
        });
        return dialog;
    }

}