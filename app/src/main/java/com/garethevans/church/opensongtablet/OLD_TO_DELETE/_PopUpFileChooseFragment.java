/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._PopUpSizeAndAlpha;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class PopUpFileChooseFragment extends DialogFragment {

    private static String[] foundFiles;
    private StorageAccess storageAccess;
    private _Preferences preferences;

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

    private static final String[] imagefiletypes = {".jpg",".jpeg",".JPG","JPEG",".png",".PNG",".gif",".GIF"};
    private static final String[] videofiletypes = {".mp4",".MP4",".mpg","MPG",".mpeg",".MPEG",".mov",".MOV",".m4v","M4V"};
    private static String[] filechecks;
    private String myTitle = "";
    private ArrayList<String> filesfound;

    private static String myswitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_file_chooser, container, false);
        ListView fileListView = V.findViewById(R.id.fileListView);
        TextView location = V.findViewById(R.id.location);

        storageAccess = new StorageAccess();
        preferences = new _Preferences();

        // Decide on the title of the file chooser
        if (PresenterMode.whatBackgroundLoaded!=null) {
            myswitch = PresenterMode.whatBackgroundLoaded;
        } else {
            myswitch = StaticVariables.whattodo;
        }
        switch (myswitch) {
            case "logo":
                myTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.logo);
                filechecks = imagefiletypes;
                location.setText("OpenSong/Backgrounds/");
                listvidsandimages();
                break;

            case "image1":
                myTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.choose_image1);
                filechecks = imagefiletypes;
                location.setText("OpenSong/Backgrounds/");
                listvidsandimages();
                break;

            case "image2":
                myTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.choose_image2);
                filechecks = imagefiletypes;
                location.setText("OpenSong/Backgrounds/");
                listvidsandimages();
                break;

            case "video1":
                myTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.choose_video1);
                filechecks = videofiletypes;
                location.setText("\"OpenSong/Backgrounds/\"");
                listvidsandimages();
                break;

            case "video2":
                myTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.choose_video2);
                filechecks = videofiletypes;
                location.setText("OpenSong/Backgrounds/");
                listvidsandimages();
                break;

            case "customnote":
                myTitle = getResources().getString(R.string.load) + " - " + getResources().getString(R.string.note);
                filechecks = null;
                location.setText("OpenSong/Notes/");
                listnotes();
                break;

            case "customslide":
                myTitle = getResources().getString(R.string.load) + " - " + getResources().getString(R.string.slide);
                filechecks = null;
                location.setText("OpenSong/Slides/");
                listslides();
                break;

            case "customimage":
                myTitle = getResources().getString(R.string.load) + " - " + getResources().getString(R.string.image_slide);
                filechecks = null;
                location.setText("OpenSong/Images/");
                listimageslides();
                break;

            case "customscripture":
                myTitle = getResources().getString(R.string.load) + " - " + getResources().getString(R.string.scripture);
                filechecks = null;
                location.setVisibility(View.GONE);
                listscriptures();
                break;
        }

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(myTitle);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                if (StaticVariables.whattodo.equals("customnote") ||
                        StaticVariables.whattodo.equals("customslide") ||
                        StaticVariables.whattodo.equals("customimage") ||
                        StaticVariables.whattodo.equals("customscripture")) {
                    dismiss();
                    DialogFragment newFragment = PopUpCustomSlideFragment.newInstance();
                    newFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "dialog");
                } else {
                    dismiss();
                }
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        // Populate the file list view
        fileListView.setAdapter(new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_single_choice, foundFiles));

        // Listen for clicks inside
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the appropriate file
                switch (myswitch) {
                    case "logo":
                        preferences.setMyPreferenceString(getActivity(),"customLogo",foundFiles[position]);
                        break;

                    case "image1":
                        preferences.setMyPreferenceString(getActivity(),"backgroundImage1",foundFiles[position]);
                        break;

                    case "image2":
                        preferences.setMyPreferenceString(getActivity(),"backgroundImage2",foundFiles[position]);
                        break;

                    case "video1":
                        preferences.setMyPreferenceString(getActivity(),"backgroundVideo1",foundFiles[position]);
                        break;

                    case "video2":
                        preferences.setMyPreferenceString(getActivity(),"backgroundVideo2",foundFiles[position]);
                        break;

                    case "customnote":
                        FullscreenActivity.customreusabletoload = Objects.requireNonNull(getActivity()).getResources().getString(R.string.note)  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customslide":
                        FullscreenActivity.customreusabletoload = Objects.requireNonNull(getActivity()).getResources().getString(R.string.slide)  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customimage":
                        FullscreenActivity.customreusabletoload = Objects.requireNonNull(getActivity()).getResources().getString(R.string.image)  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customscripture":
                        FullscreenActivity.customreusabletoload = Objects.requireNonNull(getActivity()).getResources().getString(R.string.scripture)  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                }
                dismiss();
            }
        });

        _PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void listimageslides() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Images", "");
        processfilelist();
    }

    private void listslides() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Slides", "");
        processfilelist();
    }

    private void listscriptures() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Scripture", "");
        processfilelist();
    }

    private void listnotes() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Notes", "");
        processfilelist();
    }

    private void listvidsandimages() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Backgrounds", "");
        processfilelist();
    }

    private void processfilelist() {

        foundFiles = new String[filesfound.size()];
        foundFiles = filesfound.toArray(foundFiles);
        ArrayList<String> tempFoundFiles = new ArrayList<>();

        // Go through each file
        for (String tempmyFile : foundFiles) {

            // If we need to check the filetype and it is ok, add it to the array
            if (filechecks != null && filechecks.length > 0) {
                for (String filecheck : filechecks) {
                    if (tempmyFile!=null && tempmyFile.contains(filecheck)) {
                        tempFoundFiles.add(tempmyFile);
                    }
                }

                // Otherwise, no check needed, add to the array (if it isn't a directory)
            } else {
                if (tempmyFile!=null) {
                    tempFoundFiles.add(tempmyFile);
                }
            }
        }

        // Sort the array list alphabetically by locale rules
        // Add locale sort
        Collator coll = Collator.getInstance(StaticVariables.locale);
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

}*/
