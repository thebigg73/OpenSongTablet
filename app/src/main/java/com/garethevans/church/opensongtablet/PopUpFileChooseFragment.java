/*
 * Copyright (c) 2015.
 * The code is provided free of charge.  You can use, modify, contribute and improve it as long as this source is referenced.
 * Commercial use should seek permission.
 */

package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PopUpFileChooseFragment extends DialogFragment {

    static ArrayList<String> tempBackgroundImageFiles;
    static ArrayList<String> tempBackgroundVideoFiles;
    static String[] BackgroundImageFiles;
    static PopUpFileChooseFragment newInstance() {
        PopUpFileChooseFragment frag;
        frag = new PopUpFileChooseFragment();
        return frag;
    }
    ListView fileListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Decide on the title of the file chooser
        String myTitle = "";
        switch (PresenterMode.whatBackgroundLoaded) {
            case "image1":
                myTitle = getActivity().getResources().getString(R.string.choose_image1);
                break;
            case "image2":
                myTitle = getActivity().getResources().getString(R.string.choose_image2);
                break;
            case "video1":
                myTitle = getActivity().getResources().getString(R.string.choose_video1);
                break;
            case "video2":
                myTitle = getActivity().getResources().getString(R.string.choose_video2);
                break;
        }

        getDialog().setTitle(getActivity().getResources().getString(R.string.file_chooser));
        View V = inflater.inflate(R.layout.popup_file_chooser, container, false);

        TextView mTitle = (TextView) V.findViewById(R.id.fileChooserTitle);
        mTitle.setText(myTitle);

        Button cancelFileChoose = (Button) V.findViewById(R.id.cancelFileButton);
        cancelFileChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        fileListView = (ListView) V.findViewById(R.id.fileListView);

        // List the images/videos - use extensions
        if (PresenterMode.whatBackgroundLoaded.equals("image1") || PresenterMode.whatBackgroundLoaded.equals("image2")) {
            listImages();
        } else if (PresenterMode.whatBackgroundLoaded.equals("video1") || PresenterMode.whatBackgroundLoaded.equals("video2")) {
            listVideos();
        }

        // Populate the file list view
        fileListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, BackgroundImageFiles));

        // Listen for clicks inside
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the appropriate file
                switch (PresenterMode.whatBackgroundLoaded) {
                    case "image1":
                        FullscreenActivity.backgroundImage1 = BackgroundImageFiles[position];
                        break;
                    case "image2":
                        FullscreenActivity.backgroundImage2 = BackgroundImageFiles[position];
                        break;
                    case "video1":
                        FullscreenActivity.backgroundVideo1 = BackgroundImageFiles[position];
                        break;
                    case "video2":
                        FullscreenActivity.backgroundVideo2 = BackgroundImageFiles[position];
                        break;
                }
                Preferences.savePreferences();
                reOpenBackgrounds();
                dismiss();
            }
        });

        return V;
    }

    public void reOpenBackgrounds() {
        // This reopens the choose backgrounds popupFragment
        DialogFragment newFragment = PopUpBackgroundsFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void listImages() {

            File[] tempmyFiles = FullscreenActivity.dirbackgrounds.listFiles();
            // Go through this list and check if the item is a directory or a file.
            // Add these to the correct array
            int tempnumfiles;
            if (tempmyFiles != null && tempmyFiles.length>0) {
                tempnumfiles = tempmyFiles.length;
            } else {
                tempnumfiles = 0;
            }

            tempBackgroundImageFiles = new ArrayList<>();

            for (int x=0; x<tempnumfiles; x++) {
                if (tempmyFiles[x] != null && tempmyFiles[x].isFile() && (
                        tempmyFiles[x].getName().contains(".jpg")
                                || tempmyFiles[x].getName().contains(".JPG")
                                || tempmyFiles[x].getName().contains(".jpeg")
                                || tempmyFiles[x].getName().contains(".JPEG")
                                || tempmyFiles[x].getName().contains(".gif")
                                || tempmyFiles[x].getName().contains(".GIF")
                                || tempmyFiles[x].getName().contains(".png")
                                || tempmyFiles[x].getName().contains(".PNG"))) {
                    tempBackgroundImageFiles.add(tempmyFiles[x].getName());
                }
            }
            Collections.sort(tempBackgroundImageFiles, String.CASE_INSENSITIVE_ORDER);

            // Convert arraylist to string array
            BackgroundImageFiles = new String[tempBackgroundImageFiles.size()];
            BackgroundImageFiles = tempBackgroundImageFiles.toArray(BackgroundImageFiles);

        }

    public void listVideos() {

        File[] tempmyFiles = FullscreenActivity.dirbackgrounds.listFiles();
        // Go through this list and check if the item is a directory or a file.
        // Add these to the correct array
        int tempnumfiles;
        if (tempmyFiles != null && tempmyFiles.length>0) {
            tempnumfiles = tempmyFiles.length;
        } else {
            tempnumfiles = 0;
        }

        tempBackgroundVideoFiles = new ArrayList<>();

        for (int x=0; x<tempnumfiles; x++) {
            if (tempmyFiles[x] != null && tempmyFiles[x].isFile() && (
                    tempmyFiles[x].getName().contains(".mp4")
                            || tempmyFiles[x].getName().contains(".MP4")
                            || tempmyFiles[x].getName().contains(".mpg")
                            || tempmyFiles[x].getName().contains(".MPG")
                            || tempmyFiles[x].getName().contains(".mov")
                            || tempmyFiles[x].getName().contains(".MOV")
                            || tempmyFiles[x].getName().contains(".m4v")
                            || tempmyFiles[x].getName().contains(".M4V"))) {
                tempBackgroundVideoFiles.add(tempmyFiles[x].getName());
            }
        }
        Collections.sort(tempBackgroundVideoFiles, String.CASE_INSENSITIVE_ORDER);

        // Convert arraylist to string array
        BackgroundImageFiles = new String[tempBackgroundVideoFiles.size()];
        BackgroundImageFiles = tempBackgroundVideoFiles.toArray(BackgroundImageFiles);

    }

 }