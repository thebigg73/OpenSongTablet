package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PopUpBibleXMLFragment extends DialogFragment {

    static PopUpBibleXMLFragment newInstance() {
        PopUpBibleXMLFragment frag;
        frag = new PopUpBibleXMLFragment();
        return frag;
    }

    public interface MyInterface {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    ArrayList<String> bibleFileNames, bibleBookNames, bibleChapters, quickUpdate;
    public static ArrayList<String> bibleVerses, bibleText;
    ArrayAdapter<String> blank_array;
    Spinner bibleFileSpinner, bibleBookSpinner, bibleChapterSpinner, bibleVerseFromSpinner, bibleVerseToSpinner;
    ProgressBar progressBar;
    TextView previewTextView;
    String bible;
    public static boolean includeVersNums = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_biblexml, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.bibleXML));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe, getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the basic views
        bibleFileSpinner = V.findViewById(R.id.bibleFileSpinner);
        bibleBookSpinner = V.findViewById(R.id.bibleBookSpinner);
        bibleChapterSpinner = V.findViewById(R.id.bibleChapterSpinner);
        bibleVerseFromSpinner = V.findViewById(R.id.bibleVerseFromSpinner);
        bibleVerseToSpinner = V.findViewById(R.id.bibleVerseToSpinner);
        previewTextView = V.findViewById(R.id.previewTextView);
        progressBar = V.findViewById(R.id.progressBar);
        SwitchCompat includeVersNumsSwitch = V.findViewById(R.id.includeVersNumsSwitch);
        includeVersNumsSwitch.setChecked(includeVersNums);
        includeVersNumsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                includeVersNums = b;
                // Try to update the preview
                if (quickUpdate!=null) {
                    try {
                        Bible.getVersesForChapter(new File(quickUpdate.get(0)), quickUpdate.get(1), quickUpdate.get(2));
                        getBibleText(new File(quickUpdate.get(0)), quickUpdate.get(1), quickUpdate.get(2),
                                quickUpdate.get(3), quickUpdate.get(4));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Prepare an empty array
        ArrayList<String> emptyArray = new ArrayList<>();
        emptyArray.add("");
        blank_array = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,emptyArray);

        // Update the bible file spinner and select the appropriate one
        updateBibleFiles();

        quickUpdate = null;
        return V;
    }

    public void updateBibleFiles() {
        // This looks for bible files inside the OpenSong/OpenSong Scripture/ folder
        try {
            UpdateBibleFiles update_biblefiles = new UpdateBibleFiles();
            update_biblefiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class UpdateBibleFiles extends AsyncTask<Object,Void,String> {
        @Override
        protected void onPreExecute() {
            // Initialise the other spinners
            progressBar.setVisibility(View.VISIBLE);
            initialiseTheSpinners(bibleFileSpinner);
            initialiseTheSpinners(bibleBookSpinner);
            initialiseTheSpinners(bibleChapterSpinner);
            initialiseTheSpinners(bibleVerseFromSpinner);
            initialiseTheSpinners(bibleVerseToSpinner);
            previewTextView.setText("");
        }

        @Override
        protected String doInBackground(Object... objects) {
            bibleFileNames = new ArrayList<>();
            bibleFileNames.add("");
            File[] files = FullscreenActivity.dirbibles.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    bibleFileNames.add(f.getName());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            // Set up the spinner
            ArrayAdapter<String> aa = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, bibleFileNames);
            bibleFileSpinner.setAdapter(aa);
            bibleFileSpinner.setEnabled(true);

            // Set up the listener for the bible file spinner
            bibleFileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (bibleFileNames.size()>=i) {
                        FullscreenActivity.bibleFile = bibleFileNames.get(i);
                        Preferences.savePreferences();
                        File bibleFileChosen = new File(FullscreenActivity.dirbibles,bibleFileNames.get(i));
                        if (bibleFileChosen!=null && bibleFileChosen.exists() && bibleFileChosen.isFile()) {
                            updateBibleBooks(bibleFileChosen);
                        }
                        /*File bibleFileChosen = new File(FullscreenActivity.dirbibles, bibleFileNames.get(i));
                        updateBibleBooks(bibleFileChosen);*/
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            // Decide if we have already selected our favourite bible file, if so, set it
            if (bibleFileNames!=null && bibleFileNames.size()>0) {
                for (int i=0; i<bibleFileNames.size(); i++) {
                    if (bibleFileNames.get(i).equals(FullscreenActivity.bibleFile)) {
                        bibleFileSpinner.setSelection(i);
                    }
                }
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    public void updateBibleBooks(File bibleFileChosen) {
        try {
            quickUpdate = null;
            UpdateBibleBooks update_biblebooks = new UpdateBibleBooks(bibleFileChosen);
            update_biblebooks.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class UpdateBibleBooks extends AsyncTask<Object,Void,String> {

        File bibleFileChosen;
        UpdateBibleBooks(File f) {
            bibleFileChosen = f;
        }

        @Override
        protected void onPreExecute() {
            // Initialise the other spinners
            progressBar.setVisibility(View.VISIBLE);
            initialiseTheSpinners(bibleBookSpinner);
            initialiseTheSpinners(bibleChapterSpinner);
            initialiseTheSpinners(bibleVerseFromSpinner);
            initialiseTheSpinners(bibleVerseToSpinner);
            previewTextView.setText("");
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Get the bible book names if the bible file is set correctly
            bibleBookNames = Bible.getBibleBookNames(getActivity(), bibleFileChosen);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            ArrayAdapter b_aa = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,bibleBookNames);
            bibleBookSpinner.setAdapter(b_aa);
            bibleBookSpinner.setEnabled(true);
            // If the book listener changes, update the chapters and verses
            bibleBookSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // Get the Bible book name
                    if (bibleBookNames.size()>0 && bibleBookNames.size()>=i) {
                        String bibleBookName = bibleBookNames.get(i);
                        updateBibleChapters(bibleFileChosen, bibleBookName);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
            progressBar.setVisibility(View.GONE);
            quickUpdate = null;
        }
    }

    public void updateBibleChapters(File bibleFileChosen, String bibleBookName) {
        try {
            quickUpdate = null;
            UpdateBibleChapters update_biblechapters = new UpdateBibleChapters(bibleFileChosen, bibleBookName);
            update_biblechapters.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class UpdateBibleChapters extends AsyncTask<Object, Void, String> {
        File bibleFileChosen;
        String bibleBookName;

        UpdateBibleChapters (File f, String s){
            bibleFileChosen = f;
            bibleBookName = s;
        }

        @Override
        protected void onPreExecute() {
            // Initialise the other spinners
            progressBar.setVisibility(View.VISIBLE);
            initialiseTheSpinners(bibleChapterSpinner);
            initialiseTheSpinners(bibleVerseFromSpinner);
            initialiseTheSpinners(bibleVerseToSpinner);
            previewTextView.setText("");
        }

        @Override
        protected String doInBackground(Object... objects) {
            bibleChapters = Bible.getChaptersForBook(getActivity(), bibleFileChosen, bibleBookName);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            ArrayAdapter c_aa = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,bibleChapters);
            bibleChapterSpinner.setAdapter(c_aa);
            bibleChapterSpinner.setEnabled(true);
            bibleChapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (bibleChapters.size()>0 && bibleChapters.size()>=i) {
                        String bibleChapter = bibleChapters.get(i);
                        updateBibleVerses(bibleFileChosen, bibleBookName, bibleChapter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
            progressBar.setVisibility(View.GONE);
        }
    }

    public void updateBibleVerses(File bibleFileChosen, String bibleBookName, String bibleChapter) {
        try {
            quickUpdate = null;
            UpdateBibleVerses update_bibleverses = new UpdateBibleVerses(bibleFileChosen, bibleBookName, bibleChapter);
            update_bibleverses.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class UpdateBibleVerses extends AsyncTask<Object,Void,String> {

        File bibleFileChosen;
        String bibleBookName;
        String bibleChapter;
        UpdateBibleVerses(File f, String s1, String s2) {
            bibleFileChosen = f;
            bibleBookName = s1;
            bibleChapter = s2;
        }

        @Override
        protected void onPreExecute() {
            // Initialise the other spinners
            progressBar.setVisibility(View.VISIBLE);
            initialiseTheSpinners(bibleVerseFromSpinner);
            initialiseTheSpinners(bibleVerseToSpinner);
            previewTextView.setText("");
        }

        @Override
        protected String doInBackground(Object... objects) {
            Bible.getVersesForChapter(bibleFileChosen, bibleBookName, bibleChapter);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (bibleVerses != null && bibleVerses.size() > 0) {
                ArrayAdapter v_aa = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, bibleVerses);
                bibleVerseFromSpinner.setAdapter(v_aa);
                bibleVerseToSpinner.setAdapter(v_aa);
                bibleVerseFromSpinner.setEnabled(true);
                bibleVerseToSpinner.setEnabled(true);
                bibleVerseFromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (bibleVerses.size() > 0 && bibleVerses.size() >= i) {
                            String bibleVerseFrom = bibleVerses.get(i);

                            // Whatever this value is, if the 'to' spinner is before this, make it match this
                            if (bibleVerseToSpinner.getSelectedItemPosition() < i) {
                                bibleVerseToSpinner.setSelection(i);
                            }
                            String bibleVerseTo = bibleVerses.get(bibleVerseToSpinner.getSelectedItemPosition());
                            getBibleText(bibleFileChosen, bibleBookName, bibleChapter, bibleVerseFrom, bibleVerseTo);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                bibleVerseToSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (bibleVerses.size() > 0 && bibleVerses.size() >= i) {

                            String bibleVerseTo = bibleVerses.get(i);

                            // Whatever this value is, if the 'from' spinner is after this, make it match this
                            if (bibleVerseFromSpinner.getSelectedItemPosition() > i) {
                                bibleVerseFromSpinner.setSelection(i);
                            }
                            String bibleVerseFrom = bibleVerses.get(bibleVerseFromSpinner.getSelectedItemPosition());
                            getBibleText(bibleFileChosen, bibleBookName, bibleChapter, bibleVerseFrom, bibleVerseTo);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    public void getBibleText(File bibleFileChosen, String bibleBookName, String bibleChapter, String bibleVerseFrom, String bibleVerseTo) {
        quickUpdate = new ArrayList<>();
        quickUpdate.add(bibleFileChosen.toString());
        quickUpdate.add(bibleBookName);
        quickUpdate.add(bibleChapter);
        quickUpdate.add(bibleVerseFrom);
        quickUpdate.add(bibleVerseTo);
        int from;
        int to;
        try {
            from = Integer.parseInt(bibleVerseFrom);
        } catch (Exception e) {
            from = 0;
        }
        try {
            to = Integer.parseInt(bibleVerseTo);
        } catch (Exception e) {
            to = 0;
        }
        String s = "";
        if (to>0 && from>0 && to>=from) {

            for (int i=from; i<=to; i++) {
                if (bibleText.size()>=i) {
                    s = s + bibleText.get(i-1) + " ";
                }
            }
            // Trim and fix new sentence double spaces
            s = s.trim();
            s = s.replace(".  ",". ");
            s = s.replace(". ", ".  ");
        }
        previewTextView.setText(s);

        // Work out the Scripture title to use
        if (Bible.bibleFormat.equals("Zefania")) {
            bible = Bible.getZefaniaBibleName(bibleFileChosen);
        } else {
            bible = bibleFileChosen.getName().toUpperCase(FullscreenActivity.locale);
            bible = bible.replace(".XML", "");
            bible = bible.replace(".XMM", "");
        }

        String verses;
        if (from==to) {
            verses = "" + from;
        } else {
            verses = from + "-" + to;
        }
        FullscreenActivity.scripture_title = bibleBookName + " " + bibleChapter + ":" + verses + " (" + bible + ")";
        FullscreenActivity.scripture_verse = Bible.shortenTheLines(s,40,6);
    }

    public void doSave() {
        FullscreenActivity.whattodo = "customreusable_scripture";
        if (mListener!=null) {
            try {
                mListener.openFragment();
                dismiss();
            } catch (Exception e) {
                Log.d("d","Error grabbing details");
            }
        }

        if (mListener!=null) {
            try {
                FullscreenActivity.whattodo = "customreusable_scripture";
                mListener.openFragment();
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initialiseTheSpinners(Spinner spinner) {
        spinner.setAdapter(blank_array);
        spinner.setOnItemSelectedListener(null);
        spinner.setEnabled(false);
    }
}