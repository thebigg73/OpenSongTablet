package com.garethevans.church.opensongtablet.songprocessing;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetImageAdjustBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.OutputStream;

public class ImageAdjustBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BottomSheetDialogFrag";
    private BottomSheetImageAdjustBinding myView;
    private MainActivityInterface mainActivityInterface;
    private Song thisSong;
    private String crop_image_string, crop_website;
    private int originalWidth, originalHeight;

    @SuppressWarnings("unused")
    ImageAdjustBottomSheet() {
        dismiss();
    }

    ImageAdjustBottomSheet(Song thisSong) {
        this.thisSong = thisSong;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        prepareStrings(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        prepareStrings(getContext());

        myView = BottomSheetImageAdjustBinding.inflate(inflater,container,false);
        myView.dialogHeading.setWebHelp(mainActivityInterface,crop_website);
        myView.dialogHeading.setClose(this);

        // Prepare views
        prepareViews();

        // Prepare listeners
        prepareListeners();

        return myView.getRoot();

    }

    private void prepareStrings(Context c) {
        if ( c != null) {
            crop_image_string = c.getString(R.string.image_adjust);
            crop_website = c.getString(R.string.website_image_adjust);
        }
    }
    private void prepareViews() {
        if (getContext()!=null) {
            try {
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                        thisSong.getFolder(), thisSong.getFilename());

                myView.cropImageView.setImageUriAsync(uri);

                // Get the original sizes
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(
                        getContext().getContentResolver().openInputStream(uri),
                        null,
                        options);
                originalWidth = options.outWidth;
                originalHeight= options.outHeight;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void prepareListeners() {
        // Set up the slider and button listeners
        myView.rotateImageLeft.setOnClickListener(view -> myView.cropImageView.rotateImage(270));
        myView.rotateImageRight.setOnClickListener(view -> myView.cropImageView.rotateImage(90));
        myView.saveChanges.setOnClickListener(view -> doCrop());
    }

    private void doCrop() {
        Bitmap bmp = myView.cropImageView.getCroppedImage();
        if (bmp!=null) {
            Log.d(TAG, "bmp:" + bmp.getWidth() + "x" + bmp.getHeight());
            // Write a temporary version of this image to the export folder.
            // After showing the are you sure prompt, we either cancel (delete the temp file)
            // or we copy the temp file to replace the original one.
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","",thisSong.getFilename());
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,"null","Export","",thisSong.getFilename());
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
            mainActivityInterface.getStorageAccess().writeImage(outputStream,bmp);
            String oldSize = originalWidth + "x" + originalHeight;
            String newSize = bmp.getWidth() + "x" + bmp.getHeight();
            String okString = crop_image_string + ": " + oldSize + " -> " + newSize;
            AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("cropImage",okString,null,null,null,thisSong);
            areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"AreYouSureBottomSheet");
            dismiss();
        }
    }

}
