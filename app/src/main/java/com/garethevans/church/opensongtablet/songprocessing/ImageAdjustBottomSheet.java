package com.garethevans.church.opensongtablet.songprocessing;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetImageAdjustBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.InputStream;
import java.io.OutputStream;

public class ImageAdjustBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BottomSheetDialogFrag";
    private BottomSheetImageAdjustBinding myView;
    private MainActivityInterface mainActivityInterface;
    private Song thisSong;
    private String crop_image_string, crop_website;
    private int originalWidth, originalHeight, exifRotation=0;

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
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        myView = BottomSheetImageAdjustBinding.inflate(inflater,container,false);
        myView.dialogHeading.setWebHelp(mainActivityInterface,crop_website);
        myView.dialogHeading.setClose(this);
        myView.progressBar.setVisibility(View.GONE);

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            prepareStrings(getContext());

            mainActivityInterface.getMainHandler().post(() -> {
                // Prepare views
                prepareViews();

                // Prepare listeners
                prepareListeners();
            });
        });

    return myView.getRoot();

    }

    private void prepareStrings(Context c) {
        if ( c != null) {
            crop_image_string = c.getString(R.string.image_adjust);
            crop_website = c.getString(R.string.website_image_adjust);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void prepareViews() {
        if (getContext()!=null) {
            try {
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                        thisSong.getFolder(), thisSong.getFilename());

                if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        try {
                            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
                            if (inputStream!=null) {
                                ExifInterface ei = new ExifInterface(inputStream);
                                String attributeOrientation = ei.getAttribute(ExifInterface.TAG_ORIENTATION);
                                if (attributeOrientation != null && !attributeOrientation.isEmpty() &&
                                        !attributeOrientation.replaceAll("\\D", "").isEmpty()) {
                                    int currentOrientation = Integer.parseInt(attributeOrientation.replaceAll("\\D", ""));
                                    switch (currentOrientation) {
                                        case ExifInterface.ORIENTATION_ROTATE_90:
                                            exifRotation = 90;
                                            break;
                                        case ExifInterface.ORIENTATION_ROTATE_180:
                                            exifRotation = 180;
                                            break;
                                        case ExifInterface.ORIENTATION_ROTATE_270:
                                            exifRotation = 270;
                                            break;
                                    }
                                }
                                inputStream.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    int finalRotation = exifRotation;
                    myView.cropImageView.setImageUriAsync(uri);
                    myView.cropImageView.setOnSetImageUriCompleteListener((cropImageView, uri1, e) -> {
                        // Rotate in opposite direction
                        myView.cropImageView.rotateImage(-finalRotation);
                    });


                    // Get the original sizes
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(
                            getContext().getContentResolver().openInputStream(uri),
                            null,
                            options);
                    originalWidth = options.outWidth;
                    originalHeight= options.outHeight;

                } else if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    // Hide the rotate buttons
                    myView.rotateImageLeft.setVisibility(View.GONE);
                    myView.rotateImageRight.setVisibility(View.GONE);
                    ParcelFileDescriptor pfd = mainActivityInterface.getProcessSong().getPDFParcelFileDescriptor(uri);
                    PdfRenderer pdfr = mainActivityInterface.getProcessSong().getPDFRenderer(pfd);
                    PdfRenderer.Page page = mainActivityInterface.getProcessSong().getPDFPage(pdfr,0);
                    Bitmap bmp = mainActivityInterface.getProcessSong().getBitmapFromPDF(thisSong.getFolder(),
                            thisSong.getFilename(),0,page.getWidth(),page.getHeight(),"Y", false);
                    myView.cropImageView.setImageBitmap(bmp);
                    originalWidth = page.getWidth();
                    originalHeight = page.getHeight();
                }

            } catch (Exception e) {
                e.printStackTrace();
                mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void prepareListeners() {
        // Set up the slider and button listeners
        myView.rotateImageLeft.setOnClickListener(view -> mainActivityInterface.getMainHandler().post(() -> myView.cropImageView.rotateImage(270)));
        myView.rotateImageRight.setOnClickListener(view -> mainActivityInterface.getMainHandler().post(() -> myView.cropImageView.rotateImage(90)));
        myView.saveChanges.setOnClickListener(view -> doCrop());
    }

    private void doCrop() {
        myView.progressBar.setVisibility(View.VISIBLE);
        // Put the rotation back to the default (don't change the exif rotation)
        myView.cropImageView.rotateImage(exifRotation);
        myView.cropImageView.setVisibility(View.INVISIBLE);
        Bitmap bmp = myView.cropImageView.getCroppedImage();
        Rect cropPoints = myView.cropImageView.getCropRect();

        if (thisSong.getFiletype().equals("PDF") && cropPoints!=null) {
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                String destClip = "destClip:" + cropPoints.left + "," +
                        cropPoints.top + "," +
                        cropPoints.right + "," +
                        cropPoints.bottom + ";";

                // Set the crop points to the user3 field - overwrite everything else!
                thisSong.setUser3(destClip);
                mainActivityInterface.getSaveSong().updateSong(thisSong, false);

                dismiss();
            });
        } else if (thisSong.getFiletype().equals("IMG") && bmp!=null) {
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                // Write a temporary version of this image to the export folder.
                // After showing the are you sure prompt, we either cancel (delete the temp file)
                // or we copy the temp file to replace the original one.
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", thisSong.getFilename());
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, "null", "Export", "", thisSong.getFilename());
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                mainActivityInterface.getStorageAccess().writeImage(outputStream, bmp);
                String oldSize = originalWidth + "x" + originalHeight;
                String newSize = bmp.getWidth() + "x" + bmp.getHeight();
                String okString = crop_image_string + ": " + oldSize + " -> " + newSize;
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("cropImage", okString, null, null, null, thisSong);
                areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AreYouSureBottomSheet");

                dismiss();
            });
        }
    }
}
