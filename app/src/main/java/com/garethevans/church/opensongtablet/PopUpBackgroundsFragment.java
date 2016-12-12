/*
 * Copyright (c) 2015.
 * The code is provided free of charge.  You can use, modify, contribute and improve it as long as this source is referenced.
 * Commercial use should seek permission.
 */

package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class PopUpBackgroundsFragment extends DialogFragment {

    static int whichvideobgpressd;
    String whichCheckBox = "";
    static TextView presoAlphaText;
    static SeekBar presoAlphaProgressBar;


    static PopUpBackgroundsFragment newInstance() {
        PopUpBackgroundsFragment frag;
        frag = new PopUpBackgroundsFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.presoBackground));
        final View V = inflater.inflate(R.layout.popup_projector_background, container, false);

        Button closeFragDialog = (Button) V.findViewById(R.id.closeFragButtonBackground);
        closeFragDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the images and videos chosen (files and which is selected)
                Preferences.savePreferences();
                dismiss();
            }
        });

        ImageView chooseImage1Button = (ImageView) V.findViewById(R.id.chooseImage1Button);
        ImageView chooseImage2Button = (ImageView) V.findViewById(R.id.chooseImage2Button);
        final VideoView chooseVideo1Button = (VideoView) V.findViewById(R.id.chooseVideo1Button);
        final VideoView chooseVideo2Button = (VideoView) V.findViewById(R.id.chooseVideo2Button);
        final CheckBox image1CheckBox = (CheckBox) V.findViewById(R.id.image1CheckBox);
        final CheckBox image2CheckBox = (CheckBox) V.findViewById(R.id.image2CheckBox);
        final CheckBox video1CheckBox = (CheckBox) V.findViewById(R.id.video1CheckBox);
        final CheckBox video2CheckBox = (CheckBox) V.findViewById(R.id.video2CheckBox);
        presoAlphaProgressBar = (SeekBar) V.findViewById(R.id.presoAlphaProgressBar);
        presoAlphaText = (TextView) V.findViewById(R.id.presoAlphaText);
        int getpresoalpha = (int) (FullscreenActivity.presoAlpha*100);
        String valuetext = getpresoalpha+ " %";
        presoAlphaText.setText(valuetext);
        presoAlphaProgressBar.setMax(100);
        presoAlphaProgressBar.setProgress((int) (FullscreenActivity.presoAlpha * 100));
        presoAlphaProgressBar.setOnSeekBarChangeListener(new setAlphaListener());

        // Set the images if they've already been set
        File img1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
        File img2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
        File vid1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo1);
        File vid2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo2);

        if (img1File.isFile()) {
            //Ok file exists.  Try to load it (but beware of errors!
            Bitmap bitmap1 = BitmapFactory.decodeFile(img1File.getAbsolutePath());
            chooseImage1Button.setImageBitmap(bitmap1);
        } else {
            //Ok file doesn't exist.  Use the default icon
            chooseImage1Button.setImageResource(R.drawable.ic_action_picture);
        }
        if (img2File.isFile()) {
            //Ok file exists.  Try to load it (but beware of errors!
            Bitmap bitmap2 = BitmapFactory.decodeFile(img2File.getAbsolutePath());
            chooseImage2Button.setImageBitmap(bitmap2);
        } else {
            //Ok file doesn't exist.  Use the default icon
            chooseImage2Button.setImageResource(R.drawable.ic_action_picture);
        }
        if (vid1File.isFile()) {
            //Ok file exists.  Try to load it (but beware of errors!
            String bgvid1 = vid1File.toString();
            Uri videoUri = Uri.parse(bgvid1);
            chooseVideo1Button.setVideoURI(videoUri);
            chooseVideo1Button.seekTo(100);
        }
        if (vid2File.isFile()) {
            //Ok file exists.  Try to load it (but beware of errors!
            String bgvid2 = vid2File.toString();
            Uri videoUri = Uri.parse(bgvid2);
            chooseVideo2Button.setVideoURI(videoUri);
            chooseVideo2Button.seekTo(100);
        }

        // Set the listeners for the background buttons
        // These bring up a file chooser dialog
        chooseImage1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open another popup listing the files to choose from
                PresenterMode.whatBackgroundLoaded = "image1";
                chooseFile();
            }
        });
        chooseImage2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open another popup listing the files to choose from
                PresenterMode.whatBackgroundLoaded = "image2";
                chooseFile();
            }
        });

        chooseVideo1Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == V.findViewById(R.id.chooseVideo1Button).getId() && event.getAction() == MotionEvent.ACTION_UP) {
                    whichvideobgpressd = 1;
                    PresenterMode.whatBackgroundLoaded = "video1";
                    chooseFile();
                }
                return true;
            }
        });
        chooseVideo2Button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == chooseVideo2Button.getId() && event.getAction() == MotionEvent.ACTION_UP) {
                    whichvideobgpressd = 2;
                    PresenterMode.whatBackgroundLoaded = "video2";
                    chooseFile();
                }
                return true;
            }
        });


        // Check the appropriate background button
        switch (FullscreenActivity.backgroundToUse) {
            case "img1":
                image1CheckBox.setChecked(true);
                break;
            case "img2":
                image2CheckBox.setChecked(true);
                break;
            case "vid1":
                video1CheckBox.setChecked(true);
                break;
            case "vid2":
                video2CheckBox.setChecked(true);
                break;
            case "none":
                // No checked buttons
                break;
        }

        // Set the listeners for the CheckBoxes
        // Ticking it will turn of the project button on the PresenterMode
        // Background is updated

        image1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Call the script to process what to do
                    whichCheckBox = "img1";
                    image2CheckBox.setChecked(false);
                    video1CheckBox.setChecked(false);
                    video2CheckBox.setChecked(false);
                    // Save this to the preferences
                    FullscreenActivity.backgroundTypeToUse = "image";
                    FullscreenActivity.backgroundToUse = whichCheckBox;
                    Preferences.savePreferences();
                    MyPresentation.img1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
                    MyPresentation.imgFile = MyPresentation.img1File;
                    // Try to change the background!
                    if (PresenterMode.numdisplays > 0) {
                        MyPresentation.fixBackground();
                    }

                } else {
                    // We need a background, so if no backgrounds are selected, set it to none!
                    if (!image1CheckBox.isChecked() && !image2CheckBox.isChecked() && !video1CheckBox.isChecked() && !video2CheckBox.isChecked()) {
                        FullscreenActivity.backgroundTypeToUse = "none";
                        FullscreenActivity.backgroundToUse = "none";
                        Preferences.savePreferences();
                        // Try to change the background!
                        if (PresenterMode.numdisplays > 0) {
                            MyPresentation.fixBackground();
                        }
                    }
                    // Been uncheck by checking another option, so do nothing
                }
            }
        });

        image2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Call the script to process what to do
                    whichCheckBox = "img2";
                    image1CheckBox.setChecked(false);
                    video1CheckBox.setChecked(false);
                    video2CheckBox.setChecked(false);
                    // Save this to the preferences
                    FullscreenActivity.backgroundTypeToUse = "image";
                    FullscreenActivity.backgroundToUse = whichCheckBox;
                    Preferences.savePreferences();
                    MyPresentation.img2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
                    MyPresentation.imgFile = MyPresentation.img2File;
                    // Try to change the background!
                    if (PresenterMode.numdisplays > 0) {
                        MyPresentation.fixBackground();
                    }

                } else {
                    // We need a background, so if no backgrounds are selected, set it to none!
                    if (!image1CheckBox.isChecked() && !image2CheckBox.isChecked() && !video1CheckBox.isChecked() && !video2CheckBox.isChecked()) {
                        FullscreenActivity.backgroundTypeToUse = "none";
                        FullscreenActivity.backgroundToUse = "none";
                        Preferences.savePreferences();
                        // Try to change the background!
                        if (PresenterMode.numdisplays > 0) {
                            MyPresentation.fixBackground();
                        }
                    }
                    // Been uncheck by checking another option, so do nothing
                }
            }
        });

        video1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Call the script to process what to do
                    whichCheckBox = "vid1";
                    image1CheckBox.setChecked(false);
                    image2CheckBox.setChecked(false);
                    video2CheckBox.setChecked(false);
                    // Save this to the preferences
                    FullscreenActivity.backgroundTypeToUse = "video";
                    FullscreenActivity.backgroundToUse = whichCheckBox;
                    Preferences.savePreferences();
                    // Try to change the background!
                    if (PresenterMode.numdisplays > 0) {
                        MyPresentation.vidFile = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo1;
                        try {
                            MyPresentation.reloadVideo();
                        } catch (Exception e) {
                            Log.d("e","Problem preparing video");
                        }
                        MyPresentation.fixBackground();
                    }

                } else {
                    // We need a background, so if no backgrounds are selected, set it to none!
                    if (!image1CheckBox.isChecked() && !image2CheckBox.isChecked() && !video1CheckBox.isChecked() && !video2CheckBox.isChecked()) {
                        FullscreenActivity.backgroundTypeToUse = "none";
                        FullscreenActivity.backgroundToUse = "none";
                        Preferences.savePreferences();
                        // Try to change the background!
                        if (PresenterMode.numdisplays > 0) {
                            MyPresentation.fixBackground();
                        }
                    }
                    // Been uncheck by checking another option, so do nothing
                }
            }
        });

        video2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Call the script to process what to do
                    whichCheckBox = "vid2";
                    image1CheckBox.setChecked(false);
                    image2CheckBox.setChecked(false);
                    video1CheckBox.setChecked(false);
                    // Save this to the preferences
                    FullscreenActivity.backgroundTypeToUse = "video";
                    FullscreenActivity.backgroundToUse = whichCheckBox;
                    Preferences.savePreferences();
                    // Try to change the background!
                    if (PresenterMode.numdisplays > 0) {
                        MyPresentation.vidFile = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo2;
                        try {
                            MyPresentation.reloadVideo();
                        } catch (Exception e) {
                            Log.d("e","Problem preparing video");
                        }
                        MyPresentation.fixBackground();
                    }

                } else {
                    // We need a background, so if no backgrounds are selected, set it to none!
                    if (!image1CheckBox.isChecked() && !image2CheckBox.isChecked() && !video1CheckBox.isChecked() && !video2CheckBox.isChecked()) {
                        FullscreenActivity.backgroundTypeToUse = "none";
                        FullscreenActivity.backgroundToUse = "none";
                        Preferences.savePreferences();
                        // Try to change the background!
                        if (PresenterMode.numdisplays > 0) {
                            MyPresentation.fixBackground();
                        }
                        // Been uncheck by checking another option, so do nothing
                    }
                }
            }
        });

        return V;
    }

    public void chooseFile() {
        // This calls the generic file chooser popupFragment
        DialogFragment newFragment = PopUpFileChooseFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
        dismiss();
    }

    private class setAlphaListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FullscreenActivity.presoAlpha = (float)progress / 100f;
            String update = progress + " %";
            presoAlphaText.setText(update);
            MyPresentation.updateAlpha();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            // Save preferences
            Preferences.savePreferences();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}