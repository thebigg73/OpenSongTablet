package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class PopUpFileChooseFragment extends DialogFragment {

    static Collator coll;
    static ArrayList<String> tempFoundFiles;
    static String[] foundFiles;
    File[] tempmyFiles;

    static PopUpFileChooseFragment newInstance() {
        PopUpFileChooseFragment frag;
        frag = new PopUpFileChooseFragment();
        return frag;
    }

    public interface MyInterface {
        void loadCustomReusable();
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    ListView fileListView;
    static String[] imagefiletypes = {".jpg",".jpeg",".JPG","JPEG",".png",".PNG",".gif",".GIF"};
    static String[] videofiletypes = {".mp4",".MP4",".mpg","MPG",".mpeg",".MPEG",".mov",".MOV",".m4v","M4V"};
    static String[] filechecks;
    String myTitle = "";

    static String myswitch;

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(myTitle);
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (FullscreenActivity.whattodo.equals("customnote") ||
                            FullscreenActivity.whattodo.equals("customslide") ||
                            FullscreenActivity.whattodo.equals("customimage") ||
                            FullscreenActivity.whattodo.equals("customscripture")) {
                        dismiss();
                        DialogFragment newFragment = PopUpCustomSlideFragment.newInstance();
                        newFragment.show(getFragmentManager(), "dialog");
                    } else {
                        dismiss();
                    }
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setVisibility(View.GONE);
        } else {
            getDialog().setTitle(myTitle);
        }
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

        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_file_chooser, container, false);

        fileListView = (ListView) V.findViewById(R.id.fileListView);

        // Decide on the title of the file chooser


        if (PresenterMode.whatBackgroundLoaded!=null) {
            myswitch = PresenterMode.whatBackgroundLoaded;
        } else {
            myswitch = FullscreenActivity.whattodo;
        }
        switch (myswitch) {
            case "image1":
                myTitle = getActivity().getResources().getString(R.string.choose_image1);
                filechecks = imagefiletypes;
                listvidsandimages();
                break;

            case "image2":
                myTitle = getActivity().getResources().getString(R.string.choose_image2);
                filechecks = imagefiletypes;
                listvidsandimages();
                break;

            case "video1":
                myTitle = getActivity().getResources().getString(R.string.choose_video1);
                filechecks = videofiletypes;
                listvidsandimages();
                break;

            case "video2":
                myTitle = getActivity().getResources().getString(R.string.choose_video2);
                filechecks = videofiletypes;
                listvidsandimages();
                break;

            case "customnote":
                myTitle = getResources().getString(R.string.options_set_load) + " - " + getResources().getString(R.string.note);
                filechecks = null;
                listnotes();
                break;

            case "customslide":
                myTitle = getResources().getString(R.string.options_set_load) + " - " + getResources().getString(R.string.slide);
                filechecks = null;
                listslides();
                break;

            case "customimage":
                myTitle = getResources().getString(R.string.options_set_load) + " - " + getResources().getString(R.string.image_slide);
                filechecks = null;
                listimageslides();
                break;

            case "customscripture":
                myTitle = getResources().getString(R.string.options_set_load) + " - " + getResources().getString(R.string.scripture);
                filechecks = null;
                listscriptures();
                break;
        }

        // Populate the file list view
        fileListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, foundFiles));

        // Listen for clicks inside
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the appropriate file
                switch (myswitch) {
                    case "image1":
                        FullscreenActivity.backgroundImage1 = foundFiles[position];
                        reOpenBackgrounds();
                        break;

                    case "image2":
                        FullscreenActivity.backgroundImage2 = foundFiles[position];
                        reOpenBackgrounds();
                        break;

                    case "video1":
                        FullscreenActivity.backgroundVideo1 = foundFiles[position];
                        reOpenBackgrounds();
                        break;

                    case "video2":
                        FullscreenActivity.backgroundVideo2 = foundFiles[position];
                        reOpenBackgrounds();
                        break;

                    case "customnote":
                        FullscreenActivity.customreusabletoload = FullscreenActivity.text_note  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customslide":
                        FullscreenActivity.customreusabletoload = FullscreenActivity.text_slide  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customimage":
                        FullscreenActivity.customreusabletoload = FullscreenActivity.image  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customscripture":
                        FullscreenActivity.customreusabletoload = FullscreenActivity.text_scripture  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                }
                Preferences.savePreferences();
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

    public void listimageslides() {
        File location = new File(FullscreenActivity.homedir + "/Images");
        tempmyFiles = location.listFiles();
        processfilelist();
    }

    public void listslides() {
        File location = new File(FullscreenActivity.homedir + "/Slides");
        tempmyFiles = location.listFiles();
        processfilelist();
    }

    public void listscriptures() {
        File location = new File(FullscreenActivity.homedir + "/Scripture");
        tempmyFiles = location.listFiles();
        processfilelist();
    }

    public void listnotes() {
        File location = new File(FullscreenActivity.homedir + "/Notes");
        tempmyFiles = location.listFiles();
        processfilelist();
    }

    public void listvidsandimages() {
        tempmyFiles = FullscreenActivity.dirbackgrounds.listFiles();
        processfilelist();
    }

    public void processfilelist() {

        tempFoundFiles = new ArrayList<>();

        // Go through each file
        for (File tempmyFile : tempmyFiles) {

            // If we need to check the filetype and it is ok, add it to the array
            if (filechecks != null && filechecks.length > 0) {
                for (String filecheck : filechecks) {
                    if (tempmyFile!=null && tempmyFile.getName().contains(filecheck) && !tempmyFile.isDirectory()) {
                        tempFoundFiles.add(tempmyFile.getName());
                    }
                }

                // Otherwise, no check needed, add to the array (if it isn't a directory)
            } else {
                if (tempmyFile!=null && !tempmyFile.isDirectory()) {
                    tempFoundFiles.add(tempmyFile.getName());
                }
            }
        }

        // Sort the array list alphabetically by locale rules
        // Add locale sort
        coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(tempFoundFiles, coll);

        // Convert arraylist to string array
        foundFiles = new String[tempFoundFiles.size()];
        foundFiles = tempFoundFiles.toArray(foundFiles);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}