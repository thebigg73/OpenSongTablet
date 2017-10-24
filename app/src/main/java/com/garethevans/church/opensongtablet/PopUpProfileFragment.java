package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Gravity;
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
import java.util.Locale;

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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_profile, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.profile));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

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
                if (xpp.getName().equals("ab_titleSize")) {
                    FullscreenActivity.ab_titleSize = getFloatValue(xpp.nextText(), 13.0f);

                } else if (xpp.getName().equals("ab_authorSize")) {
                    FullscreenActivity.ab_authorSize = getFloatValue(xpp.nextText(), 11.0f);

                } else if (xpp.getName().equals("alphabeticalSize")) {
                    FullscreenActivity.alphabeticalSize = getFloatValue(xpp.nextText(), 14.0f);

                } else if (xpp.getName().equals("alwaysPreferredChordFormat")) {
                    FullscreenActivity.alwaysPreferredChordFormat = getTextValue(xpp.nextText(), "N");

                } else if (xpp.getName().equals("autoProject")) {
                    FullscreenActivity.autoProject = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("autoscroll_default_or_prompt")) {
                    FullscreenActivity.autoscroll_default_or_prompt = getTextValue(xpp.nextText(),"prompt");

                } else if (xpp.getName().equals("autoScrollDelay")) {
                    FullscreenActivity.autoScrollDelay = getIntegerValue(xpp.nextText(),10);

                } else if (xpp.getName().equals("autostartautoscroll")) {
                    FullscreenActivity.autostartautoscroll = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("backgroundImage1")) {
                    FullscreenActivity.backgroundImage1 = getTextValue(xpp.nextText(),"ost_bg.png");

                } else if (xpp.getName().equals("backgroundImage2")) {
                    FullscreenActivity.backgroundImage2 = getTextValue(xpp.nextText(),"ost_bg.png");

                } else if (xpp.getName().equals("backgroundVideo1")) {
                    FullscreenActivity.backgroundVideo1 = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("backgroundVideo2")) {
                    FullscreenActivity.backgroundVideo2 = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("backgroundToUse")) {
                    FullscreenActivity.backgroundToUse = getTextValue(xpp.nextText(),"img1");

                } else if (xpp.getName().equals("backgroundTypeToUse")) {
                    FullscreenActivity.backgroundTypeToUse = getTextValue(xpp.nextText(),"image");

                } else if (xpp.getName().equals("batteryDialOn")) {
                    FullscreenActivity.batteryDialOn = getBooleanValue(xpp.nextText(), true);

                } else if (xpp.getName().equals("batteryOn")) {
                    FullscreenActivity.batteryOn = getBooleanValue(xpp.nextText(), true);

                } else if (xpp.getName().equals("batterySize")) {
                    FullscreenActivity.batterySize = getFloatValue(xpp.nextText(), 9.0f);

                } else if (xpp.getName().equals("bibleFile")) {
                    FullscreenActivity.bibleFile = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("capoDisplay")) {
                    FullscreenActivity.capoDisplay = getTextValue(xpp.nextText(),"both");

                } else if (xpp.getName().equals("chordfontscalesize")) {
                    FullscreenActivity.chordfontscalesize = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("chordFormat")) {
                    FullscreenActivity.chordFormat = getTextValue(xpp.nextText(),"1");

                } else if (xpp.getName().equals("chordInstrument")) {
                    FullscreenActivity.chordInstrument = getTextValue(xpp.nextText(),"g");

                } else if (xpp.getName().equals("commentfontscalesize")) {
                    FullscreenActivity.commentfontscalesize = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("custom1_lyricsBackgroundColor")) {
                    FullscreenActivity.custom1_lyricsBackgroundColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("custom1_lyricsBridgeColor")) {
                    FullscreenActivity.custom1_lyricsBridgeColor = getIntegerValue(xpp.nextText(),0xff330000);
                } else if (xpp.getName().equals("custom1_lyricsCapoColor")) {
                    FullscreenActivity.custom1_lyricsCapoColor = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("custom1_lyricsChordsColor")) {
                    FullscreenActivity.custom1_lyricsChordsColor = getIntegerValue(xpp.nextText(),0xffffff00);
                } else if (xpp.getName().equals("custom1_lyricsChorusColor")) {
                    FullscreenActivity.custom1_lyricsChorusColor = getIntegerValue(xpp.nextText(),0xff000033);
                } else if (xpp.getName().equals("custom1_lyricsCommentColor")) {
                    FullscreenActivity.custom1_lyricsCommentColor = getIntegerValue(xpp.nextText(),0xff003300);
                } else if (xpp.getName().equals("custom1_lyricsCustomColor")) {
                    FullscreenActivity.custom1_lyricsCustomColor = getIntegerValue(xpp.nextText(),0xff222200);
                } else if (xpp.getName().equals("custom1_lyricsPreChorusColor")) {
                    FullscreenActivity.custom1_lyricsPreChorusColor = getIntegerValue(xpp.nextText(),0xff112211);
                } else if (xpp.getName().equals("custom1_lyricsTagColor")) {
                    FullscreenActivity.custom1_lyricsTagColor = getIntegerValue(xpp.nextText(),0xff330033);
                } else if (xpp.getName().equals("custom1_lyricsTextColor")) {
                    FullscreenActivity.custom1_lyricsTextColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom1_lyricsVerseColor")) {
                    FullscreenActivity.custom1_lyricsVerseColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("custom1_metronome")) {
                    FullscreenActivity.custom1_metronome = getIntegerValue(xpp.nextText(),0xffaa1212);
                } else if (xpp.getName().equals("custom1_pagebuttons")) {
                    FullscreenActivity.custom1_pagebuttons = getIntegerValue(xpp.nextText(),0xff452277);
                } else if (xpp.getName().equals("custom1_presoAlertFont")) {
                    FullscreenActivity.custom1_presoAlertFont = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("custom1_presoFont")) {
                    FullscreenActivity.custom1_presoFont = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom1_presoInfoFont")) {
                    FullscreenActivity.custom1_presoInfoFont = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom1_presoShadow")) {
                    FullscreenActivity.custom1_presoShadow = getIntegerValue(xpp.nextText(),0xff000000);

                } else if (xpp.getName().equals("custom2_lyricsBackgroundColor")) {
                    FullscreenActivity.custom2_lyricsBackgroundColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom2_lyricsBridgeColor")) {
                    FullscreenActivity.custom2_lyricsBridgeColor = getIntegerValue(xpp.nextText(),0xffddffff);
                } else if (xpp.getName().equals("custom2_lyricsCapoColor")) {
                    FullscreenActivity.custom2_lyricsCapoColor = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("custom2_lyricsChordsColor")) {
                    FullscreenActivity.custom2_lyricsChordsColor = getIntegerValue(xpp.nextText(),0xff0000dd);
                } else if (xpp.getName().equals("custom2_lyricsChorusColor")) {
                    FullscreenActivity.custom2_lyricsChorusColor = getIntegerValue(xpp.nextText(),0xffffddff);
                } else if (xpp.getName().equals("custom2_lyricsCommentColor")) {
                    FullscreenActivity.custom2_lyricsCommentColor = getIntegerValue(xpp.nextText(),0xffddddff);
                } else if (xpp.getName().equals("custom2_lyricsCustomColor")) {
                    FullscreenActivity.custom2_lyricsCustomColor = getIntegerValue(xpp.nextText(),0xffccddff);
                } else if (xpp.getName().equals("custom2_lyricsPreChorusColor")) {
                    FullscreenActivity.custom2_lyricsPreChorusColor = getIntegerValue(xpp.nextText(),0xffeeccee);
                } else if (xpp.getName().equals("custom2_lyricsTagColor")) {
                    FullscreenActivity.custom2_lyricsTagColor = getIntegerValue(xpp.nextText(),0xffddffdd);
                } else if (xpp.getName().equals("custom2_lyricsTextColor")) {
                    FullscreenActivity.custom2_lyricsTextColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("custom2_lyricsVerseColor")) {
                    FullscreenActivity.custom2_lyricsVerseColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom2_metronome")) {
                    FullscreenActivity.custom2_metronome = getIntegerValue(xpp.nextText(),0xffaa1212);
                } else if (xpp.getName().equals("custom2_pagebuttons")) {
                    FullscreenActivity.custom2_pagebuttons = getIntegerValue(xpp.nextText(),0xff452277);
                } else if (xpp.getName().equals("custom2_presoAlertFont")) {
                    FullscreenActivity.custom2_presoAlertFont = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("custom2_presoFont")) {
                    FullscreenActivity.custom2_presoFont = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom2_presoInfoFont")) {
                    FullscreenActivity.custom2_presoInfoFont = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("custom2_presoShadow")) {
                    FullscreenActivity.custom2_presoShadow = getIntegerValue(xpp.nextText(),0xff000000);

                } else if (xpp.getName().equals("customLogo")) {
                    FullscreenActivity.customLogo = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("customLogoSize")) {
                    FullscreenActivity.customLogoSize = getFloatValue(xpp.nextText(),0.5f);

                } else if (xpp.getName().equals("customStorage")) {
                    FullscreenActivity.customStorage = getTextValue(xpp.nextText(), Environment.getExternalStorageDirectory().getAbsolutePath());

                } else if (xpp.getName().equals("dark_lyricsBackgroundColor")) {
                    FullscreenActivity.dark_lyricsBackgroundColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("dark_lyricsBridgeColor")) {
                    FullscreenActivity.dark_lyricsBridgeColor = getIntegerValue(xpp.nextText(),0xff330000);
                } else if (xpp.getName().equals("dark_lyricsCapoColor")) {
                    FullscreenActivity.dark_lyricsCapoColor = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("dark_lyricsChordsColor")) {
                    FullscreenActivity.dark_lyricsChordsColor = getIntegerValue(xpp.nextText(),0xffffff00);
                } else if (xpp.getName().equals("dark_lyricsChorusColor")) {
                    FullscreenActivity.dark_lyricsChorusColor = getIntegerValue(xpp.nextText(),0xff000033);
                } else if (xpp.getName().equals("dark_lyricsCommentColor")) {
                    FullscreenActivity.dark_lyricsCommentColor = getIntegerValue(xpp.nextText(),0xff003300);
                } else if (xpp.getName().equals("dark_lyricsCustomColor")) {
                    FullscreenActivity.dark_lyricsCustomColor = getIntegerValue(xpp.nextText(),0xff222200);
                } else if (xpp.getName().equals("dark_lyricsPreChorusColor")) {
                    FullscreenActivity.dark_lyricsPreChorusColor = getIntegerValue(xpp.nextText(),0xff112211);
                } else if (xpp.getName().equals("dark_lyricsTagColor")) {
                    FullscreenActivity.dark_lyricsTagColor = getIntegerValue(xpp.nextText(),0xff330033);
                } else if (xpp.getName().equals("dark_lyricsTextColor")) {
                    FullscreenActivity.dark_lyricsTextColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("dark_lyricsVerseColor")) {
                    FullscreenActivity.dark_lyricsVerseColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("dark_metronome")) {
                    FullscreenActivity.dark_metronome = getIntegerValue(xpp.nextText(),0xffaa1212);
                } else if (xpp.getName().equals("dark_pagebuttons")) {
                    FullscreenActivity.dark_pagebuttons = getIntegerValue(xpp.nextText(),0xff452277);
                } else if (xpp.getName().equals("dark_presoAlertFont")) {
                    FullscreenActivity.dark_presoAlertFont = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("dark_presoFont")) {
                    FullscreenActivity.dark_presoFont = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("dark_presoInfoFont")) {
                    FullscreenActivity.dark_presoInfoFont = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("dark_presoShadow")) {
                    FullscreenActivity.dark_presoShadow = getIntegerValue(xpp.nextText(),0xff000000);

                } else if (xpp.getName().equals("default_autoscroll_predelay")) {
                    FullscreenActivity.default_autoscroll_predelay = getIntegerValue(xpp.nextText(),10);

                } else if (xpp.getName().equals("default_autoscroll_songlength")) {
                    FullscreenActivity.default_autoscroll_songlength = getIntegerValue(xpp.nextText(), 180);

                } else if (xpp.getName().equals("drawingEraserSize")) {
                    FullscreenActivity.drawingEraserSize = getIntegerValue(xpp.nextText(), 20);
                } else if (xpp.getName().equals("drawingHighlightColor")) {
                    FullscreenActivity.drawingHighlightColor = getTextValue(xpp.nextText(), "yellow");
                } else if (xpp.getName().equals("drawingHighlightSize")) {
                    FullscreenActivity.drawingHighlightSize = getIntegerValue(xpp.nextText(), 20);
                } else if (xpp.getName().equals("drawingPenColor")) {
                    FullscreenActivity.drawingPenColor = getTextValue(xpp.nextText(), "yellow");
                } else if (xpp.getName().equals("drawingPenSize")) {
                    FullscreenActivity.drawingPenSize = getIntegerValue(xpp.nextText(), 20);
                } else if (xpp.getName().equals("drawingTool")) {
                    FullscreenActivity.drawingTool = getTextValue(xpp.nextText(), "highlighter");

                } else if (xpp.getName().equals("exportOpenSongAppSet")) {
                    FullscreenActivity.exportOpenSongAppSet = getBooleanValue(xpp.nextText(),false);
                } else if (xpp.getName().equals("exportOpenSongApp")) {
                    FullscreenActivity.exportOpenSongApp = getBooleanValue(xpp.nextText(),false);
                } else if (xpp.getName().equals("exportDesktop")) {
                    FullscreenActivity.exportDesktop = getBooleanValue(xpp.nextText(),false);
                } else if (xpp.getName().equals("exportText")) {
                    FullscreenActivity.exportText = getBooleanValue(xpp.nextText(),false);
                } else if (xpp.getName().equals("exportChordPro")) {
                    FullscreenActivity.exportChordPro = getBooleanValue(xpp.nextText(),false);
                } else if (xpp.getName().equals("exportOnSong")) {
                    FullscreenActivity.exportOnSong = getBooleanValue(xpp.nextText(),false);
                } else if (xpp.getName().equals("exportImage")) {
                    FullscreenActivity.exportImage = getBooleanValue(xpp.nextText(),false);
                } else if (xpp.getName().equals("exportPDF")) {
                    FullscreenActivity.exportPDF = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("fabSize")) {
                    FullscreenActivity.fabSize = getIntegerValue(xpp.nextText(), FloatingActionButton.SIZE_MINI);

                } else if (xpp.getName().equals("gesture_doubletap")) {
                    FullscreenActivity.gesture_doubletap = getTextValue(xpp.nextText(), "2");

                } else if (xpp.getName().equals("gesture_longpress")) {
                    FullscreenActivity.gesture_longpress = getTextValue(xpp.nextText(), "1");

                } else if (xpp.getName().equals("grouppagebuttons")) {
                    FullscreenActivity.grouppagebuttons = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("headingfontscalesize")) {
                    FullscreenActivity.headingfontscalesize = getFloatValue(xpp.nextText(),0.6f);

                } else if (xpp.getName().equals("hideActionBar")) {
                    FullscreenActivity.hideActionBar = getBooleanValue(xpp.nextText(), false);

                } else if (xpp.getName().equals("highlightShowSecs")) {
                    FullscreenActivity.highlightShowSecs = getIntegerValue(xpp.nextText(), 0);

                } else if (xpp.getName().equals("languageToLoad")) {
                    FullscreenActivity.languageToLoad = getTextValue(xpp.nextText(), "");

                } else if (xpp.getName().equals("lastSetName")) {
                    FullscreenActivity.lastSetName = getTextValue(xpp.nextText(), "");

                } else if (xpp.getName().equals("light_lyricsBackgroundColor")) {
                    FullscreenActivity.light_lyricsBackgroundColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("light_lyricsBridgeColor")) {
                    FullscreenActivity.light_lyricsBridgeColor = getIntegerValue(xpp.nextText(),0xffddffff);
                } else if (xpp.getName().equals("light_lyricsCapoColor")) {
                    FullscreenActivity.light_lyricsCapoColor = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("light_lyricsChordsColor")) {
                    FullscreenActivity.light_lyricsChordsColor = getIntegerValue(xpp.nextText(),0xff0000dd);
                } else if (xpp.getName().equals("light_lyricsChorusColor")) {
                    FullscreenActivity.light_lyricsChorusColor = getIntegerValue(xpp.nextText(),0xffffddff);
                } else if (xpp.getName().equals("light_lyricsCommentColor")) {
                    FullscreenActivity.light_lyricsCommentColor = getIntegerValue(xpp.nextText(),0xffddddff);
                } else if (xpp.getName().equals("light_lyricsCustomColor")) {
                    FullscreenActivity.light_lyricsCustomColor = getIntegerValue(xpp.nextText(),0xffccddff);
                } else if (xpp.getName().equals("light_lyricsPreChorusColor")) {
                    FullscreenActivity.light_lyricsPreChorusColor = getIntegerValue(xpp.nextText(),0xffeeccee);
                } else if (xpp.getName().equals("light_lyricsTagColor")) {
                    FullscreenActivity.light_lyricsTagColor = getIntegerValue(xpp.nextText(),0xffddffdd);
                } else if (xpp.getName().equals("light_lyricsTextColor")) {
                    FullscreenActivity.light_lyricsTextColor = getIntegerValue(xpp.nextText(),0xff000000);
                } else if (xpp.getName().equals("light_lyricsVerseColor")) {
                    FullscreenActivity.light_lyricsVerseColor = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("light_metronome")) {
                    FullscreenActivity.light_metronome = getIntegerValue(xpp.nextText(),0xffaa1212);
                } else if (xpp.getName().equals("light_pagebuttons")) {
                    FullscreenActivity.light_pagebuttons = getIntegerValue(xpp.nextText(),0xff452277);
                } else if (xpp.getName().equals("light_presoAlertFont")) {
                    FullscreenActivity.light_presoAlertFont = getIntegerValue(xpp.nextText(),0xffff0000);
                } else if (xpp.getName().equals("light_presoFont")) {
                    FullscreenActivity.light_presoFont = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("light_presoInfoFont")) {
                    FullscreenActivity.light_presoInfoFont = getIntegerValue(xpp.nextText(),0xffffffff);
                } else if (xpp.getName().equals("light_presoShadow")) {
                    FullscreenActivity.light_presoShadow = getIntegerValue(xpp.nextText(),0xff000000);

                } else if (xpp.getName().equals("linespacing")) {
                    FullscreenActivity.linespacing = getIntegerValue(xpp.nextText(),0);

                } else if (xpp.getName().equals("locale")) {
                    FullscreenActivity.locale = new Locale(getTextValue(xpp.nextText(),Preferences.getStoredLocale().toString()));

                } else if (xpp.getName().equals("longpressdownpedalgesture")) {
                    FullscreenActivity.longpressdownpedalgesture = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("longpressnextpedalgesture")) {
                    FullscreenActivity.longpressnextpedalgesture = getTextValue(xpp.nextText(),"4");

                } else if (xpp.getName().equals("longpresspreviouspedalgesture")) {
                    FullscreenActivity.longpresspreviouspedalgesture = getTextValue(xpp.nextText(),"1");

                } else if (xpp.getName().equals("longpressuppedalgesture")) {
                    FullscreenActivity.longpressuppedalgesture = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("maxvolrange")) {
                    FullscreenActivity.maxvolrange = getIntegerValue(xpp.nextText(),400);

                } else if (xpp.getName().equals("mediaStore")) {
                    FullscreenActivity.mediaStore = getTextValue(xpp.nextText(),"int");

                } else if (xpp.getName().equals("metronomepan")) {
                    FullscreenActivity.metronomepan = getTextValue(xpp.nextText(),"both");

                } else if (xpp.getName().equals("metronomevol")) {
                    FullscreenActivity.metronomevol = getFloatValue(xpp.nextText(),0.5f);

                } else if (xpp.getName().equals("mAuthor")) {
                    FullscreenActivity.mAuthor = getTextValue(xpp.nextText(),"Gareth Evans");

                } else if (xpp.getName().equals("mCopyright")) {
                    FullscreenActivity.mCopyright = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("mDisplayTheme")) {
                    FullscreenActivity.mDisplayTheme = getTextValue(xpp.nextText(),"Theme.Holo");

                } else if (xpp.getName().equals("menuSize")) {
                    FullscreenActivity.menuSize = getFloatValue(xpp.nextText(), 0.6f);

                } else if (xpp.getName().equals("mFontSize")) {
                    FullscreenActivity.mFontSize = getFloatValue(xpp.nextText(),42.0f);

                } else if (xpp.getName().equals("mMaxFontSize")) {
                    FullscreenActivity.mMaxFontSize = getIntegerValue(xpp.nextText(),50);

                } else if (xpp.getName().equals("mMinFontSize")) {
                    FullscreenActivity.mMinFontSize = getIntegerValue(xpp.nextText(),8);

                } else if (xpp.getName().equals("mStorage")) {
                    FullscreenActivity.mStorage = getTextValue(xpp.nextText(),"int");

                } else if (xpp.getName().equals("mTitle")) {
                    FullscreenActivity.mTitle = getTextValue(xpp.nextText(),"Welcome to OpenSongApp");

                } else if (xpp.getName().equals("myAlert")) {
                    FullscreenActivity.myAlert = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("mychordsfontnum")) {
                    FullscreenActivity.mychordsfontnum = getIntegerValue(xpp.nextText(),8);

                } else if (xpp.getName().equals("mylyricsfontnum")) {
                    FullscreenActivity.mylyricsfontnum = getIntegerValue(xpp.nextText(),8);

                } else if (xpp.getName().equals("mypresofontnum")) {
                    FullscreenActivity.mypresofontnum = getIntegerValue(xpp.nextText(),8);

                } else if (xpp.getName().equals("mypresoinfofontnum")) {
                    FullscreenActivity.mypresoinfofontnum = getIntegerValue(xpp.nextText(),8);

                } else if (xpp.getName().equals("mySet")) {
                    FullscreenActivity.mySet = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("override_fullscale")) {
                    FullscreenActivity.override_fullscale = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("override_widthscale")) {
                    FullscreenActivity.override_widthscale = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("padpan")) {
                    FullscreenActivity.padpan = getTextValue(xpp.nextText(),"both");

                } else if (xpp.getName().equals("padvol")) {
                    FullscreenActivity.padvol = getFloatValue(xpp.nextText(), 1.0f);

                } else if (xpp.getName().equals("page_autoscroll_visible")) {
                    FullscreenActivity.page_autoscroll_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_chord_visible")) {
                    FullscreenActivity.page_chord_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_custom_grouped")) {
                    FullscreenActivity.page_custom_grouped = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_custom_visible")) {
                    FullscreenActivity.page_custom_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_custom1_visible")) {
                    FullscreenActivity.page_custom1_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_custom2_visible")) {
                    FullscreenActivity.page_custom2_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_custom3_visible")) {
                    FullscreenActivity.page_custom3_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_custom4_visible")) {
                    FullscreenActivity.page_custom4_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_extra_grouped")) {
                    FullscreenActivity.page_extra_grouped = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_extra_visible")) {
                    FullscreenActivity.page_extra_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_highlight_visible")) {
                    FullscreenActivity.page_highlight_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_links_visible")) {
                    FullscreenActivity.page_links_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_metronome_visible")) {
                    FullscreenActivity.page_metronome_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_notation_visible")) {
                    FullscreenActivity.page_notation_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_pad_visible")) {
                    FullscreenActivity.page_pad_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_pages_visible")) {
                    FullscreenActivity.page_pages_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_set_visible")) {
                    FullscreenActivity.page_set_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("page_sticky_visible")) {
                    FullscreenActivity.page_sticky_visible = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("pagebutton_position")) {
                    FullscreenActivity.pagebutton_position = getTextValue(xpp.nextText(),"right");

                } else if (xpp.getName().equals("pagebutton_scale")) {
                    FullscreenActivity.pagebutton_scale = getTextValue(xpp.nextText(),"M");

                } else if (xpp.getName().equals("pageButtonAlpha")) {
                    FullscreenActivity.pageButtonAlpha = getFloatValue(xpp.nextText(),0.2f);

                } else if (xpp.getName().equals("pageturner_AUTOSCROLL")) {
                    FullscreenActivity.pageturner_AUTOSCROLL = getIntegerValue(xpp.nextText(), -1);

                } else if (xpp.getName().equals("pageturner_AUTOSCROLLPAD")) {
                    FullscreenActivity.pageturner_AUTOSCROLLPAD = getIntegerValue(xpp.nextText(), -1);

                } else if (xpp.getName().equals("pageturner_AUTOSCROLLMETRONOME")) {
                    FullscreenActivity.pageturner_AUTOSCROLLMETRONOME = getIntegerValue(xpp.nextText(), -1);

                } else if (xpp.getName().equals("pageturner_AUTOSCROLLPADMETRONOME")) {
                    FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME = getIntegerValue(xpp.nextText(), -1);

                } else if (xpp.getName().equals("pageturner_DOWN")) {
                    FullscreenActivity.pageturner_DOWN = getIntegerValue(xpp.nextText(), 20);

                } else if (xpp.getName().equals("pageturner_METRONOME")) {
                    FullscreenActivity.pageturner_METRONOME = getIntegerValue(xpp.nextText(), -1);

                } else if (xpp.getName().equals("pageturner_NEXT")) {
                    FullscreenActivity.pageturner_NEXT = getIntegerValue(xpp.nextText(), 22);

                } else if (xpp.getName().equals("pageturner_PAD")) {
                    FullscreenActivity.pageturner_PAD = getIntegerValue(xpp.nextText(), -1);

                } else if (xpp.getName().equals("pageturner_PADMETRONOME")) {
                    FullscreenActivity.pageturner_PADMETRONOME = getIntegerValue(xpp.nextText(), -1);

                } else if (xpp.getName().equals("pageturner_PREVIOUS")) {
                    FullscreenActivity.pageturner_PREVIOUS = getIntegerValue(xpp.nextText(), 21);

                } else if (xpp.getName().equals("pageturner_UP")) {
                    FullscreenActivity.pageturner_UP = getIntegerValue(xpp.nextText(), 19);

                } else if (xpp.getName().equals("popupAlpha_All")) {
                    FullscreenActivity.popupAlpha_All = getFloatValue(xpp.nextText(),0.7f);

                } else if (xpp.getName().equals("popupAlpha_Set")) {
                    FullscreenActivity.popupAlpha_Set = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupDim_All")) {
                    FullscreenActivity.popupDim_All = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupDim_Set")) {
                    FullscreenActivity.popupDim_Set = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupPosition_All")) {
                    FullscreenActivity.popupPosition_All = getTextValue(xpp.nextText(),"c");

                } else if (xpp.getName().equals("popupPosition_Set")) {
                    FullscreenActivity.popupPosition_Set = getTextValue(xpp.nextText(),"c");

                } else if (xpp.getName().equals("popupScale_All")) {
                    FullscreenActivity.popupScale_All = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("popupScale_Set")) {
                    FullscreenActivity.popupScale_Set = getFloatValue(xpp.nextText(),0.8f);

                } else if (xpp.getName().equals("prefChord_Aflat_Gsharp")) {
                    FullscreenActivity.prefChord_Aflat_Gsharp = getTextValue(xpp.nextText(),"b");
                } else if (xpp.getName().equals("prefChord_Bflat_Asharp")) {
                    FullscreenActivity.prefChord_Bflat_Asharp = getTextValue(xpp.nextText(),"b");
                } else if (xpp.getName().equals("prefChord_Dflat_Csharp")) {
                    FullscreenActivity.prefChord_Dflat_Csharp = getTextValue(xpp.nextText(),"b");
                } else if (xpp.getName().equals("prefChord_Eflat_Dsharp")) {
                    FullscreenActivity.prefChord_Eflat_Dsharp = getTextValue(xpp.nextText(),"b");
                } else if (xpp.getName().equals("prefChord_Gflat_Fsharp")) {
                    FullscreenActivity.prefChord_Gflat_Fsharp = getTextValue(xpp.nextText(),"b");
                } else if (xpp.getName().equals("prefChord_Aflatm_Gsharpm")) {
                    FullscreenActivity.prefChord_Aflatm_Gsharpm = getTextValue(xpp.nextText(),"#");
                } else if (xpp.getName().equals("prefChord_Bflatm_Asharpm")) {
                    FullscreenActivity.prefChord_Bflatm_Asharpm = getTextValue(xpp.nextText(),"b");
                } else if (xpp.getName().equals("prefChord_Dflatm_Csharpm")) {
                    FullscreenActivity.prefChord_Dflatm_Csharpm = getTextValue(xpp.nextText(),"#");
                } else if (xpp.getName().equals("prefChord_Eflatm_Dsharpm")) {
                    FullscreenActivity.prefChord_Eflatm_Dsharpm = getTextValue(xpp.nextText(),"b");
                } else if (xpp.getName().equals("prefChord_Gflatm_Fsharpm")) {
                    FullscreenActivity.prefChord_Gflatm_Fsharpm = getTextValue(xpp.nextText(),"#");

                } else if (xpp.getName().equals("prefStorage")) {
                    FullscreenActivity.prefStorage = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("presenterChords")) {
                    FullscreenActivity.presenterChords = getTextValue(xpp.nextText(),"N");

                } else if (xpp.getName().equals("presoAlpha")) {
                    FullscreenActivity.presoAlpha = getFloatValue(xpp.nextText(),1.0f);

                } else if (xpp.getName().equals("presoAlertSize")) {
                    FullscreenActivity.presoAlertSize = getFloatValue(xpp.nextText(),8.0f);

                } else if (xpp.getName().equals("presoAuthorSize")) {
                    FullscreenActivity.presoAuthorSize = getFloatValue(xpp.nextText(),8.0f);

                } else if (xpp.getName().equals("presoAutoScale")) {
                    FullscreenActivity.presoAutoScale = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("presoCopyrightSize")) {
                    FullscreenActivity.presoCopyrightSize = getFloatValue(xpp.nextText(),8.0f);

                } else if (xpp.getName().equals("presoFontSize")) {
                    FullscreenActivity.presoFontSize = getIntegerValue(xpp.nextText(),12);

                } else if (xpp.getName().equals("presoInfoAlign")) {
                    FullscreenActivity.presoInfoAlign = getIntegerValue(xpp.nextText(), Gravity.RIGHT);

                } else if (xpp.getName().equals("presoLyricsAlign")) {
                    FullscreenActivity.presoLyricsAlign = getIntegerValue(xpp.nextText(), Gravity.CENTER_HORIZONTAL);

                } else if (xpp.getName().equals("presoMaxFontSize")) {
                    FullscreenActivity.presoLyricsAlign = getIntegerValue(xpp.nextText(), 40);

                } else if (xpp.getName().equals("presoShowChords")) {
                    FullscreenActivity.presoShowChords = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("presoTitleSize")) {
                    FullscreenActivity.presoTitleSize = getFloatValue(xpp.nextText(),10.0f);

                } else if (xpp.getName().equals("presoTransitionTime")) {
                    FullscreenActivity.presoTransitionTime = getIntegerValue(xpp.nextText(), 800);

                } else if (xpp.getName().equals("profile")) {
                    FullscreenActivity.profile = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("quickLaunchButton_1")) {
                    FullscreenActivity.quickLaunchButton_1 = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("quickLaunchButton_2")) {
                    FullscreenActivity.quickLaunchButton_2 = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("quickLaunchButton_3")) {
                    FullscreenActivity.quickLaunchButton_3 = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("quickLaunchButton_4")) {
                    FullscreenActivity.quickLaunchButton_4 = getTextValue(xpp.nextText(),"");

                } else if (xpp.getName().equals("scrollDistance")) {
                    FullscreenActivity.scrollDistance = getFloatValue(xpp.nextText(),0.6f);

                } else if (xpp.getName().equals("scrollSpeed")) {
                    FullscreenActivity.scrollSpeed = getIntegerValue(xpp.nextText(),1500);

                } else if (xpp.getName().equals("showAlphabeticalIndexInSongMenu")) {
                    FullscreenActivity.showAlphabeticalIndexInSongMenu = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("showCapoChords")) {
                    FullscreenActivity.showCapoChords = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("showLyrics")) {
                    FullscreenActivity.showLyrics = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("showNativeAndCapoChords")) {
                    FullscreenActivity.showNativeAndCapoChords = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("showChords")) {
                    FullscreenActivity.showChords = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("showNextInSet")) {
                    FullscreenActivity.showNextInSet = getTextValue(xpp.nextText(),"bottom");

                } else if (xpp.getName().equals("showSetTickBoxInSongMenu")) {
                    FullscreenActivity.showSetTickBoxInSongMenu = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("songfilename")) {
                    FullscreenActivity.songfilename = getTextValue(xpp.nextText(), "");

                } else if (xpp.getName().equals("stagemodeScale")) {
                    FullscreenActivity.stagemodeScale = getFloatValue(xpp.nextText(), 0.7f);

                } else if (xpp.getName().equals("stickyNotesShowSecs")) {
                    FullscreenActivity.stickyNotesShowSecs = getIntegerValue(xpp.nextText(), 8);

                } else if (xpp.getName().equals("stickyOpacity")) {
                    FullscreenActivity.stickyOpacity = getFloatValue(xpp.nextText(), 0.8f);

                } else if (xpp.getName().equals("stickyTextSize")) {
                    FullscreenActivity.stickyTextSize = getFloatValue(xpp.nextText(), 14.0f);

                } else if (xpp.getName().equals("stickyWidth")) {
                    FullscreenActivity.stickyWidth = getIntegerValue(xpp.nextText(), 400);


                } else if (xpp.getName().equals("SWIPE_MAX_OFF_PATH")) {
                    FullscreenActivity.SWIPE_MAX_OFF_PATH = getIntegerValue(xpp.nextText(), 200);

                } else if (xpp.getName().equals("SWIPE_MIN_DISTANCE")) {
                    FullscreenActivity.SWIPE_MIN_DISTANCE = getIntegerValue(xpp.nextText(), 250);

                } else if (xpp.getName().equals("SWIPE_THRESHOLD_VELOCITY")) {
                    FullscreenActivity.SWIPE_THRESHOLD_VELOCITY = getIntegerValue(xpp.nextText(), 600);


                } else if (xpp.getName().equals("swipeDrawer")) {
                    FullscreenActivity.swipeDrawer = getTextValue(xpp.nextText(),"Y");

                } else if (xpp.getName().equals("swipeForMenus")) {
                    FullscreenActivity.swipeForMenus = getBooleanValue(xpp.nextText(), true);

                } else if (xpp.getName().equals("swipeForSongs")) {
                    FullscreenActivity.swipeForSongs = getBooleanValue(xpp.nextText(),true);

                } else if (xpp.getName().equals("swipeSet")) {
                    FullscreenActivity.swipeSet = getTextValue(xpp.nextText(),"Y");

                } else if (xpp.getName().equals("timerFontSizeAutoScroll")) {
                    FullscreenActivity.timerFontSizeAutoScroll = getFloatValue(xpp.nextText(),14.0f);

                } else if (xpp.getName().equals("timerFontSizePad")) {
                    FullscreenActivity.timerFontSizePad = getFloatValue(xpp.nextText(), 14.0f);

                } else if (xpp.getName().equals("timeFormat24h")) {
                    FullscreenActivity.timeFormat24h = getBooleanValue(xpp.nextText(), true);

                } else if (xpp.getName().equals("timeOn")) {
                        FullscreenActivity.timeOn = getBooleanValue(xpp.nextText(), true);

                } else if (xpp.getName().equals("timeSize")) {
                    FullscreenActivity.timeSize = getFloatValue(xpp.nextText(), 9.0f);

                } else if (xpp.getName().equals("toggleAutoHighlight")) {
                    FullscreenActivity.toggleAutoHighlight = getBooleanValue(xpp.nextText(), true);

                } else if (xpp.getName().equals("toggleAutoSticky")) {
                    FullscreenActivity.toggleAutoSticky = getTextValue(xpp.nextText(),"N");

                } else if (xpp.getName().equals("togglePageButtons")) {
                    FullscreenActivity.togglePageButtons = getTextValue(xpp.nextText(),"Y");

                } else if (xpp.getName().equals("toggleScrollArrows")) {
                    FullscreenActivity.toggleScrollArrows = getTextValue(xpp.nextText(),"S");

                } else if (xpp.getName().equals("toggleScrollBeforeSwipe")) {
                    FullscreenActivity.toggleScrollBeforeSwipe = getTextValue(xpp.nextText(),"Y");

                } else if (xpp.getName().equals("toggleYScale")) {
                    FullscreenActivity.toggleYScale = getTextValue(xpp.nextText(),"W");

                } else if (xpp.getName().equals("transposeStyle")) {
                    FullscreenActivity.transposeStyle = getTextValue(xpp.nextText(), "sharps");

                } else if (xpp.getName().equals("trimSections")) {
                    FullscreenActivity.trimSections = getBooleanValue(xpp.nextText(), false);

                } else if (xpp.getName().equals("trimSectionSpace")) {
                    FullscreenActivity.trimSectionSpace = getBooleanValue(xpp.nextText(), false);

                } else if (xpp.getName().equals("usePresentationOrder")) {
                    FullscreenActivity.usePresentationOrder = getBooleanValue(xpp.nextText(),false);

                } else if (xpp.getName().equals("visualmetronome")) {
                    FullscreenActivity.visualmetronome = getBooleanValue(xpp.nextText(),false);

                //} else if (xpp.getName().equals("whichMode")) {
                //    FullscreenActivity.whichMode = getTextValue(xpp.nextText(),"Performance");

                } else if (xpp.getName().equals("whichSetCategory")) {
                    FullscreenActivity.whichSetCategory = getTextValue(xpp.nextText(),FullscreenActivity.mainfoldername);

                } else if (xpp.getName().equals("whichSongFolder")) {
                    FullscreenActivity.whichSongFolder = getTextValue(xpp.nextText(),FullscreenActivity.mainfoldername);

                } else if (xpp.getName().equals("xmargin_presentation")) {
                    FullscreenActivity.xmargin_presentation = getIntegerValue(xpp.nextText(),50);

                } else if (xpp.getName().equals("ymargin_presentation")) {
                    FullscreenActivity.ymargin_presentation = getIntegerValue(xpp.nextText(),25);

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
        text += "<myprofile>\n";
        text += "  <ab_titleSize>" + FullscreenActivity.ab_titleSize + "</ab_titleSize>\n";
        text += "  <ab_authorSize>" + FullscreenActivity.ab_authorSize + "</ab_authorSize>\n";
        text += "  <alphabeticalSize>" + FullscreenActivity.alphabeticalSize + "</alphabeticalSize>\n";
        text += "  <alwaysPreferredChordFormat>" + FullscreenActivity.alwaysPreferredChordFormat + "</alwaysPreferredChordFormat>\n";
        text += "  <autoProject>" + FullscreenActivity.autoProject + "</autoProject>\n";
        text += "  <autoscroll_default_or_prompt>" + FullscreenActivity.autoscroll_default_or_prompt + "</autoscroll_default_or_prompt>\n";
        text += "  <autoScrollDelay>" + FullscreenActivity.autoScrollDelay + "</autoScrollDelay>\n";
        text += "  <autostartautoscroll>" + FullscreenActivity.autostartautoscroll + "</autostartautoscroll>\n";
        text += "  <backgroundImage1>" + FullscreenActivity.backgroundImage1 + "</backgroundImage1>\n";
        text += "  <backgroundImage2>" + FullscreenActivity.backgroundImage2 + "</backgroundImage2>\n";
        text += "  <backgroundVideo1>" + FullscreenActivity.backgroundVideo1 + "</backgroundVideo1>\n";
        text += "  <backgroundVideo2>" + FullscreenActivity.backgroundVideo2 + "</backgroundVideo2>\n";
        text += "  <backgroundToUse>" + FullscreenActivity.backgroundToUse + "</backgroundToUse>\n";
        text += "  <backgroundTypeToUse>" + FullscreenActivity.backgroundTypeToUse + "</backgroundTypeToUse>\n";
        text += "  <batteryDialOn>" + FullscreenActivity.batteryDialOn + "</batteryDialOn>\n";
        text += "  <batteryOn>" + FullscreenActivity.batteryOn + "</batteryOn>\n";
        text += "  <batterySize>" + FullscreenActivity.batterySize + "</batterySize>\n";
        text += "  <bibleFile>" + FullscreenActivity.bibleFile + "</bibleFile>\n";
        text += "  <capoDisplay>" + FullscreenActivity.capoDisplay + "</capoDisplay>\n";
        text += "  <chordfontscalesize>" + FullscreenActivity.chordfontscalesize + "</chordfontscalesize>\n";
        text += "  <chordFormat>" + FullscreenActivity.chordFormat + "</chordFormat>\n";
        text += "  <chordInstrument>" + FullscreenActivity.chordInstrument + "</chordInstrument>\n";
        text += "  <commentfontscalesize>" + FullscreenActivity.commentfontscalesize + "</commentfontscalesize>\n";
        text += "  <custom1_lyricsBackgroundColor>" + FullscreenActivity.custom1_lyricsBackgroundColor + "</custom1_lyricsBackgroundColor>\n";
        text += "  <custom1_lyricsBridgeColor>" + FullscreenActivity.custom1_lyricsBridgeColor + "</custom1_lyricsBridgeColor>\n";
        text += "  <custom1_lyricsCapoColor>" + FullscreenActivity.custom1_lyricsCapoColor + "</custom1_lyricsCapoColor>\n";
        text += "  <custom1_lyricsChordsColor>" + FullscreenActivity.custom1_lyricsChordsColor + "</custom1_lyricsChordsColor>\n";
        text += "  <custom1_lyricsChorusColor>" + FullscreenActivity.custom1_lyricsChorusColor + "</custom1_lyricsChorusColor>\n";
        text += "  <custom1_lyricsCommentColor>" + FullscreenActivity.custom1_lyricsCommentColor + "</custom1_lyricsCommentColor>\n";
        text += "  <custom1_lyricsCustomColor>" + FullscreenActivity.custom1_lyricsCustomColor + "</custom1_lyricsCustomColor>\n";
        text += "  <custom1_lyricsPreChorusColor>" + FullscreenActivity.custom1_lyricsPreChorusColor + "</custom1_lyricsPreChorusColor>\n";
        text += "  <custom1_lyricsTagColor>" + FullscreenActivity.custom1_lyricsTagColor + "</custom1_lyricsTagColor>\n";
        text += "  <custom1_lyricsTextColor>" + FullscreenActivity.custom1_lyricsTextColor + "</custom1_lyricsTextColor>\n";
        text += "  <custom1_lyricsVerseColor>" + FullscreenActivity.custom1_lyricsVerseColor + "</custom1_lyricsVerseColor>\n";
        text += "  <custom1_metronome>" + FullscreenActivity.custom1_metronome + "</custom1_metronome>\n";
        text += "  <custom1_pagebuttons>" + FullscreenActivity.custom1_pagebuttons + "</custom1_pagebuttons>\n";
        text += "  <custom1_presoAlertFont>" + FullscreenActivity.custom1_presoAlertFont + "</custom1_presoAlertFont>\n";
        text += "  <custom1_presoFont>" + FullscreenActivity.custom1_presoFont + "</custom1_presoFont>\n";
        text += "  <custom1_presoInfoFont>" + FullscreenActivity.custom1_presoInfoFont + "</custom1_presoInfoFont>\n";
        text += "  <custom1_presoShadow>" + FullscreenActivity.custom1_presoShadow + "</custom1_presoShadow>\n";
        text += "  <custom2_lyricsBackgroundColor>" + FullscreenActivity.custom2_lyricsBackgroundColor + "</custom2_lyricsBackgroundColor>\n";
        text += "  <custom2_lyricsBridgeColor>" + FullscreenActivity.custom2_lyricsBridgeColor + "</custom2_lyricsBridgeColor>\n";
        text += "  <custom2_lyricsCapoColor>" + FullscreenActivity.custom2_lyricsCapoColor + "</custom2_lyricsCapoColor>\n";
        text += "  <custom2_lyricsChordsColor>" + FullscreenActivity.custom2_lyricsChordsColor + "</custom2_lyricsChordsColor>\n";
        text += "  <custom2_lyricsChorusColor>" + FullscreenActivity.custom2_lyricsChorusColor + "</custom2_lyricsChorusColor>\n";
        text += "  <custom2_lyricsCommentColor>" + FullscreenActivity.custom2_lyricsCommentColor + "</custom2_lyricsCommentColor>\n";
        text += "  <custom2_lyricsCustomColor>" + FullscreenActivity.custom2_lyricsCustomColor + "</custom2_lyricsCustomColor>\n";
        text += "  <custom2_lyricsPreChorusColor>" + FullscreenActivity.custom2_lyricsPreChorusColor + "</custom2_lyricsPreChorusColor>\n";
        text += "  <custom2_lyricsTagColor>" + FullscreenActivity.custom2_lyricsTagColor + "</custom2_lyricsTagColor>\n";
        text += "  <custom2_lyricsTextColor>" + FullscreenActivity.custom2_lyricsTextColor + "</custom2_lyricsTextColor>\n";
        text += "  <custom2_lyricsVerseColor>" + FullscreenActivity.custom2_lyricsVerseColor + "</custom2_lyricsVerseColor>\n";
        text += "  <custom2_metronome>" + FullscreenActivity.custom2_metronome + "</custom2_metronome>\n";
        text += "  <custom2_pagebuttons>" + FullscreenActivity.custom2_pagebuttons + "</custom2_pagebuttons>\n";
        text += "  <custom2_presoAlertFont>" + FullscreenActivity.custom2_presoAlertFont + "</custom2_presoAlertFont>\n";
        text += "  <custom2_presoFont>" + FullscreenActivity.custom2_presoFont + "</custom2_presoFont>\n";
        text += "  <custom2_presoInfoFont>" + FullscreenActivity.custom2_presoInfoFont + "</custom2_presoInfoFont>\n";
        text += "  <custom2_presoShadow>" + FullscreenActivity.custom2_presoShadow + "</custom2_presoShadow>\n";
        text += "  <customLogo>" + FullscreenActivity.customLogo + "</customLogo>\n";
        text += "  <customLogoSize>" + FullscreenActivity.customLogoSize + "</customLogoSize>\n";
        text += "  <customStorage>" + FullscreenActivity.customStorage + "</customStorage>\n";
        text += "  <dark_lyricsBackgroundColor>" + FullscreenActivity.dark_lyricsBackgroundColor + "</dark_lyricsBackgroundColor>\n";
        text += "  <dark_lyricsBridgeColor>" + FullscreenActivity.dark_lyricsBridgeColor + "</dark_lyricsBridgeColor>\n";
        text += "  <dark_lyricsCapoColor>" + FullscreenActivity.dark_lyricsCapoColor + "</dark_lyricsCapoColor>\n";
        text += "  <dark_lyricsChordsColor>" + FullscreenActivity.dark_lyricsChordsColor + "</dark_lyricsChordsColor>\n";
        text += "  <dark_lyricsChorusColor>" + FullscreenActivity.dark_lyricsChorusColor + "</dark_lyricsChorusColor>\n";
        text += "  <dark_lyricsCommentColor>" + FullscreenActivity.dark_lyricsCommentColor + "</dark_lyricsCommentColor>\n";
        text += "  <dark_lyricsCustomColor>" + FullscreenActivity.dark_lyricsCustomColor + "</dark_lyricsCustomColor>\n";
        text += "  <dark_lyricsPreChorusColor>" + FullscreenActivity.dark_lyricsPreChorusColor + "</dark_lyricsPreChorusColor>\n";
        text += "  <dark_lyricsTagColor>" + FullscreenActivity.dark_lyricsTagColor + "</dark_lyricsTagColor>\n";
        text += "  <dark_lyricsTextColor>" + FullscreenActivity.dark_lyricsTextColor + "</dark_lyricsTextColor>\n";
        text += "  <dark_lyricsVerseColor>" + FullscreenActivity.dark_lyricsVerseColor + "</dark_lyricsVerseColor>\n";
        text += "  <dark_metronome>" + FullscreenActivity.dark_metronome + "</dark_metronome>\n";
        text += "  <dark_pagebuttons>" + FullscreenActivity.dark_pagebuttons + "</dark_pagebuttons>\n";
        text += "  <dark_presoAlertFont>" + FullscreenActivity.dark_presoAlertFont + "</dark_presoAlertFont>\n";
        text += "  <dark_presoFont>" + FullscreenActivity.dark_presoFont + "</dark_presoFont>\n";
        text += "  <dark_presoInfoFont>" + FullscreenActivity.dark_presoInfoFont + "</dark_presoInfoFont>\n";
        text += "  <dark_presoShadow>" + FullscreenActivity.dark_presoShadow + "</dark_presoShadow>\n";
        text += "  <default_autoscroll_predelay>" + FullscreenActivity.default_autoscroll_predelay + "</default_autoscroll_predelay>\n";
        text += "  <default_autoscroll_songlength>" + FullscreenActivity.default_autoscroll_songlength + "</default_autoscroll_songlength>\n";
        text += "  <drawingEraserSize>" + FullscreenActivity.drawingEraserSize + "</drawingEraserSize>\n";
        text += "  <drawingHighlightColor>" + FullscreenActivity.drawingHighlightColor + "</drawingHighlightColor>\n";
        text += "  <drawingHighlightSize>" + FullscreenActivity.drawingHighlightSize + "</drawingHighlightSize>\n";
        text += "  <drawingPenColor>" + FullscreenActivity.drawingPenColor + "</drawingPenColor>\n";
        text += "  <drawingPenSize>" + FullscreenActivity.drawingPenSize + "</drawingPenSize>\n";
        text += "  <drawingTool>" + FullscreenActivity.drawingTool + "</drawingTool>\n";
        text += "  <exportOpenSongAppSet>" + FullscreenActivity.exportOpenSongAppSet + "</exportOpenSongAppSet>\n";
        text += "  <exportOpenSongApp>" + FullscreenActivity.exportOpenSongApp + "</exportOpenSongApp>\n";
        text += "  <exportDesktop>" + FullscreenActivity.exportDesktop + "</exportDesktop>\n";
        text += "  <exportText>" + FullscreenActivity.exportText + "</exportText>\n";
        text += "  <exportChordPro>" + FullscreenActivity.exportChordPro + "</exportChordPro>\n";
        text += "  <exportOnSong>" + FullscreenActivity.exportOnSong + "</exportOnSong>\n";
        text += "  <exportImage>" + FullscreenActivity.exportImage + "</exportImage>\n";
        text += "  <exportPDF>" + FullscreenActivity.exportPDF + "</exportPDF>\n";
        text += "  <fabSize>" + FullscreenActivity.fabSize + "</fabSize>\n";
        text += "  <gesture_doubletap>" + FullscreenActivity.gesture_doubletap + "</gesture_doubletap>\n";
        text += "  <gesture_longpress>" + FullscreenActivity.gesture_longpress + "</gesture_longpress>\n";
        text += "  <grouppagebuttons>" + FullscreenActivity.grouppagebuttons + "</grouppagebuttons>\n";
        text += "  <headingfontscalesize>" + FullscreenActivity.headingfontscalesize + "</headingfontscalesize>\n";
        text += "  <highlightShowSecs>" + FullscreenActivity.highlightShowSecs + "</highlightShowSecs>\n";
        text += "  <hideActionBar>" + FullscreenActivity.hideActionBar + "</hideActionBar>\n";
        text += "  <languageToLoad>" + FullscreenActivity.languageToLoad + "</languageToLoad>\n";
        text += "  <lastSetName>" + FullscreenActivity.lastSetName + "</lastSetName>\n";
        text += "  <light_lyricsBackgroundColor>" + FullscreenActivity.light_lyricsBackgroundColor + "</light_lyricsBackgroundColor>\n";
        text += "  <light_lyricsBridgeColor>" + FullscreenActivity.light_lyricsBridgeColor + "</light_lyricsBridgeColor>\n";
        text += "  <light_lyricsCapoColor>" + FullscreenActivity.light_lyricsCapoColor + "</light_lyricsCapoColor>\n";
        text += "  <light_lyricsChordsColor>" + FullscreenActivity.light_lyricsChordsColor + "</light_lyricsChordsColor>\n";
        text += "  <light_lyricsChorusColor>" + FullscreenActivity.light_lyricsChorusColor + "</light_lyricsChorusColor>\n";
        text += "  <light_lyricsCommentColor>" + FullscreenActivity.light_lyricsCommentColor + "</light_lyricsCommentColor>\n";
        text += "  <light_lyricsCustomColor>" + FullscreenActivity.light_lyricsCustomColor + "</light_lyricsCustomColor>\n";
        text += "  <light_lyricsPreChorusColor>" + FullscreenActivity.light_lyricsPreChorusColor + "</light_lyricsPreChorusColor>\n";
        text += "  <light_lyricsTagColor>" + FullscreenActivity.light_lyricsTagColor + "</light_lyricsTagColor>\n";
        text += "  <light_lyricsTextColor>" + FullscreenActivity.light_lyricsTextColor + "</light_lyricsTextColor>\n";
        text += "  <light_lyricsVerseColor>" + FullscreenActivity.light_lyricsVerseColor + "</light_lyricsVerseColor>\n";
        text += "  <light_metronome>" + FullscreenActivity.light_metronome + "</light_metronome>\n";
        text += "  <light_pagebuttons>" + FullscreenActivity.light_pagebuttons + "</light_pagebuttons>\n";
        text += "  <light_presoAlertFont>" + FullscreenActivity.light_presoAlertFont + "</light_presoAlertFont>\n";
        text += "  <light_presoFont>" + FullscreenActivity.light_presoFont + "</light_presoFont>\n";
        text += "  <light_presoInfoFont>" + FullscreenActivity.light_presoInfoFont + "</light_presoInfoFont>\n";
        text += "  <light_presoShadow>" + FullscreenActivity.light_presoShadow + "</light_presoShadow>\n";
        text += "  <linespacing>" + FullscreenActivity.linespacing + "</linespacing>\n";
        text += "  <locale>" + FullscreenActivity.locale + "</locale>\n";
        text += "  <longpressdownpedalgesture>" + FullscreenActivity.longpressdownpedalgesture + "</longpressdownpedalgesture>\n";
        text += "  <longpressnextpedalgesture>" + FullscreenActivity.longpressnextpedalgesture + "</longpressnextpedalgesture>\n";
        text += "  <longpresspreviouspedalgesture>" + FullscreenActivity.longpresspreviouspedalgesture + "</longpresspreviouspedalgesture>\n";
        text += "  <longpressuppedalgesture>" + FullscreenActivity.longpressuppedalgesture + "</longpressuppedalgesture>\n";
        text += "  <maxvolrange>" + FullscreenActivity.maxvolrange + "</maxvolrange>\n";
        text += "  <mediaStore>" + FullscreenActivity.mediaStore + "</mediaStore>\n";
        text += "  <menuSize>" + FullscreenActivity.menuSize + "</menuSize>\n";
        text += "  <metronomepan>" + FullscreenActivity.metronomepan + "</metronomepan>\n";
        text += "  <metronomevol>" + FullscreenActivity.metronomevol + "</metronomevol>\n";
        text += "  <mAuthor>" + FullscreenActivity.mAuthor + "</mAuthor>\n";
        text += "  <mCopyright>" + FullscreenActivity.mCopyright + "</mCopyright>\n";
        text += "  <mDisplayTheme>" + FullscreenActivity.mDisplayTheme + "</mDisplayTheme>\n";
        text += "  <mFontSize>" + FullscreenActivity.mFontSize + "</mFontSize>\n";
        text += "  <mMaxFontSize>" + FullscreenActivity.mMaxFontSize + "</mMaxFontSize>\n";
        text += "  <mMinFontSize>" + FullscreenActivity.mMinFontSize + "</mMinFontSize>\n";
        text += "  <mStorage>" + FullscreenActivity.mTitle + "</mStorage>\n";
        text += "  <mTitle>" + FullscreenActivity.mTitle + "</mTitle>\n";
        text += "  <myAlert>" + FullscreenActivity.myAlert + "</myAlert>\n";
        text += "  <mychordsfontnum>" + FullscreenActivity.mychordsfontnum + "</mychordsfontnum>\n";
        text += "  <mylyricsfontnum>" + FullscreenActivity.mylyricsfontnum + "</mylyricsfontnum>\n";
        text += "  <mypresofontnum>" + FullscreenActivity.mylyricsfontnum + "</mypresofontnum>\n";
        text += "  <mypresoinfofontnum>" + FullscreenActivity.mypresoinfofontnum + "</mypresoinfofontnum>\n";
        text += "  <mySet>" + FullscreenActivity.mySet + "</mySet>\n";
        text += "  <override_fullscale>" + FullscreenActivity.override_fullscale + "</override_fullscale>\n";
        text += "  <override_widthscale>" + FullscreenActivity.override_widthscale + "</override_widthscale>\n";
        text += "  <padpan>" + FullscreenActivity.padpan + "</padpan>\n";
        text += "  <padvol>" + FullscreenActivity.padvol + "</padvol>\n";
        text += "  <page_autoscroll_visible>" + FullscreenActivity.page_autoscroll_visible + "</page_autoscroll_visible>\n";
        text += "  <page_chord_visible>" + FullscreenActivity.page_custom_visible + "</page_chord_visible>\n";
        text += "  <page_custom_grouped>" + FullscreenActivity.page_custom_grouped + "</page_custom_grouped>\n";
        text += "  <page_custom_visible>" + FullscreenActivity.page_custom_visible + "</page_custom_visible>\n";
        text += "  <page_custom1_visible>" + FullscreenActivity.page_custom1_visible + "</page_custom1_visible>\n";
        text += "  <page_custom2_visible>" + FullscreenActivity.page_custom2_visible + "</page_custom2_visible>\n";
        text += "  <page_custom3_visible>" + FullscreenActivity.page_custom3_visible + "</page_custom3_visible>\n";
        text += "  <page_custom4_visible>" + FullscreenActivity.page_custom4_visible + "</page_custom4_visible>\n";
        text += "  <page_extra_grouped>" + FullscreenActivity.page_extra_grouped + "</page_extra_grouped>\n";
        text += "  <page_extra_visible>" + FullscreenActivity.page_extra_visible + "</page_extra_visible>\n";
        text += "  <page_highlight_visible>" + FullscreenActivity.page_highlight_visible + "</page_highlight_visible>\n";
        text += "  <page_links_visible>" + FullscreenActivity.page_links_visible + "</page_links_visible>\n";
        text += "  <page_metronome_visible>" + FullscreenActivity.page_metronome_visible + "</page_metronome_visible>\n";
        text += "  <page_notation_visible>" + FullscreenActivity.page_notation_visible + "</page_notation_visible>\n";
        text += "  <page_pad_visible>" + FullscreenActivity.page_pad_visible + "</page_pad_visible>\n";
        text += "  <page_pages_visible>" + FullscreenActivity.page_pages_visible + "</page_pages_visible>\n";
        text += "  <page_set_visible>" + FullscreenActivity.page_set_visible + "</page_set_visible>\n";
        text += "  <page_sticky_visible>" + FullscreenActivity.page_sticky_visible + "</page_sticky_visible>\n";
        text += "  <pagebutton_position>" + FullscreenActivity.pagebutton_position + "</pagebutton_position>\n";
        text += "  <pagebutton_scale>" + FullscreenActivity.pagebutton_scale + "</pagebutton_scale>\n";
        text += "  <pageturner_AUTOSCROLL>" + FullscreenActivity.pageturner_AUTOSCROLL + "</pageturner_AUTOSCROLL>\n";
        text += "  <pageturner_AUTOSCROLLPAD>" + FullscreenActivity.pageturner_AUTOSCROLLPAD + "</pageturner_AUTOSCROLLPAD>\n";
        text += "  <pageturner_AUTOSCROLLMETRONOME>" + FullscreenActivity.pageturner_AUTOSCROLLMETRONOME + "</pageturner_AUTOSCROLLMETRONOME>\n";
        text += "  <pageturner_AUTOSCROLLPADMETRONOME>" + FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME + "</pageturner_AUTOSCROLLPADMETRONOME>\n";
        text += "  <pageturner_DOWN>" + FullscreenActivity.pageturner_DOWN + "</pageturner_DOWN>\n";
        text += "  <pageturner_METRONOME>" + FullscreenActivity.pageturner_METRONOME + "</pageturner_METRONOME>\n";
        text += "  <pageturner_NEXT>" + FullscreenActivity.pageturner_NEXT + "</pageturner_NEXT>\n";
        text += "  <pageturner_PAD>" + FullscreenActivity.pageturner_PAD + "</pageturner_PAD>\n";
        text += "  <pageturner_PADMETRONOME>" + FullscreenActivity.pageturner_PADMETRONOME + "</pageturner_PADMETRONOME>\n";
        text += "  <pageturner_PREVIOUS>" + FullscreenActivity.pageturner_PREVIOUS + "</pageturner_PREVIOUS>\n";
        text += "  <pageturner_UP>" + FullscreenActivity.pageturner_UP + "</pageturner_UP>\n";
        text += "  <pageButtonAlpha>" + FullscreenActivity.pageButtonAlpha + "</pageButtonAlpha>\n";
        text += "  <popupAlpha_Set>" + FullscreenActivity.popupAlpha_Set + "</popupAlpha_Set>\n";
        text += "  <popupDim_All>" + FullscreenActivity.popupDim_All + "</popupDim_All>\n";
        text += "  <popupDim_Set>" + FullscreenActivity.popupDim_Set + "</popupDim_Set>\n";
        text += "  <popupPosition_All>" + FullscreenActivity.popupPosition_All + "</popupPosition_All>\n";
        text += "  <popupPosition_Set>" + FullscreenActivity.popupPosition_Set + "</popupPosition_Set>\n";
        text += "  <popupScale_All>" + FullscreenActivity.popupScale_All + "</popupScale_All>\n";
        text += "  <popupScale_Set>" + FullscreenActivity.popupScale_Set + "</popupScale_Set>\n";
        text += "  <prefChord_Aflat_Gsharp>" + FullscreenActivity.prefChord_Aflat_Gsharp + "</prefChord_Aflat_Gsharp>\n";
        text += "  <prefChord_Bflat_Asharp>" + FullscreenActivity.prefChord_Bflat_Asharp + "</prefChord_Bflat_Asharp>\n";
        text += "  <prefChord_Dflat_Csharp>" + FullscreenActivity.prefChord_Dflat_Csharp + "</prefChord_Dflat_Csharp>\n";
        text += "  <prefChord_Eflat_Dsharp>" + FullscreenActivity.prefChord_Eflat_Dsharp + "</prefChord_Eflat_Dsharp>\n";
        text += "  <prefChord_Gflat_Fsharp>" + FullscreenActivity.prefChord_Gflat_Fsharp + "</prefChord_Gflat_Fsharp>\n";
        text += "  <prefChord_Aflatm_Gsharpm>" + FullscreenActivity.prefChord_Aflatm_Gsharpm + "</prefChord_Aflatm_Gsharpm>\n";
        text += "  <prefChord_Bflatm_Asharpm>" + FullscreenActivity.prefChord_Bflatm_Asharpm + "</prefChord_Bflatm_Asharpm>\n";
        text += "  <prefChord_Dflatm_Csharpm>" + FullscreenActivity.prefChord_Dflatm_Csharpm + "</prefChord_Dflatm_Csharpm>\n";
        text += "  <prefChord_Eflatm_Dsharpm>" + FullscreenActivity.prefChord_Eflatm_Dsharpm + "</prefChord_Eflatm_Dsharpm>\n";
        text += "  <prefChord_Gflatm_Fsharpm>" + FullscreenActivity.prefChord_Gflatm_Fsharpm + "</prefChord_Gflatm_Fsharpm>\n";
        text += "  <prefStorage>" + FullscreenActivity.prefStorage + "</prefStorage>\n";
        text += "  <presenterChords>" + FullscreenActivity.presenterChords + "</presenterChords>\n";
        text += "  <presoAlertSize>" + FullscreenActivity.presoAlertSize + "</presoAlertSize>\n";
        text += "  <presoAlpha>" + FullscreenActivity.presoAlpha + "</presoAlpha>\n";
        text += "  <presoAuthorSize>" + FullscreenActivity.presoAuthorSize + "</presoAuthorSize>\n";
        text += "  <presoAutoScale>" + FullscreenActivity.presoAutoScale + "</presoAutoScale>\n";
        text += "  <presoFontSize>" + FullscreenActivity.presoFontSize + "</presoFontSize>\n";
        text += "  <presoCopyrightSize>" + FullscreenActivity.presoCopyrightSize + "</presoCopyrightSize>\n";
        text += "  <presoInfoAlign>" + FullscreenActivity.presoInfoAlign + "</presoInfoAlign>\n";
        text += "  <presoLyricsAlign>" + FullscreenActivity.presoLyricsAlign + "</presoLyricsAlign>\n";
        text += "  <presoMaxFontSize>" + FullscreenActivity.presoMaxFontSize + "</presoMaxFontSize>\n";
        text += "  <presoShowChords>" + FullscreenActivity.presoShowChords + "</presoShowChords>\n";
        text += "  <presoTitleSize>" + FullscreenActivity.presoTitleSize + "</presoTitleSize>\n";
        text += "  <presoTransitionTime>" + FullscreenActivity.presoTransitionTime + "</presoTransitionTime>\n";
        text += "  <profile>" + FullscreenActivity.profile + "</profile>\n";
        text += "  <quickLaunchButton_1>" + FullscreenActivity.quickLaunchButton_1 + "</quickLaunchButton_1>\n";
        text += "  <quickLaunchButton_2>" + FullscreenActivity.quickLaunchButton_2 + "</quickLaunchButton_2>\n";
        text += "  <quickLaunchButton_3>" + FullscreenActivity.quickLaunchButton_3 + "</quickLaunchButton_3>\n";
        text += "  <quickLaunchButton_4>" + FullscreenActivity.quickLaunchButton_4 + "</quickLaunchButton_4>\n";
        text += "  <scrollDistance>" + FullscreenActivity.scrollDistance + "</scrollDistance>\n";
        text += "  <scrollSpeed>" + FullscreenActivity.scrollSpeed + "</scrollSpeed>\n";
        text += "  <showAlphabeticalIndexInSongMenu>" + FullscreenActivity.showAlphabeticalIndexInSongMenu + "</showAlphabeticalIndexInSongMenu>\n";
        text += "  <showCapoChords>" + FullscreenActivity.showCapoChords + "</showCapoChords>\n";
        text += "  <showChords>" + FullscreenActivity.showChords + "</showChords>\n";
        text += "  <showNativeAndCapoChords>" + FullscreenActivity.showNativeAndCapoChords + "</showNativeAndCapoChords>\n";
        text += "  <showNextInSet>" + FullscreenActivity.showNextInSet + "</showNextInSet>\n";
        text += "  <showLyrics>" + FullscreenActivity.showLyrics + "</showLyrics>\n";
        text += "  <showSetTickBoxInSongMenu>" + FullscreenActivity.showSetTickBoxInSongMenu + "</showSetTickBoxInSongMenu>\n";
        text += "  <songfilename>" + FullscreenActivity.songfilename + "</songfilename>\n";
        text += "  <stagemodeScale>" + FullscreenActivity.stagemodeScale + "</stagemodeScale>\n";
        text += "  <stickyNotesShowSecs>" + FullscreenActivity.stickyNotesShowSecs + "</stickyNotesShowSecs>\n";
        text += "  <stickyOpacity>" + FullscreenActivity.stickyOpacity + "</stickyOpacity>\n";
        text += "  <stickyTextSize>" + FullscreenActivity.stickyTextSize + "</stickyTextSize>\n";
        text += "  <stickyWidth>" + FullscreenActivity.stickyWidth + "</stickyWidth>\n";
        text += "  <SWIPE_MAX_OFF_PATH>" + FullscreenActivity.SWIPE_MAX_OFF_PATH + "</SWIPE_MAX_OFF_PATH>\n";
        text += "  <SWIPE_MIN_DISTANCE>" + FullscreenActivity.SWIPE_MIN_DISTANCE + "</SWIPE_MIN_DISTANCE>\n";
        text += "  <SWIPE_THRESHOLD_VELOCITY>" + FullscreenActivity.SWIPE_THRESHOLD_VELOCITY + "</SWIPE_THRESHOLD_VELOCITY>\n";
        text += "  <swipeDrawer>" + FullscreenActivity.swipeDrawer + "</swipeDrawer>\n";
        text += "  <swipeForMenus>" + FullscreenActivity.swipeForSongs + "</swipeForMenus>\n";
        text += "  <swipeForSongs>" + FullscreenActivity.swipeForSongs + "</swipeForSongs>\n";
        text += "  <swipeSet>" + FullscreenActivity.swipeSet + "</swipeSet>\n";
        text += "  <timerFontSizeAutoScroll>" + FullscreenActivity.timerFontSizeAutoScroll + "</timerFontSizeAutoScroll>\n";
        text += "  <timerFontSizePad>" + FullscreenActivity.timerFontSizePad + "</timerFontSizePad>\n";
        text += "  <timeFormat24h>" + FullscreenActivity.timeFormat24h + "</timeFormat24h>\n";
        text += "  <timeOn>" + FullscreenActivity.timeOn + "</timeOn>\n";
        text += "  <timeSize>" + FullscreenActivity.timeSize + "</timeSize>\n";
        text += "  <toggleAutoHighlight>" + FullscreenActivity.toggleAutoHighlight + "</toggleAutoHighlight>\n";
        text += "  <toggleAutoSticky>" + FullscreenActivity.toggleAutoSticky + "</toggleAutoSticky>\n";
        text += "  <togglePageButtons>" + FullscreenActivity.togglePageButtons + "</togglePageButtons>\n";
        text += "  <toggleScrollArrows>" + FullscreenActivity.toggleScrollArrows + "</toggleScrollArrows>\n";
        text += "  <toggleScrollBeforeSwipe>" + FullscreenActivity.toggleScrollBeforeSwipe + "</toggleScrollBeforeSwipe>\n";
        text += "  <toggleYScale>" + FullscreenActivity.toggleYScale + "</toggleYScale>\n";
        text += "  <transposeStyle>" + FullscreenActivity.transposeStyle + "</transposeStyle>\n";
        text += "  <trimSections>" + FullscreenActivity.trimSections + "</trimSections>\n";
        text += "  <trimSectionSpace>" + FullscreenActivity.trimSectionSpace + "</trimSectionSpace>\n";
        text += "  <usePresentationOrder>" + FullscreenActivity.usePresentationOrder + "</usePresentationOrder>\n";
        text += "  <visualmetronome>" + FullscreenActivity.visualmetronome + "</visualmetronome>\n";
        //text += "  <whichMode>" + FullscreenActivity.whichMode + "</whichMode>\n";
        text += "  <whichSetCategory>" + FullscreenActivity.whichSetCategory + "</whichSetCategory>\n";
        text += "  <whichSongFolder>" + FullscreenActivity.whichSongFolder + "</whichSongFolder>\n";
        text += "  <xmargin_presentation>" + FullscreenActivity.xmargin_presentation + "</xmargin_presentation>\n";
        text += "  <ymargin_presentation>" + FullscreenActivity.visualmetronome + "</ymargin_presentation>\n";
        text += "</myprofile>";
        return text;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}