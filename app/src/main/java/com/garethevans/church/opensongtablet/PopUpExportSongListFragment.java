package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class PopUpExportSongListFragment extends DialogFragment {

    static PopUpExportSongListFragment newInstance() {
        PopUpExportSongListFragment frag;
        frag = new PopUpExportSongListFragment();
        return frag;
    }

    ListView songDirectoy_ListView;
    Button cancel_Button;
    Button export_Button;

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

        View V = inflater.inflate(R.layout.popup_exportsonglist, container, false);
        getDialog().setTitle(getActivity().getString(R.string.exportsongdirectory));
        getDialog().setCanceledOnTouchOutside(true);

        songDirectoy_ListView = (ListView) V.findViewById(R.id.songDirectoy_ListView);
        cancel_Button = (Button) V.findViewById(R.id.cancel_Button);
        export_Button = (Button) V.findViewById(R.id.export_Button);

        // Prepare a list of the song directories
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, FullscreenActivity.mSongFolderNames);
        songDirectoy_ListView.setAdapter(adapter);
        songDirectoy_ListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Listener
        cancel_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        export_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFoldersSelected();
            }
        });

        return V;
    }

    public void getFoldersSelected() {
        // Get the selected index
        ArrayList<String> folders = new ArrayList<>();
        for (int i=0;i<FullscreenActivity.mSongFolderNames.length;i++) {
            if (songDirectoy_ListView.isItemChecked(i)) {
                folders.add(FullscreenActivity.mSongFolderNames[i]);
            }
        }
        if (folders.size()>0) {
            prepareSongDirectory(folders);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    public void prepareSongDirectory(ArrayList<String> directories) {
        // For each selected directory, list the song that exist.
        String songContents = "";

        Log.d("d", "directories="+directories.toString());
        for (String directory:directories) {
            File directory_file;
            if (directory.equals(FullscreenActivity.mainfoldername)) {
                directory_file = FullscreenActivity.dir;
            } else {
                directory_file = new File(FullscreenActivity.dir + "/" + directory);
            }
            File[] contents;
            if (directory_file.exists()) {
                contents = directory_file.listFiles();
                ArrayList<String> files_ar = new ArrayList<>();
                for (File s:contents) {
                    if (s.isFile()) {
                        files_ar.add(s.getName());
                    }
                }

                songContents += getActivity().getString(R.string.songsinfolder) + " \"" + directory + "\":\n\n";

                try {
                    Collator coll = Collator.getInstance(FullscreenActivity.locale);
                    coll.setStrength(Collator.SECONDARY);
                    Collections.sort(files_ar, coll);
                    for (int l=0;l<files_ar.size();l++) {
                        songContents += files_ar.get(l) + "\n";
                    }
                } catch (Exception e) {
                    // Error sorting
                }

            }
            songContents += "\n\n\n\n";
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getActivity().getString(R.string.app_name) + " " +
                getActivity().getString(R.string.exportsongdirectory));
        intent.putExtra(Intent.EXTRA_TEXT, songContents);

        String title = getActivity().getResources().getString(R.string.options_song_export);
        Intent chooser = Intent.createChooser(intent, title);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(chooser);
        }
    }
}