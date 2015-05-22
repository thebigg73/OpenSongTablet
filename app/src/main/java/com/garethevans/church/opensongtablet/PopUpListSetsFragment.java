package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.IOException;

public class PopUpListSetsFragment extends DialogFragment {

    static PopUpListSetsFragment newInstance() {
        PopUpListSetsFragment frag;
        frag = new PopUpListSetsFragment();
        return frag;
    }

    static EditText setListName;
    static TextView newSetPromptTitle;

    static String myTitle;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_setlists, container, false);

        // Reset the setname chosen
        FullscreenActivity.setnamechosen = "";

        ListView setListView = (ListView) V.findViewById(R.id.setListView);
        setListName = (EditText) V.findViewById(R.id.setListName);
        newSetPromptTitle = (TextView) V.findViewById(R.id.newSetPromptTitle);
        Button listSetCancelButton = (Button) V.findViewById(R.id.listSetCancelButton);
        Button listSetOkButton = (Button) V.findViewById(R.id.listSetOkButton);
        setListName.setText(FullscreenActivity.lastSetName);

        myTitle = getActivity().getResources().getString(R.string.options_set);

        // Customise the view depending on what we are doing
        ArrayAdapter<String> adapter = null;

        switch (FullscreenActivity.whattodo) {
            case "loadset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_load);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, FullscreenActivity.mySetsFileNames);

                break;
            case "saveset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_save);
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, FullscreenActivity.mySetsFileNames);

                break;
            case "deleteset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_delete);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, FullscreenActivity.mySetsFileNames);

                break;
            case "exportset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_export);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, FullscreenActivity.mySetsFileNames);

                break;
        }

        // Prepare the toast message using the title.  It is cleared if cancel is clicked
        FullscreenActivity.myToastMessage = myTitle + " : " + getActivity().getResources().getString(R.string.ok);

        getDialog().setTitle(myTitle);


        // Set The Adapter
        setListView.setAdapter(adapter);

        setListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the name of the set to do stuff with
                FullscreenActivity.setnamechosen = FullscreenActivity.mySetsFileNames[position];
                setListName.setText(FullscreenActivity.mySetsFileNames[position]);
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

        return V;
    }

    // Actions to do with the selected set
    public void doLoadSet() {
        // Load the set up
        FullscreenActivity.settoload = null;
        FullscreenActivity.settoload = FullscreenActivity.setnamechosen;
        FullscreenActivity.lastSetName = FullscreenActivity.setnamechosen;
        try {
            SetActions.loadASet(getView());
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        // Reset the options menu
        SetActions.prepareSetList();
        SetActions.indexSongInSet();
        FullscreenActivity.setView = "Y";

        // Save the new set to the preferences
        Preferences.savePreferences();

        // Tell the listener to do something
        mListener.refreshAll();

        //Close this dialog
        dismiss();
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
        String message = getResources().getString(R.string.options_set_delete) + " \'" + setListName.getText().toString().trim() + "\"?";
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

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, FullscreenActivity.settoload);
        emailIntent.putExtra(Intent.EXTRA_TITLE, FullscreenActivity.settoload);
        emailIntent.putExtra(Intent.EXTRA_TEXT, FullscreenActivity.settoload + "\n\n" + FullscreenActivity.emailtext);
        FullscreenActivity.emailtext = "";
        File file = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
        if (!file.exists() || !file.canRead()) {
            return;
        }
        Uri uri = Uri.fromFile(file);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(emailIntent, FullscreenActivity.exportsavedset));

        // Close this dialog
        dismiss();
    }

}