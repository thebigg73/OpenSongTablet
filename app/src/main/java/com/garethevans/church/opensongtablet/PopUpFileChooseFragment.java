package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class PopUpFileChooseFragment extends DialogFragment {

    static Collator coll;
    static ArrayList<String> tempFoundFiles;
    static String[] foundFiles;
    StorageAccess storageAccess;
    Preferences preferences;

    static PopUpFileChooseFragment newInstance() {
        PopUpFileChooseFragment frag;
        frag = new PopUpFileChooseFragment();
        return frag;
    }

    public interface MyInterface {
        void loadCustomReusable();
        void openFragment();
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

    TextView location;
    ListView fileListView;
    static String[] imagefiletypes = {".jpg",".jpeg",".JPG","JPEG",".png",".PNG",".gif",".GIF"};
    static String[] videofiletypes = {".mp4",".MP4",".mpg","MPG",".mpeg",".MPEG",".mov",".MOV",".m4v","M4V"};
    static String[] filechecks;
    String myTitle = "";
    ArrayList<String> filesfound;

    static String myswitch;

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_file_chooser, container, false);
        fileListView = V.findViewById(R.id.fileListView);
        location = V.findViewById(R.id.location);

        storageAccess = new StorageAccess();
        preferences = new Preferences();

        // Decide on the title of the file chooser
        if (PresenterMode.whatBackgroundLoaded!=null) {
            myswitch = PresenterMode.whatBackgroundLoaded;
        } else {
            myswitch = FullscreenActivity.whattodo;
        }
        switch (myswitch) {
            case "logo":
                myTitle = getActivity().getResources().getString(R.string.logo);
                filechecks = imagefiletypes;
                location.setText(FullscreenActivity.dirbackgrounds.toString());
                listvidsandimages();
                break;

            case "image1":
                myTitle = getActivity().getResources().getString(R.string.choose_image1);
                filechecks = imagefiletypes;
                location.setText(FullscreenActivity.dirbackgrounds.toString());
                listvidsandimages();
                break;

            case "image2":
                myTitle = getActivity().getResources().getString(R.string.choose_image2);
                filechecks = imagefiletypes;
                location.setText(FullscreenActivity.dirbackgrounds.toString());
                listvidsandimages();
                break;

            case "video1":
                myTitle = getActivity().getResources().getString(R.string.choose_video1);
                filechecks = videofiletypes;
                location.setText(FullscreenActivity.dirbackgrounds.toString());
                listvidsandimages();
                break;

            case "video2":
                myTitle = getActivity().getResources().getString(R.string.choose_video2);
                filechecks = videofiletypes;
                location.setText(FullscreenActivity.dirbackgrounds.toString());
                listvidsandimages();
                break;

            case "customnote":
                myTitle = getResources().getString(R.string.options_set_load) + " - " + getResources().getString(R.string.note);
                filechecks = null;
                location.setText("OpenSong/Notes/");
                listnotes();
                break;

            case "customslide":
                myTitle = getResources().getString(R.string.options_set_load) + " - " + getResources().getString(R.string.slide);
                filechecks = null;
                location.setText("OpenSong/Slides/");
                listslides();
                break;

            case "customimage":
                myTitle = getResources().getString(R.string.options_set_load) + " - " + getResources().getString(R.string.image_slide);
                filechecks = null;
                location.setText("OpenSong/Images/");
                listimageslides();
                break;

            case "customscripture":
                myTitle = getResources().getString(R.string.options_set_load) + " - " + getResources().getString(R.string.scripture);
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
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
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
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);


        // Populate the file list view
        fileListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, foundFiles));

        // Listen for clicks inside
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the appropriate file
                switch (myswitch) {
                    case "logo":
                        FullscreenActivity.customLogo = foundFiles[position];
                        //reOpenBackgrounds();
                        break;

                    case "image1":
                        FullscreenActivity.backgroundImage1 = foundFiles[position];
                        //reOpenBackgrounds();
                        break;

                    case "image2":
                        FullscreenActivity.backgroundImage2 = foundFiles[position];
                        //reOpenBackgrounds();
                        break;

                    case "video1":
                        FullscreenActivity.backgroundVideo1 = foundFiles[position];
                        //reOpenBackgrounds();
                        break;

                    case "video2":
                        FullscreenActivity.backgroundVideo2 = foundFiles[position];
                        //reOpenBackgrounds();
                        break;

                    case "customnote":
                        FullscreenActivity.customreusabletoload = getActivity().getResources().getString(R.string.note)  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customslide":
                        FullscreenActivity.customreusabletoload = getActivity().getResources().getString(R.string.slide)  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customimage":
                        FullscreenActivity.customreusabletoload = getActivity().getResources().getString(R.string.image)  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                    case "customscripture":
                        FullscreenActivity.customreusabletoload = getActivity().getResources().getString(R.string.scripture)  + "/" + foundFiles[position];
                        mListener.loadCustomReusable();
                        break;

                }
                Preferences.savePreferences();
                dismiss();
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void listimageslides() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Images", "");
        processfilelist();
    }

    public void listslides() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Slides", "");
        processfilelist();
    }

    public void listscriptures() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Scripture", "");
        processfilelist();
    }

    public void listnotes() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Notes", "");
        processfilelist();
    }

    public void listvidsandimages() {
        filesfound = storageAccess.listFilesInFolder(getActivity(), preferences, "Backgrounds", "");
        processfilelist();
    }

    public void processfilelist() {

        foundFiles = new String[filesfound.size()];
        foundFiles = filesfound.toArray(foundFiles);
        tempFoundFiles = new ArrayList<>();

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