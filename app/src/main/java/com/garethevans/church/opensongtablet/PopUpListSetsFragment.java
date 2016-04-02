package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PopUpListSetsFragment extends DialogFragment {

    static PopUpListSetsFragment newInstance() {
        PopUpListSetsFragment frag;
        frag = new PopUpListSetsFragment();
        return frag;
    }

    static EditText setListName;
    static TextView newSetPromptTitle;
    static String myTitle;
    static FetchDataTask dataTask;
    static ProgressDialog prog;
    public static String val;
    public static Handler mHandler;
    public static Runnable runnable;
    public static String[] setnames;
    public static ArrayAdapter<String> adapter;
    public static ListView setListView1;

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;

    @Override
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
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("PuSl", "main dismiss");
        try {
            dataTask.cancel(true);
        } catch (Exception e) {
            // Don't worry
        }

        try {
            dataTask = null;
        } catch (Exception e) {
            // Don't worry
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_setlists, container, false);

        // Reset the setname chosen
        FullscreenActivity.setnamechosen = "";
        FullscreenActivity.abort = false;

        ImageButton listSort_imageButton = (ImageButton) V.findViewById(R.id.listSort_imageButton);
        listSort_imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.sortAlphabetically = !FullscreenActivity.sortAlphabetically;
                sortSetLists();
            }
        });
        setListView1 = (ListView) V.findViewById(R.id.setListView1);
        setListName = (EditText) V.findViewById(R.id.setListName);
        newSetPromptTitle = (TextView) V.findViewById(R.id.newSetPromptTitle);
        Button listSetCancelButton = (Button) V.findViewById(R.id.listSetCancelButton);
        Button listSetOkButton = (Button) V.findViewById(R.id.listSetOkButton);
        setListName.setText(FullscreenActivity.lastSetName);

        // Sort the available set lists
        sortSetLists();

        myTitle = getActivity().getResources().getString(R.string.options_set);

        // Customise the view depending on what we are doing
        adapter = null;

        Log.d("d", "whattodo=" + FullscreenActivity.whattodo);
        switch (FullscreenActivity.whattodo) {
            default:
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_load);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                break;

            case "saveset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_save);
                break;

            case "deleteset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_delete);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                break;

            case "exportset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_export);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                break;
        }

        // Prepare the toast message using the title.  It is cleared if cancel is clicked
        FullscreenActivity.myToastMessage = myTitle + " : " + getActivity().getResources().getString(R.string.ok);

        getDialog().setTitle(myTitle);

        // Set The Adapter
        setCorrectAdapter();

        setListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the name of the set to do stuff with
                // Since we can select multiple sets, check it isn't already in the setnamechosen field
                Log.d("d","setnames[position] = "+setnames[position]);
                if (!FullscreenActivity.setnamechosen.contains(setnames[position])) {
                    // Add it to the setnamechosen
                    FullscreenActivity.setnamechosen = FullscreenActivity.setnamechosen + setnames[position] + "%_%";
                } else {
                    // Remove it from the setnamechosen
                    FullscreenActivity.setnamechosen = FullscreenActivity.setnamechosen.replace(setnames[position]+"%_%", "");
                }
                Log.d("d","setnamechosen = "+FullscreenActivity.setnamechosen);

                setListName.setText(setnames[position]);
            }
        });

        // Set up the cancel button
        listSetCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.myToastMessage = "";
                dismiss();
            }
        });

        // Set up the OK button
        listSetOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FullscreenActivity.whattodo.equals("loadset") && !FullscreenActivity.setnamechosen.isEmpty() && !FullscreenActivity.setnamechosen.equals("")) {
                    doLoadSet();
                } else if (FullscreenActivity.whattodo.equals("saveset") && !setListName.getText().toString().trim().isEmpty() && !setListName.getText().toString().trim().equals("")) {
                    doSaveSet();
                } else if (FullscreenActivity.whattodo.equals("deleteset") && !FullscreenActivity.setnamechosen.isEmpty() && !FullscreenActivity.setnamechosen.equals("")) {
                    doDeleteSet();
                } else if (FullscreenActivity.whattodo.equals("exportset") && !FullscreenActivity.setnamechosen.isEmpty() && !FullscreenActivity.setnamechosen.equals("")) {
                    doExportSet();
                }
            }
        });

        dataTask = new FetchDataTask();

        return V;
    }

    public void setCorrectAdapter() {
        switch (FullscreenActivity.whattodo) {
            default:
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, setnames);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                break;

            case "saveset":
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, setnames);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                break;

            case "deleteset":
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, setnames);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                break;

            case "exportset":
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, setnames);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                break;
        }
        setListView1.setAdapter(adapter);
        FullscreenActivity.setnamechosen = "";
    }

    // Actions to do with the selected set
    public void doLoadSet() {
        // Load the set up
        // Show the progress bar
        prog = null;
        prog = new ProgressDialog(getActivity()); //Assuming that you are using fragments.
        prog.setTitle(getString(R.string.options_set_load));
        prog.setMessage(getString(R.string.wait));
        prog.setCancelable(true);
        prog.setIndeterminate(true);
        prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("doLoadSet", "onDismissListener");
                FullscreenActivity.abort = true;
                try {
                    dataTask.cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mHandler = new Handler();
        runnable = new Runnable() {
            public void run() {
                prog.setMessage(val);
            }
        };
        prog.show();

        FullscreenActivity.settoload = null;
        FullscreenActivity.abort = false;

        FullscreenActivity.settoload = FullscreenActivity.setnamechosen;
        FullscreenActivity.lastSetName = FullscreenActivity.setnamechosen;
        dataTask = null;
        dataTask = new FetchDataTask();
        dataTask.execute();
    }

    public void doSaveSet() {
        // Save the set into the settoload name
        FullscreenActivity.settoload = setListName.getText().toString().trim();
        FullscreenActivity.lastSetName = setListName.getText().toString().trim();

        // Popup the are you sure alert into another dialog fragment
        String message = getResources().getString(R.string.options_set_save) + " \'" + setListName.getText().toString().trim() + "\"?";
        FullscreenActivity.myToastMessage = message;
        DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
        newFragment.show(getFragmentManager(), "dialog");
        dismiss();
        // If the user clicks on the areyousureYesButton, then action is confirmed as ConfirmedAction
    }

    public void doDeleteSet() {
        // Load the set up
        FullscreenActivity.settoload = null;
        FullscreenActivity.settoload = FullscreenActivity.setnamechosen;

        // Popup the are you sure alert into another dialog fragment
        // Get the list of set lists to be deleted
        String setstodelete = FullscreenActivity.setnamechosen.replace("%_%",", ");
        setstodelete = setstodelete.substring(0,setstodelete.length()-2);

        //String message = getResources().getString(R.string.options_set_delete) + " \"" + setListName.getText().toString().trim() + "\"?";
        String message = getResources().getString(R.string.options_set_delete) + " \"" + setstodelete + "\"?";
        FullscreenActivity.myToastMessage = message;
        DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
        newFragment.show(getFragmentManager(), "dialog");
        dismiss();
        // If the user clicks on the areyousureYesButton, then action is confirmed as ConfirmedAction

    }

    public void doExportSet() {
        // Load the set up
        FullscreenActivity.settoload = null;
        FullscreenActivity.settoload = FullscreenActivity.setnamechosen;

        // Run the script that generates the email text which has the set details in it.
        try {
            ExportPreparer.setParser();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, FullscreenActivity.settoload);
        emailIntent.putExtra(Intent.EXTRA_TITLE, FullscreenActivity.settoload);
        emailIntent.putExtra(Intent.EXTRA_TEXT, FullscreenActivity.settoload + "\n\n" + FullscreenActivity.emailtext);
        FullscreenActivity.emailtext = "";
        File setfile  = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
        File ostsfile = new File(FullscreenActivity.homedir + "/Notes/_cache/" + FullscreenActivity.settoload + ".osts");

        if (!setfile.exists() || !setfile.canRead()) {
            return;
        }

        // Copy the set file to an .osts file
        try {
            FileInputStream in = new FileInputStream(setfile);
            FileOutputStream out = new FileOutputStream(ostsfile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file (You have now copied the file)
            out.flush();
            out.close();

        } catch (Exception e) {
            // Error
            e.printStackTrace();
        }

        Uri uri_set  = Uri.fromFile(setfile);
        Uri uri_osts = Uri.fromFile(ostsfile);

        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(uri_set);
        uris.add(uri_osts);

        // Go through each song in the set and attach them
        // Also try to attach a copy of the song ending in .ost, as long as they aren't images
        Log.d("Export set","exportsetfilenames="+FullscreenActivity.exportsetfilenames);
        for (int q=0; q<FullscreenActivity.exportsetfilenames.size(); q++) {
            // Remove any subfolder from the exportsetfilenames_ost.get(q)
            String tempsong_ost = FullscreenActivity.exportsetfilenames_ost.get(q);
            tempsong_ost = tempsong_ost.substring(tempsong_ost.indexOf("/")+1);
            File songtoload  = new File(FullscreenActivity.dir + "/" + FullscreenActivity.exportsetfilenames.get(q));
            File ostsongcopy = new File(FullscreenActivity.homedir + "/Notes/_cache/" + tempsong_ost + ".ost");
            boolean isimage = false;
            if (songtoload.toString().endsWith(".jpg") || songtoload.toString().endsWith(".JPG") ||
                    songtoload.toString().endsWith(".jpeg") || songtoload.toString().endsWith(".JPEG") ||
                    songtoload.toString().endsWith(".gif") || songtoload.toString().endsWith(".GIF") ||
                    songtoload.toString().endsWith(".png") || songtoload.toString().endsWith(".PNG") ||
                    songtoload.toString().endsWith(".bmp") || songtoload.toString().endsWith(".BMP")) {
                songtoload = new File(FullscreenActivity.exportsetfilenames.get(q));
                isimage = true;
            }

            // Copy the song
            if (songtoload.exists()) {
                try {
                    if (!isimage) {
                        FileInputStream in = new FileInputStream(songtoload);
                        FileOutputStream out = new FileOutputStream(ostsongcopy);

                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();

                        // write the output file (You have now copied the file)
                        out.flush();
                        out.close();

                        Uri urisongs_ost = Uri.fromFile(ostsongcopy);
                        uris.add(urisongs_ost);
                    }
                    Uri urisongs = Uri.fromFile(songtoload);
                    uris.add(urisongs);

                } catch (Exception e) {
                    // Error
                    e.printStackTrace();
                }
            }
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivityForResult(Intent.createChooser(emailIntent, FullscreenActivity.exportsavedset), 12345);

        // Close this dialog
        dismiss();
    }

    public class FetchDataTask extends AsyncTask<String,Integer,String> {

        @Override
        public void onPreExecute() {
            // Check the directories and clear them of prior content
            SetActions.checkDirectories();
        }

        @Override
        protected String doInBackground(String... args) {
            Log.d("dataTask", "doInBackground");

            // Now users can load multiple sets and merge them, we need to load each one it turn
            // We then add the items to a temp string 'allsongsinset'
            // Once we have loaded them all, we replace the mySet field.

            String allsongsinset = "";

            // Split the string by "%_%" - last item will be empty as each set added ends with this
            String[] tempsets = FullscreenActivity.setnamechosen.split("%_%");

            for (String tempfile:tempsets) {
                if (tempfile!=null && !tempfile.equals("") && !tempfile.isEmpty()) {
                    try {
                        FullscreenActivity.settoload = tempfile;
                        SetActions.loadASet();
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }
                    allsongsinset = allsongsinset + FullscreenActivity.mySet;
                }
            }

            // Add all the songs of combined sets back to the mySet
            FullscreenActivity.mySet = allsongsinset;

            // Reset the options menu
            SetActions.prepareSetList();
            SetActions.indexSongInSet();

            return "LOADED";
        }

        @Override
        protected void onCancelled(String result) {
            Log.d("dataTask","onCancelled");
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("dataTask", "onPostExecute");
            FullscreenActivity.setView = "Y";

            if (result.equals("LOADED") && !dataTask.isCancelled()) {
                // Get the set first item
                SetActions.prepareFirstItem();

                // Save the new set to the preferences
                Preferences.savePreferences();

                // Tell the listener to do something
                mListener.refreshAll();
                FullscreenActivity.abort = false;
                //Close this dialog
                dismiss();
            }
            prog.dismiss();
        }
    }

    public void sortSetLists() {
        // Sort the set lists either alphabetically or reverse alphabetically
        ArrayList<String> setnames_ar = new ArrayList<>(Arrays.asList(FullscreenActivity.mySetsFileNames));

        if (!FullscreenActivity.sortAlphabetically) {
            Collections.sort(setnames_ar);
            Collections.reverse(setnames_ar);
            Log.d("d", "setnames_ar=" + setnames_ar);
            Log.d("d","Reverse order");
        } else {
            Collections.sort(setnames_ar);
            Log.d("d", "Normal order");
            Log.d("d", "setnames_ar=" + setnames_ar);

        }

        setnames = new String[setnames_ar.size()];
        setnames = setnames_ar.toArray(setnames);

        Log.d("d", "setnames=" + Arrays.toString(setnames));

        if (adapter!=null) {
            Log.d("d", "Update list");
            setCorrectAdapter();
            //setListView1.setAdapter(adapter);
            //((BaseAdapter) setListView1.getAdapter()).notifyDataSetChanged();
            //setCorrectAdapter();
        }

    }
}