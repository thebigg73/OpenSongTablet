package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class PopUpProfileFragment extends DialogFragment {

    static PopUpProfileFragment newInstance() {
        PopUpProfileFragment frag;
        frag = new PopUpProfileFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
        void setupPageButtons(String s);
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

    File location;
    File[] tempmyFiles;
    String[] foundFiles;
    ArrayList<String> tempFoundFiles;
    String[] filechecks;
    Collator coll;
    ScrollView profile_overview;
    RelativeLayout profile_load;
    RelativeLayout profile_save;
    TextView profileName_TextView;
    EditText profileName_EditText;
    ListView profileFilesLoad_ListView;
    ListView profileFilesSave_ListView;
    Button loadProfile_Button;
    Button saveProfile_Button;
    Button okSave_Button;
    Button cancelSave_Button;
    Button cancelLoad_Button;
    String name;
    String what = "overview";

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.profile));
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setVisibility(View.GONE);
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.profile));
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
        View V = inflater.inflate(R.layout.popup_profile, container, false);

        // Initialise the views
        profile_overview = (ScrollView) V.findViewById(R.id.profile_overview);
        profile_load = (RelativeLayout) V.findViewById(R.id.profile_load);
        profile_save = (RelativeLayout) V.findViewById(R.id.profile_save);
        profileName_TextView = (TextView) V.findViewById(R.id.profileName_TextView);
        profileName_EditText = (EditText) V.findViewById(R.id.profileName_EditText);
        profileFilesLoad_ListView = (ListView) V.findViewById(R.id.profileFilesLoad_ListView);
        profileFilesSave_ListView = (ListView) V.findViewById(R.id.profileFilesSave_ListView);
        loadProfile_Button = (Button) V.findViewById(R.id.loadProfile_Button);
        saveProfile_Button = (Button) V.findViewById(R.id.saveProfile_Button);
        cancelSave_Button = (Button) V.findViewById(R.id.cancelSave_Button);
        okSave_Button = (Button) V.findViewById(R.id.okSave_Button);
        cancelLoad_Button = (Button) V.findViewById(R.id.cancelLoad_Button);

        // Only show the first view with profile name and options to load or save or reset
        showOverView();

        // Set the profile name if it exists
        if (FullscreenActivity.profile.equals("")) {
            name = getActivity().getString(R.string.options_song_new);
        } else {
            name = FullscreenActivity.profile;
        }
        profileName_TextView.setText(name);
        profileName_EditText.setText(name);

        // Set up listeners for the overview page
        loadProfile_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoad();
            }
        });
        saveProfile_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSave();
            }
        });

        // Set up listeners for the save page
        okSave_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contents = prepareProfile();
                name = profileName_EditText.getText().toString();
                if (!name.equals("")) {
                    File myfile = new File(FullscreenActivity.dirprofiles + "/" + name);
                    try {
                        FileOutputStream overWrite = new FileOutputStream(myfile, false);
                        overWrite.write(contents.getBytes());
                        overWrite.flush();
                        overWrite.close();
                        FullscreenActivity.myToastMessage = getString(R.string.ok);
                        FullscreenActivity.profile = name;
                        profileName_TextView.setText(name);
                        profileName_EditText.setText(name);


                    } catch (Exception e) {
                        e.printStackTrace();
                        FullscreenActivity.myToastMessage = getString(R.string.profile) + " " +
                                getString(R.string.hasnotbeenexported);
                    }
                } else {
                    FullscreenActivity.myToastMessage = getString(R.string.profile) + " " +
                            getString(R.string.hasnotbeenexported);
                }
                ShowToast.showToast(getActivity());
                showOverView();
            }
        });
        cancelSave_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                what = "overview";
                showOverView();
            }
        });

        // Set up listeners for the load page
        cancelLoad_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                what = "overview";
                showOverView();
            }
        });
        return V;
    }

    public void showOverView() {
        profile_overview.setVisibility(View.VISIBLE);
        profile_load.setVisibility(View.GONE);
        profile_save.setVisibility(View.GONE);
        what = "overview";
    }

    public void showLoad() {
        profile_overview.setVisibility(View.GONE);
        profile_load.setVisibility(View.VISIBLE);
        profile_save.setVisibility(View.GONE);
        what = "load";
        setupProfileList();
    }

    public void showSave() {
        profile_overview.setVisibility(View.GONE);
        profile_load.setVisibility(View.GONE);
        profile_save.setVisibility(View.VISIBLE);
        what = "save";
        setupProfileList();
    }

    public void setupProfileList() {
        location = new File(FullscreenActivity.homedir + "/Profiles");
        tempmyFiles = location.listFiles();
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

        Log.d("d","what="+what);

        // Add the saved profiles to the listview
        // Populate the file list view
        if (what.equals("save")) {
            profileFilesSave_ListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, foundFiles));
            profileFilesSave_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        profileName_EditText.setText(foundFiles[position]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } else if (what.equals("load")) {
            profileFilesLoad_ListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, foundFiles));
            profileFilesLoad_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        FullscreenActivity.profile = foundFiles[position];
                        grabvalues(FullscreenActivity.dirprofiles + "/" + foundFiles[position]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void grabvalues(String file) throws Exception {
        // Extract all of the key bits of the profile
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        XmlPullParser xpp;
        xpp = factory.newPullParser();

        InputStream inputStream = new FileInputStream(file);
        xpp.setInput(inputStream,null);

        int eventType;
        eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {

                if (xpp.getName().equals("pagebutton_position")) {
                    FullscreenActivity.pagebutton_position = getTextValue(xpp.nextText(),"right");

                } else if (xpp.getName().equals("pagebutton_scale")) {
                    FullscreenActivity.pagebutton_scale = getTextValue(xpp.nextText(),"M");

                } else if (xpp.getName().equals("toggleAutoSticky")) {
                    FullscreenActivity.toggleAutoSticky = getTextValue(xpp.nextText(),"N");

                } else if (xpp.getName().equals("toggleScrollArrows")) {
                    FullscreenActivity.toggleScrollArrows = getTextValue(xpp.nextText(),"S");

                } else if (xpp.getName().equals("capoDisplay")) {
                    FullscreenActivity.capoDisplay = getTextValue(xpp.nextText(),"both");

                } else if (xpp.getName().equals("mylyricsfontnum")) {
                    FullscreenActivity.mylyricsfontnum = getIntegerValue(xpp.nextText(),8);

                } else if (xpp.getName().equals("mychordsfontnum")) {
                    FullscreenActivity.mychordsfontnum = getIntegerValue(xpp.nextText(),8);

                } else if (xpp.getName().equals("linespacing")) {
                    FullscreenActivity.linespacing = getIntegerValue(xpp.nextText(),0);

                } else if (xpp.getName().equals("togglePageButtons")) {
                    FullscreenActivity.togglePageButtons = getTextValue(xpp.nextText(),"Y");

                } else if (xpp.getName().equals("swipeDrawer")) {
                    FullscreenActivity.swipeDrawer = getTextValue(xpp.nextText(),"Y");

                } else if (xpp.getName().equals("autostartautoscroll")) {
                    FullscreenActivity.autostartautoscroll = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("visualmetronome")) {
                    FullscreenActivity.visualmetronome = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("mFontSize")) {
                    FullscreenActivity.mFontSize = getFloatValue(xpp.nextText(),42.0f);

                } else if (xpp.getName().equals("commentfontscalesize")) {
                    FullscreenActivity.commentfontscalesize = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("headingfontscalesize")) {
                    FullscreenActivity.headingfontscalesize = getFloatValue(xpp.nextText(),0.6f);

                } else if (xpp.getName().equals("mMaxFontSize")) {
                    FullscreenActivity.mMaxFontSize = getIntegerValue(xpp.nextText(),50);

                } else if (xpp.getName().equals("mMinFontSize")) {
                    FullscreenActivity.mMinFontSize = getIntegerValue(xpp.nextText(),8);

                } else if (xpp.getName().equals("override_fullscale")) {
                    FullscreenActivity.override_fullscale = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("override_widthscale")) {
                    FullscreenActivity.override_widthscale = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("usePresentationOrder")) {
                    FullscreenActivity.usePresentationOrder = getBooleanValue(xpp.nextText(),false);


                } else if (xpp.getName().equals("toggleYScale")) {
                    FullscreenActivity.toggleYScale = getTextValue(xpp.nextText(),"Y");

                } else if (xpp.getName().equals("swipeSet")) {
                    FullscreenActivity.swipeSet = getTextValue(xpp.nextText(),"Y");

                } else if (xpp.getName().equals("hideActionBar")) {
                    FullscreenActivity.hideActionBar = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("swipeForMenus")) {
                    FullscreenActivity.swipeForMenus = getBooleanValue(xpp.nextText(), true);

                } else if (xpp.getName().equals("menuSize")) {
                    FullscreenActivity.menuSize = getFloatValue(xpp.nextText(), 0.5f);

                } else if (xpp.getName().equals("swipeForSongs")) {
                    FullscreenActivity.swipeForSongs = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("transposeStyle")) {
                    FullscreenActivity.transposeStyle = getTextValue(xpp.nextText(),"sharps");

                } else if (xpp.getName().equals("showChords")) {
                    FullscreenActivity.showChords = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("mDisplayTheme")) {
                    FullscreenActivity.mDisplayTheme = getTextValue(xpp.nextText(),"Theme.Holo");


                } else if (xpp.getName().equals("dark_lyricsTextColor")) {
                    FullscreenActivity.dark_lyricsTextColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("dark_lyricsBackgroundColor")) {
                    FullscreenActivity.dark_lyricsBackgroundColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("dark_lyricsVerseColor")) {
                    FullscreenActivity.dark_lyricsVerseColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("dark_lyricsChorusColor")) {
                    FullscreenActivity.dark_lyricsChorusColor = getIntegerValue(xpp.nextText(),0xff000033);
                } else if (xpp.getName().equals("dark_lyricsBridgeColor")) {
                    FullscreenActivity.dark_lyricsBridgeColor = getIntegerValue(xpp.nextText(),0xff330000);
                } else if (xpp.getName().equals("dark_lyricsCommentColor")) {
                    FullscreenActivity.dark_lyricsCommentColor = getIntegerValue(xpp.nextText(),0xff003300);
                } else if (xpp.getName().equals("dark_lyricsPreChorusColor")) {
                    FullscreenActivity.dark_lyricsPreChorusColor = getIntegerValue(xpp.nextText(),0xff112211);
                } else if (xpp.getName().equals("dark_lyricsTagColor")) {
                    FullscreenActivity.dark_lyricsTagColor = getIntegerValue(xpp.nextText(),0xff330033);
                } else if (xpp.getName().equals("dark_lyricsChordsColor")) {
                    FullscreenActivity.dark_lyricsChordsColor = getIntegerValue(xpp.nextText(),0xffffff00);
                } else if (xpp.getName().equals("dark_lyricsCustomColor")) {
                    FullscreenActivity.dark_lyricsCustomColor = getIntegerValue(xpp.nextText(),0xff222200);
                } else if (xpp.getName().equals("dark_lyricsCapoColor")) {
                    FullscreenActivity.dark_lyricsCapoColor = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("dark_metronome")) {
                    FullscreenActivity.dark_metronome = getIntegerValue(xpp.nextText(),0xffaa1212);
                } else if (xpp.getName().equals("dark_pagebuttons")) {
                    FullscreenActivity.dark_pagebuttons = getIntegerValue(xpp.nextText(),0xff452277);

                } else if (xpp.getName().equals("light_lyricsTextColor")) {
                    FullscreenActivity.light_lyricsTextColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("light_lyricsBackgroundColor")) {
                    FullscreenActivity.light_lyricsBackgroundColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("light_lyricsVerseColor")) {
                    FullscreenActivity.light_lyricsVerseColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("light_lyricsChorusColor")) {
                    FullscreenActivity.light_lyricsChorusColor = getIntegerValue(xpp.nextText(),0xffffddff);
                } else if (xpp.getName().equals("light_lyricsBridgeColor")) {
                    FullscreenActivity.light_lyricsBridgeColor = getIntegerValue(xpp.nextText(),0xffddffff);
                } else if (xpp.getName().equals("light_lyricsCommentColor")) {
                    FullscreenActivity.light_lyricsCommentColor = getIntegerValue(xpp.nextText(),0xffddddff);
                } else if (xpp.getName().equals("light_lyricsPreChorusColor")) {
                    FullscreenActivity.light_lyricsPreChorusColor = getIntegerValue(xpp.nextText(),0xffeeccee);
                } else if (xpp.getName().equals("light_lyricsTagColor")) {
                    FullscreenActivity.light_lyricsTagColor = getIntegerValue(xpp.nextText(),0xffddffdd);
                } else if (xpp.getName().equals("light_lyricsChordsColor")) {
                    FullscreenActivity.light_lyricsChordsColor = getIntegerValue(xpp.nextText(),0xff0000dd);
                } else if (xpp.getName().equals("light_lyricsCustomColor")) {
                    FullscreenActivity.light_lyricsCustomColor = getIntegerValue(xpp.nextText(),0xffccddff);
                } else if (xpp.getName().equals("light_lyricsCapoColor")) {
                    FullscreenActivity.light_lyricsCapoColor = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("light_metronome")) {
                    FullscreenActivity.light_metronome = getIntegerValue(xpp.nextText(),0xffaa1212);
                } else if (xpp.getName().equals("light_pagebuttons")) {
                    FullscreenActivity.light_pagebuttons = getIntegerValue(xpp.nextText(),0xff452277);

                } else if (xpp.getName().equals("custom1_lyricsTextColor")) {
                    FullscreenActivity.custom1_lyricsTextColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom1_lyricsBackgroundColor")) {
                    FullscreenActivity.custom1_lyricsBackgroundColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("custom1_lyricsVerseColor")) {
                    FullscreenActivity.custom1_lyricsVerseColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("custom1_lyricsChorusColor")) {
                    FullscreenActivity.custom1_lyricsChorusColor = getIntegerValue(xpp.nextText(),0xff000033);
                } else if (xpp.getName().equals("custom1_lyricsBridgeColor")) {
                    FullscreenActivity.custom1_lyricsBridgeColor = getIntegerValue(xpp.nextText(),0xff330000);
                } else if (xpp.getName().equals("custom1_lyricsCommentColor")) {
                    FullscreenActivity.custom1_lyricsCommentColor = getIntegerValue(xpp.nextText(),0xff003300);
                } else if (xpp.getName().equals("custom1_lyricsPreChorusColor")) {
                    FullscreenActivity.custom1_lyricsPreChorusColor = getIntegerValue(xpp.nextText(),0xff112211);
                } else if (xpp.getName().equals("custom1_lyricsTagColor")) {
                    FullscreenActivity.custom1_lyricsTagColor = getIntegerValue(xpp.nextText(),0xff330033);
                } else if (xpp.getName().equals("custom1_lyricsChordsColor")) {
                    FullscreenActivity.custom1_lyricsChordsColor = getIntegerValue(xpp.nextText(),0xffffff00);
                } else if (xpp.getName().equals("custom1_lyricsCustomColor")) {
                    FullscreenActivity.custom1_lyricsCustomColor = getIntegerValue(xpp.nextText(),0xff222200);
                } else if (xpp.getName().equals("custom1_lyricsCapoColor")) {
                    FullscreenActivity.custom1_lyricsCapoColor = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("custom1_metronome")) {
                    FullscreenActivity.custom1_metronome = getIntegerValue(xpp.nextText(),0xffaa1212);
                } else if (xpp.getName().equals("custom1_pagebuttons")) {
                    FullscreenActivity.custom1_pagebuttons = getIntegerValue(xpp.nextText(),0xff452277);

                } else if (xpp.getName().equals("custom2_lyricsTextColor")) {
                    FullscreenActivity.custom2_lyricsTextColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("custom2_lyricsBackgroundColor")) {
                    FullscreenActivity.custom2_lyricsBackgroundColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom2_lyricsVerseColor")) {
                    FullscreenActivity.custom2_lyricsVerseColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom2_lyricsChorusColor")) {
                    FullscreenActivity.custom2_lyricsChorusColor = getIntegerValue(xpp.nextText(),0xffffddff);
                } else if (xpp.getName().equals("custom2_lyricsBridgeColor")) {
                    FullscreenActivity.custom2_lyricsBridgeColor = getIntegerValue(xpp.nextText(),0xffddffff);
                } else if (xpp.getName().equals("custom2_lyricsCommentColor")) {
                    FullscreenActivity.custom2_lyricsCommentColor = getIntegerValue(xpp.nextText(),0xffddddff);
                } else if (xpp.getName().equals("custom2_lyricsPreChorusColor")) {
                    FullscreenActivity.custom2_lyricsPreChorusColor = getIntegerValue(xpp.nextText(),0xffeeccee);
                } else if (xpp.getName().equals("custom2_lyricsTagColor")) {
                    FullscreenActivity.custom2_lyricsTagColor = getIntegerValue(xpp.nextText(),0xffddffdd);
                } else if (xpp.getName().equals("custom2_lyricsChordsColor")) {
                    FullscreenActivity.custom2_lyricsChordsColor = getIntegerValue(xpp.nextText(),0xff0000dd);
                } else if (xpp.getName().equals("custom2_lyricsCustomColor")) {
                    FullscreenActivity.custom2_lyricsCustomColor = getIntegerValue(xpp.nextText(),0xffccddff);
                } else if (xpp.getName().equals("custom2_lyricsCapoColor")) {
                    FullscreenActivity.custom2_lyricsCapoColor = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("custom2_metronome")) {
                    FullscreenActivity.custom2_metronome = getIntegerValue(xpp.nextText(),0xffaa1212);
                } else if (xpp.getName().equals("custom2_pagebuttons")) {
                    FullscreenActivity.custom2_pagebuttons = getIntegerValue(xpp.nextText(),0xff452277);

                } else if (xpp.getName().equals("chordInstrument")) {
                    FullscreenActivity.chordInstrument = getTextValue(xpp.nextText(),"g");

                } else if (xpp.getName().equals("popupScale_All")) {
                    FullscreenActivity.popupScale_All = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupScale_Set")) {
                    FullscreenActivity.popupScale_Set = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupDim_All")) {
                    FullscreenActivity.popupDim_All = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupDim_Set")) {
                    FullscreenActivity.popupDim_Set = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupAlpha_All")) {
                    FullscreenActivity.popupAlpha_All = getFloatValue(xpp.nextText(),0.7f);

                } else if (xpp.getName().equals("popupAlpha_Set")) {
                    FullscreenActivity.popupAlpha_Set = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupPosition_All")) {
                    FullscreenActivity.popupPosition_All = getTextValue(xpp.nextText(),"c");

                } else if (xpp.getName().equals("popupPosition_Set")) {
                    FullscreenActivity.popupPosition_Set = getTextValue(xpp.nextText(),"c");

                } else if (xpp.getName().equals("pageButtonAlpha")) {
                    FullscreenActivity.pageButtonAlpha = getFloatValue(xpp.nextText(),0.2f);

                } else if (xpp.getName().equals("grouppagebuttons")) {
                    FullscreenActivity.grouppagebuttons = getBooleanValue(xpp.nextText(),false);

                }

            }

            try {
                eventType = xpp.next();
            } catch (Exception e) {
                //Ooops!
                Log.d("d","error in file, or not xml");
            }
        }

        // Save the new preferences
        Preferences.savePreferences();

        // Reload the display
        dismiss();
        SetUpColours.colours();
        mListener.refreshAll();
        mListener.setupPageButtons("");
    }

    public int getIntegerValue(String s, int def) {
        int integer = def;
        if (s!=null && !s.equals("")) {
            try {
                integer = Integer.parseInt(s);
            } catch (Exception e) {
                integer = def;
            }
        }
        return integer;
    }

    public String getTextValue(String s, String def) {
        String text = def;
        if (s!=null && !s.equals("")) {
            text = s;
        }
        return text;
    }

    public boolean getBooleanValue(String s, boolean def) {
        boolean trueorfalse = def;
        if (s!=null && !s.equals("")) {
            trueorfalse = s.equals("true");
        }
        return trueorfalse;
    }

    public float getFloatValue(String s, float def) {
        float f = def;
        if (s!=null && !s.equals("")) {
            try {
                f = Float.parseFloat(s.replace("f",""));
            } catch (Exception e) {
                f = def;
            }
        }
        return f;
    }

    public String prepareProfile() {

        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        text += "<profile>\n";
        text += "  <pagebutton_position>" + FullscreenActivity.pagebutton_position + "</pagebutton_position>\n";
        text += "  <pagebutton_scale>" + FullscreenActivity.pagebutton_scale + "</pagebutton_scale>\n";
        text += "  <toggleAutoSticky>" + FullscreenActivity.toggleAutoSticky + "</toggleAutoSticky>\n";
        text += "  <toggleScrollArrows>" + FullscreenActivity.toggleScrollArrows + "</toggleScrollArrows>\n";
        text += "  <capoDisplay>" + FullscreenActivity.capoDisplay + "</capoDisplay>\n";
        text += "  <mylyricsfontnum>" + FullscreenActivity.mylyricsfontnum + "</mylyricsfontnum>\n";
        text += "  <mychordsfontnum>" + FullscreenActivity.mychordsfontnum + "</mychordsfontnum>\n";
        text += "  <linespacing>" + FullscreenActivity.linespacing + "</linespacing>\n";
        text += "  <togglePageButtons>" + FullscreenActivity.togglePageButtons + "</togglePageButtons>\n";
        text += "  <swipeDrawer>" + FullscreenActivity.swipeDrawer + "</swipeDrawer>\n";
        text += "  <autostartautoscroll>" + FullscreenActivity.autostartautoscroll + "</autostartautoscroll>\n";
        text += "  <visualmetronome>" + FullscreenActivity.visualmetronome + "</visualmetronome>\n";
        text += "  <mFontSize>" + FullscreenActivity.mFontSize + "</mFontSize>\n";
        text += "  <commentfontscalesize>" + FullscreenActivity.commentfontscalesize + "</commentfontscalesize>\n";
        text += "  <headingfontscalesize>" + FullscreenActivity.headingfontscalesize + "</headingfontscalesize>\n";
        text += "  <mMaxFontSize>" + FullscreenActivity.mMaxFontSize + "</mMaxFontSize>\n";
        text += "  <mMinFontSize>" + FullscreenActivity.mMinFontSize + "</mMinFontSize>\n";
        text += "  <override_fullscale>" + FullscreenActivity.override_fullscale + "</override_fullscale>\n";
        text += "  <override_widthscale>" + FullscreenActivity.override_widthscale + "</override_widthscale>\n";
        text += "  <usePresentationOrder>" + FullscreenActivity.usePresentationOrder + "</usePresentationOrder>\n";
        text += "  <toggleYScale>" + FullscreenActivity.toggleYScale + "</toggleYScale>\n";
        text += "  <swipeSet>" + FullscreenActivity.swipeSet + "</swipeSet>\n";
        text += "  <hideActionBar>" + FullscreenActivity.hideActionBar + "</hideActionBar>\n";
        text += "  <swipeForMenus>" + FullscreenActivity.swipeForSongs + "</swipeForMenus>\n";
        text += "  <menuSize>" + FullscreenActivity.menuSize + "</menuSize>\n";
        text += "  <swipeForSongs>" + FullscreenActivity.swipeForSongs + "</swipeForSongs>\n";
        text += "  <transposeStyle>" + FullscreenActivity.transposeStyle + "</transposeStyle>\n";
        text += "  <showChords>" + FullscreenActivity.showChords + "</showChords>\n";
        text += "  <mDisplayTheme>" + FullscreenActivity.mDisplayTheme + "</mDisplayTheme>\n";
        text += "  <dark_lyricsTextColor>" + FullscreenActivity.dark_lyricsTextColor + "</dark_lyricsTextColor>\n";
        text += "  <dark_lyricsBackgroundColor>" + FullscreenActivity.dark_lyricsBackgroundColor + "</dark_lyricsBackgroundColor>\n";
        text += "  <dark_lyricsVerseColor>" + FullscreenActivity.dark_lyricsVerseColor + "</dark_lyricsVerseColor>\n";
        text += "  <dark_lyricsChorusColor>" + FullscreenActivity.dark_lyricsChorusColor + "</dark_lyricsChorusColor>\n";
        text += "  <dark_lyricsBridgeColor>" + FullscreenActivity.dark_lyricsBridgeColor + "</dark_lyricsBridgeColor>\n";
        text += "  <dark_lyricsCommentColor>" + FullscreenActivity.dark_lyricsCommentColor + "</dark_lyricsCommentColor>\n";
        text += "  <dark_lyricsPreChorusColor>" + FullscreenActivity.dark_lyricsPreChorusColor + "</dark_lyricsPreChorusColor>\n";
        text += "  <dark_lyricsTagColor>" + FullscreenActivity.dark_lyricsTagColor + "</dark_lyricsTagColor>\n";
        text += "  <dark_lyricsChordsColor>" + FullscreenActivity.dark_lyricsChordsColor + "</dark_lyricsChordsColor>\n";
        text += "  <dark_lyricsCustomColor>" + FullscreenActivity.dark_lyricsCustomColor + "</dark_lyricsCustomColor>\n";
        text += "  <dark_lyricsCapoColor>" + FullscreenActivity.dark_lyricsCapoColor + "</dark_lyricsCapoColor>\n";
        text += "  <dark_metronome>" + FullscreenActivity.dark_metronome + "</dark_metronome>\n";
        text += "  <dark_pagebuttons>" + FullscreenActivity.dark_pagebuttons + "</dark_pagebuttons>\n";
        text += "  <light_lyricsTextColor>" + FullscreenActivity.light_lyricsTextColor + "</light_lyricsTextColor>\n";
        text += "  <light_lyricsBackgroundColor>" + FullscreenActivity.light_lyricsBackgroundColor + "</light_lyricsBackgroundColor>\n";
        text += "  <light_lyricsVerseColor>" + FullscreenActivity.light_lyricsVerseColor + "</light_lyricsVerseColor>\n";
        text += "  <light_lyricsChorusColor>" + FullscreenActivity.light_lyricsChorusColor + "</light_lyricsChorusColor>\n";
        text += "  <light_lyricsBridgeColor>" + FullscreenActivity.light_lyricsBridgeColor + "</light_lyricsBridgeColor>\n";
        text += "  <light_lyricsCommentColor>" + FullscreenActivity.light_lyricsCommentColor + "</light_lyricsCommentColor>\n";
        text += "  <light_lyricsPreChorusColor>" + FullscreenActivity.light_lyricsPreChorusColor + "</light_lyricsPreChorusColor>\n";
        text += "  <light_lyricsTagColor>" + FullscreenActivity.light_lyricsTagColor + "</light_lyricsTagColor>\n";
        text += "  <light_lyricsChordsColor>" + FullscreenActivity.light_lyricsChordsColor + "</light_lyricsChordsColor>\n";
        text += "  <light_lyricsCustomColor>" + FullscreenActivity.light_lyricsCustomColor + "</light_lyricsCustomColor>\n";
        text += "  <light_lyricsCapoColor>" + FullscreenActivity.light_lyricsCapoColor + "</light_lyricsCapoColor>\n";
        text += "  <light_metronome>" + FullscreenActivity.light_metronome + "</light_metronome>\n";
        text += "  <light_pagebuttons>" + FullscreenActivity.light_pagebuttons + "</light_pagebuttons>\n";
        text += "  <custom1_lyricsTextColor>" + FullscreenActivity.custom1_lyricsTextColor + "</custom1_lyricsTextColor>\n";
        text += "  <custom1_lyricsBackgroundColor>" + FullscreenActivity.custom1_lyricsBackgroundColor + "</custom1_lyricsBackgroundColor>\n";
        text += "  <custom1_lyricsVerseColor>" + FullscreenActivity.custom1_lyricsVerseColor + "</custom1_lyricsVerseColor>\n";
        text += "  <custom1_lyricsChorusColor>" + FullscreenActivity.custom1_lyricsChorusColor + "</custom1_lyricsChorusColor>\n";
        text += "  <custom1_lyricsBridgeColor>" + FullscreenActivity.custom1_lyricsBridgeColor + "</custom1_lyricsBridgeColor>\n";
        text += "  <custom1_lyricsCommentColor>" + FullscreenActivity.custom1_lyricsCommentColor + "</custom1_lyricsCommentColor>\n";
        text += "  <custom1_lyricsPreChorusColor>" + FullscreenActivity.custom1_lyricsPreChorusColor + "</custom1_lyricsPreChorusColor>\n";
        text += "  <custom1_lyricsTagColor>" + FullscreenActivity.custom1_lyricsTagColor + "</custom1_lyricsTagColor>\n";
        text += "  <custom1_lyricsChordsColor>" + FullscreenActivity.custom1_lyricsChordsColor + "</custom1_lyricsChordsColor>\n";
        text += "  <custom1_lyricsCustomColor>" + FullscreenActivity.custom1_lyricsCustomColor + "</custom1_lyricsCustomColor>\n";
        text += "  <custom1_lyricsCapoColor>" + FullscreenActivity.custom1_lyricsCapoColor + "</custom1_lyricsCapoColor>\n";
        text += "  <custom1_metronome>" + FullscreenActivity.custom1_metronome + "</custom1_metronome>\n";
        text += "  <custom1_pagebuttons>" + FullscreenActivity.custom1_pagebuttons + "</custom1_pagebuttons>\n";
        text += "  <custom2_lyricsTextColor>" + FullscreenActivity.custom2_lyricsTextColor + "</custom2_lyricsTextColor>\n";
        text += "  <custom2_lyricsBackgroundColor>" + FullscreenActivity.custom2_lyricsBackgroundColor + "</custom2_lyricsBackgroundColor>\n";
        text += "  <custom2_lyricsVerseColor>" + FullscreenActivity.custom2_lyricsVerseColor + "</custom2_lyricsVerseColor>\n";
        text += "  <custom2_lyricsChorusColor>" + FullscreenActivity.custom2_lyricsChorusColor + "</custom2_lyricsChorusColor>\n";
        text += "  <custom2_lyricsBridgeColor>" + FullscreenActivity.custom2_lyricsBridgeColor + "</custom2_lyricsBridgeColor>\n";
        text += "  <custom2_lyricsCommentColor>" + FullscreenActivity.custom2_lyricsCommentColor + "</custom2_lyricsCommentColor>\n";
        text += "  <custom2_lyricsPreChorusColor>" + FullscreenActivity.custom2_lyricsPreChorusColor + "</custom2_lyricsPreChorusColor>\n";
        text += "  <custom2_lyricsTagColor>" + FullscreenActivity.custom2_lyricsTagColor + "</custom2_lyricsTagColor>\n";
        text += "  <custom2_lyricsChordsColor>" + FullscreenActivity.custom2_lyricsChordsColor + "</custom2_lyricsChordsColor>\n";
        text += "  <custom2_lyricsCustomColor>" + FullscreenActivity.custom2_lyricsCustomColor + "</custom2_lyricsCustomColor>\n";
        text += "  <custom2_lyricsCapoColor>" + FullscreenActivity.custom2_lyricsCapoColor + "</custom2_lyricsCapoColor>\n";
        text += "  <custom2_metronome>" + FullscreenActivity.custom2_metronome + "</custom2_metronome>\n";
        text += "  <custom2_pagebuttons>" + FullscreenActivity.custom2_pagebuttons + "</custom2_pagebuttons>\n";
        text += "  <chordInstrument>" + FullscreenActivity.chordInstrument + "</chordInstrument>\n";
        text += "  <popupScale_All>" + FullscreenActivity.popupScale_All + "</popupScale_All>\n";
        text += "  <popupScale_Set>" + FullscreenActivity.popupScale_Set + "</popupScale_Set>\n";
        text += "  <popupDim_All>" + FullscreenActivity.popupDim_All + "</popupDim_All>\n";
        text += "  <popupDim_Set>" + FullscreenActivity.popupDim_Set + "</popupDim_Set>\n";
        text += "  <popupAlpha_All>" + FullscreenActivity.popupAlpha_All + "</popupAlpha_All>\n";
        text += "  <popupAlpha_Set>" + FullscreenActivity.popupAlpha_Set + "</popupAlpha_Set>\n";
        text += "  <popupPosition_All>" + FullscreenActivity.popupPosition_All + "</popupPosition_All>\n";
        text += "  <popupPosition_Set>" + FullscreenActivity.popupPosition_Set + "</popupPosition_Set>\n";
        text += "  <pageButtonAlpha>" + FullscreenActivity.pageButtonAlpha + "</pageButtonAlpha>\n";
        text += "  <grouppagebuttons>" + FullscreenActivity.grouppagebuttons + "</grouppagebuttons>\n";
        text += "</profile>";
        return text;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}