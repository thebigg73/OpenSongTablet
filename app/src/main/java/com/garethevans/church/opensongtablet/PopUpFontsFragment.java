package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PopUpFontsFragment extends DialogFragment {

    static PopUpFontsFragment newInstance() {
        PopUpFontsFragment frag;
        frag = new PopUpFontsFragment();
        return frag;
    }

    private Spinner lyricsFontSpinner, chordsFontSpinner, stickyFontSpinner, presoFontSpinner, presoInfoFontSpinner;

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private TextView scaleHeading_TextView, scaleComment_TextView, scaleChords_TextView, lineSpacing_TextView,
            lyricPreviewTextView, chordPreviewTextView, stickyPreviewTextView, presoPreviewTextView,
            presoinfoPreviewTextView, fontBrowse;
    SwitchCompat boldChordsHeadings, trimlinespacing_SwitchCompat, hideBox_SwitchCompat,
            trimSections_SwitchCompat, addSectionSpace_SwitchCompat;
    private ArrayAdapter<String> choose_fonts;
    private SeekBar scaleHeading_SeekBar, scaleComment_SeekBar, scaleChords_SeekBar, lineSpacing_SeekBar;
    private SetTypeFace setTypeFace;
    private Preferences preferences;
    private StorageAccess storageAccess;

    // Handlers for fonts
    private Handler lyrichandler, chordhandler, stickyhandler, presohandler, presoinfohandler, customhandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_font, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.choose_fonts));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.hide();
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getContext());
            saveMe.setEnabled(false);
            doSave();
        });

        // Initialise the helper classes
        initialseHelpers();

        // Initialise the font handlers
        initialiseFontHandlers();

        // Initialise the views
        initialiseViews(V);

        // Set out preferences
        setPreferences();

        // Initialise default listeners
        initialiseBasicListeners();

        // Decide if we have access to the font chooser
        if (hasPlayServices()) {
            // Set up the typefaces
            setUpFonts();

        } else {
            // Hide the font chooser and show the warning/info
            disableFonts(V);
        }

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    private void initialseHelpers() {
        preferences = new Preferences();
        setTypeFace = new SetTypeFace();
        storageAccess = new StorageAccess();
    }

    private void initialiseFontHandlers() {
        lyrichandler = new Handler();
        chordhandler = new Handler();
        stickyhandler = new Handler();
        presohandler = new Handler();
        presoinfohandler = new Handler();
        customhandler = new Handler();
    }

    private void initialiseViews(View V) {
        lyricsFontSpinner = V.findViewById(R.id.lyricsFontSpinner);
        chordsFontSpinner = V.findViewById(R.id.chordsFontSpinner);
        stickyFontSpinner = V.findViewById(R.id.stickyFontSpinner);
        presoFontSpinner = V.findViewById(R.id.presoFontSpinner);
        presoInfoFontSpinner = V.findViewById(R.id.presoInfoFontSpinner);
        lyricPreviewTextView = V.findViewById(R.id.lyricPreviewTextView);
        chordPreviewTextView = V.findViewById(R.id.chordPreviewTextView);
        stickyPreviewTextView = V.findViewById(R.id.stickyPreviewTextView);
        presoPreviewTextView = V.findViewById(R.id.presoPreviewTextView);
        presoinfoPreviewTextView = V.findViewById(R.id.presoinfoPreviewTextView);
        fontBrowse = V.findViewById(R.id.fontBrowse);
        scaleChords_TextView = V.findViewById(R.id.scaleChords_TextView);
        scaleChords_SeekBar = V.findViewById(R.id.scaleChords_SeekBar);
        scaleComment_TextView = V.findViewById(R.id.scaleComment_TextView);
        scaleComment_SeekBar = V.findViewById(R.id.scaleComment_SeekBar);
        scaleHeading_TextView = V.findViewById(R.id.scaleHeading_TextView);
        scaleHeading_SeekBar = V.findViewById(R.id.scaleHeading_SeekBar);
        boldChordsHeadings = V.findViewById(R.id.boldChordsHeadings);
        lineSpacing_TextView = V.findViewById(R.id.lineSpacing_TextView);
        lineSpacing_SeekBar = V.findViewById(R.id.lineSpacing_SeekBar);
        trimlinespacing_SwitchCompat = V.findViewById(R.id.trimlinespacing_SwitchCompat);
        hideBox_SwitchCompat = V.findViewById(R.id.hideBox_SwitchCompat);
        trimSections_SwitchCompat = V.findViewById(R.id.trimSections_SwitchCompat);
        addSectionSpace_SwitchCompat = V.findViewById(R.id.addSectionSpace_SwitchCompat);
    }

    private void setPreferences() {
        boldChordsHeadings.setChecked(preferences.getMyPreferenceBoolean(getContext(), "displayBoldChordsHeadings", false));
        trimSections_SwitchCompat.setChecked(preferences.getMyPreferenceBoolean(getContext(),"trimSections",true));
        hideBox_SwitchCompat.setChecked(preferences.getMyPreferenceBoolean(getContext(),"hideLyricsBox",false));
        addSectionSpace_SwitchCompat.setChecked(preferences.getMyPreferenceBoolean(getContext(),"addSectionSpace",true));
        trimlinespacing_SwitchCompat.setChecked(preferences.getMyPreferenceBoolean(getContext(),"trimLines",false));
        lineSpacing_SeekBar.setEnabled(preferences.getMyPreferenceBoolean(getContext(),"trimLines",false));
    }

    private boolean hasPlayServices() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext()) == ConnectionResult.SUCCESS;
    }

    private void initialiseBasicListeners() {
        fontBrowse.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://fonts.google.com"));
            startActivity(intent);
        });

        // Listen for seekbar changes
        scaleHeading_SeekBar.setMax(200);
        int progress = (int) (preferences.getMyPreferenceFloat(getContext(),"scaleHeadings", 0.6f) * 100);
        scaleHeading_SeekBar.setProgress(progress);
        String text = progress + "%";
        scaleHeading_TextView.setText(text);
        scaleHeading_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleHeading_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        scaleChords_SeekBar.setMax(200);
        progress = (int) (preferences.getMyPreferenceFloat(getContext(),"scaleChords",1.0f) * 100);
        scaleChords_SeekBar.setProgress(progress);
        text = progress + "%";
        scaleChords_TextView.setText(text);
        scaleChords_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleChords_TextView.setText(text);
                //float newsize = 12 * ((float) progress/100.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        scaleComment_SeekBar.setMax(200);
        progress = (int) (preferences.getMyPreferenceFloat(getContext(),"scaleComments", 0.8f) * 100);
        scaleComment_SeekBar.setProgress(progress);
        text = progress + "%";
        scaleComment_TextView.setText(text);
        scaleComment_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleComment_TextView.setText(text);
                //float newsize = 12 * ((float) progress/100.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        lineSpacing_SeekBar.setMax(100);
        progress = (int) (preferences.getMyPreferenceFloat(getContext(),"lineSpacing",0.1f) * 100);
        lineSpacing_SeekBar.setProgress(progress);
        text = progress + "%";
        lineSpacing_TextView.setText(text);
        lineSpacing_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                lineSpacing_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        trimlinespacing_SwitchCompat.setOnCheckedChangeListener((buttonView, b) -> {
            // Disable the linespacing seekbar if required
            lineSpacing_SeekBar.setEnabled(b);
            preferences.setMyPreferenceBoolean(getContext(),"trimLines",b);
        });

        boldChordsHeadings.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(), "displayBoldChordsHeadings", b));
        hideBox_SwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"hideLyricsBox",b));
        trimSections_SwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"trimSections",b));
        addSectionSpace_SwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            // Historic button name - actually asks if space should be added
            preferences.setMyPreferenceBoolean(getContext(),"addSectionSpace",b);
        });

        // If we are running kitkat, hide the trim options
        if (!storageAccess.lollipopOrLater()) {
            lineSpacing_SeekBar.setVisibility(View.GONE);
            lineSpacing_TextView.setVisibility(View.GONE);
            trimlinespacing_SwitchCompat.setVisibility(View.GONE);
        }
    }

    private void setUpFonts() {
        setTypeFace.setUpAppFonts(getContext(), preferences, lyrichandler, chordhandler, stickyhandler,
                presohandler, presoinfohandler, customhandler);
        // Try to get a list of fonts from Google
        GetFontList getFontList = new GetFontList();
        getFontList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void disableFonts(View V) {
        V.findViewById(R.id.play_services_error).setVisibility(View.VISIBLE);
        V.findViewById(R.id.fontBrowse).setVisibility(View.GONE);
        V.findViewById(R.id.choose_fonts1).setVisibility(View.GONE);
        V.findViewById(R.id.choose_fonts2).setVisibility(View.GONE);
        V.findViewById(R.id.choose_fonts3).setVisibility(View.GONE);
        V.findViewById(R.id.choose_fonts4).setVisibility(View.GONE);
        V.findViewById(R.id.choose_fonts5).setVisibility(View.GONE);
        V.findViewById(R.id.play_services_how).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_services_help)));
            startActivity(i);
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    private void setSpinners() {
        lyricsFontSpinner.setAdapter(choose_fonts);
        chordsFontSpinner.setAdapter(choose_fonts);
        stickyFontSpinner.setAdapter(choose_fonts);
        presoFontSpinner.setAdapter(choose_fonts);
        presoInfoFontSpinner.setAdapter(choose_fonts);

        // Select the appropriate items in the list
        lyricsFontSpinner.setSelection(getPositionInList("fontLyric"));
        chordsFontSpinner.setSelection(getPositionInList("fontChord"));
        stickyFontSpinner.setSelection(getPositionInList("fontSticky"));
        presoFontSpinner.setSelection(getPositionInList("fontPreso"));
        presoInfoFontSpinner.setSelection(getPositionInList("fontPresoInfo"));

        lyricsFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontLyric", "lyric", lyricPreviewTextView, lyrichandler);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        chordsFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontChord", "chord", chordPreviewTextView, chordhandler);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        stickyFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontSticky", "sticky", stickyPreviewTextView, stickyhandler);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        presoFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontPreso", "preso", presoPreviewTextView, presohandler);
                if (mListener!=null) {
                    mListener.refreshSecondaryDisplay("all");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        presoInfoFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateItem(position, "fontPresoInfo", "presoinfo", presoinfoPreviewTextView, presoinfohandler);
                if (mListener!=null) {
                    mListener.refreshSecondaryDisplay("all");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }
    private void updateItem(int position, String prefname, String what, TextView textView, Handler handler) {
        String fontchosen = choose_fonts.getItem(position);
        preferences.setMyPreferenceString(getContext(), prefname, fontchosen);
        setTypeFace.setChosenFont(getContext(), preferences, fontchosen, what,
                textView, handler);
    }

    private int getPositionInList(String what) {
        String valToFind = preferences.getMyPreferenceString(getContext(), what, "lato");
        try {
            return choose_fonts.getPosition(valToFind);
        } catch (Exception e) {
            return -1;
        }
    }

    private void doSave() {
        try {
            float num = (float) scaleHeading_SeekBar.getProgress() / 100.0f;
            preferences.setMyPreferenceFloat(getContext(), "scaleHeadings", num);
            num = (float) scaleComment_SeekBar.getProgress() / 100.0f;
            preferences.setMyPreferenceFloat(getContext(), "scaleComments", num);
            num = (float) scaleChords_SeekBar.getProgress() / 100.0f;
            preferences.setMyPreferenceFloat(getContext(), "scaleChords", num);
            num = (float) lineSpacing_SeekBar.getProgress() / 100.0f;
            preferences.setMyPreferenceFloat(getContext(), "lineSpacing", num);
            mListener.refreshAll();
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface MyInterface {
        void refreshAll();
        void refreshSecondaryDisplay(String which);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFontList extends AsyncTask<Object,String,String> {

        @Override
        protected String doInBackground(Object... objects) {
            // IV - Give the spinners an empty line to avoid them starting at reduced height
            ArrayList<String> fontnames = new ArrayList<>();
            fontnames.add("");
            choose_fonts = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, fontnames);
            lyricsFontSpinner.setAdapter(choose_fonts);
            chordsFontSpinner.setAdapter(choose_fonts);
            stickyFontSpinner.setAdapter(choose_fonts);
            presoFontSpinner.setAdapter(choose_fonts);
            presoInfoFontSpinner.setAdapter(choose_fonts);
            try {
                URL url = new URL("https://www.googleapis.com/webfonts/v1/webfonts?key=AIzaSyBKvCB1NnWwXGyGA7RTar0VQFCM3rdOE8k&sort=alpha");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            ArrayList<String> fontnames = new ArrayList<>();

            if (response == null) {
                // Set up the custom fonts - use my preferred Google font lists as local files no longer work!!!
                ArrayList<String> customfontsavail = setTypeFace.googleFontsAllowed();
                try {
                    choose_fonts = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, customfontsavail);
                    choose_fonts.setDropDownViewResource(R.layout.my_spinner);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                // Split the returned JSON into lines
                String[] lines = response.split("\n");

                for (String line : lines) {
                    if (line.contains("\"family\":")) {
                        line = line.replace("\"family\"", "");
                        line = line.replace(":", "");
                        line = line.replace("\"", "");
                        line = line.replace(",", "");
                        line = line.trim();

                        // Fonts that don't work (there are hundred that do, so don't include the ones that don't)
                        String notworking = "Aleo Angkor Asap_Condensed B612 B612_Mono Bai_Jamjuree " +
                                "Barlow_Condensed Barlow_Semi_Condensed Barricecito Battambang " +
                                "Bayon Beth_Ellen BioRhyme_Expanded Blinker Bokor Buda Cabin_Condensed " +
                                "Calligraffitti Chakre_Petch Charm Charmonman Chenla Coda_Caption " +
                                "Content Crimson_Pro DM_Sans DM_Serif_Display DM_Serif_Text Dangrek " +
                                "Darker_Grotesque Encode_Sans_Condensed Encode_Sans_Expanded " +
                                "Encode_Sans_Semi_Condensed Encode_Sans_Semi_Expanded Fahkwang " +
                                "Farro Fasthand Fira_Code Freehand Grenze Hanuman IBM_Plex_Sans_Condensed " +
                                "K2D Khmer KoHo Kodchasan Kosugi Kosugi_Maru Koulen Krub Lacquer " +
                                "Libre_Barcode_128 Libre_Barcode_128_Text Libre_Barcode_39 " +
                                "Libre_Barcode_39_Extended Libre_Barcode_39_Extended_Text Libre_Barcode_39_Text " +
                                "Libre_Caslon_Display Libre_Caslon_Text Literata Liu_Jian_Mao_Cao " +
                                "Long_Cang M_PLUS_1p M_PLUS_Rounded_1c Ma_Shan_Zheng Major_Mono_Display " +
                                "Mali Markazi_Text Metal Molle Moul Moulpali Niramit Nokora Notable " +
                                "Noto_Sans_HK Noto_Sans_JP Noto_Sans_KR Noto_Sans_SC Noto_Sans_TC " +
                                "Noto_Serif_JP Noto_Serif_KR Noto_Serif_SC Noto_Serif_TC Open_Sans_Condensed " +
                                "Orbitron Preahvihear Red_Hat_Display Red_Hat_Text Roboto_Condensed " +
                                "Saira_Condensed Saira_Extra_Condensed Saira_Semi_Condensed Saira_Stencil_One " +
                                "Sarabun Sawarabi_Gothic Sawarabi_Mincho Siemreap Single_Day Srisakdi " +
                                "Staatliches Sunflower Suwannaphum Taprom Thasadith Ubuntu_Condensed " +
                                "UnifrakturCook ZCOOL_KuaiLe ZCOOL_QingKe_HuangYou ZCOOL_XiaoWei Zhi_Mhang_Xing ";

                        if (!notworking.contains(line.trim().replace(" ","_")+" ")) {
                            fontnames.add(line);
                        }
                    }
                }
                // Set up the custom fonts - use my preferred Google font lists as local files no longer work!!!
                choose_fonts = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, fontnames);
                choose_fonts.setDropDownViewResource(R.layout.my_spinner);
                choose_fonts.notifyDataSetChanged();
            }

            // Set the dropdown lists
            setSpinners();
        }
    }
}