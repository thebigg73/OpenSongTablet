package com.garethevans.church.opensongtablet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class PopUpExportSongListFragment extends DialogFragment {

    static PopUpExportSongListFragment newInstance() {
        PopUpExportSongListFragment frag;
        frag = new PopUpExportSongListFragment();
        return frag;
    }

    private ListView songDirectoy_ListView;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ArrayList<String> songfolders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View V = inflater.inflate(R.layout.popup_exportsonglist, container, false);
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.exportsongdirectory));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getActivity());
            saveMe.setEnabled(false);
            getFoldersSelected();
        });

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        SongFolders songFolders = new SongFolders();


        songDirectoy_ListView = V.findViewById(R.id.songDirectoy_ListView);

        // Prepare a list of the song directories
        songfolders = songFolders.prepareSongFolders(getActivity(),preferences);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_multiple_choice, songfolders);
        songDirectoy_ListView.setAdapter(adapter);
        songDirectoy_ListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void getFoldersSelected() {
        // Get the selected index
        ArrayList<String> folders = new ArrayList<>();
        for (int i=0;i<songfolders.size();i++) {
            if (songDirectoy_ListView.isItemChecked(i)) {
                folders.add(songfolders.get(i));
            }
        }
        if (folders.size()>0) {
            prepareSongDirectory(folders);
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    private void prepareSongDirectory(ArrayList<String> directories) {
        // For each selected directory, list the songs that exist.
        StringBuilder songContents = new StringBuilder();

        for (String directory : directories) {
            if (directory.equals(getString(R.string.mainfoldername)) || directory.equals("MAIN")) {
                directory = "";
            }
            ArrayList<String> files_ar = storageAccess.listFilesInFolder(getActivity(), preferences, "Songs", directory);
            songContents.append(Objects.requireNonNull(getActivity()).getString(R.string.songsinfolder)).append(" \"");
            if (directory.equals("")) {
                songContents.append(getString(R.string.mainfoldername));
            } else {
                songContents.append(directory);
            }
            songContents.append("\":\n\n");
            try {
                Collator coll = Collator.getInstance(StaticVariables.locale);
                coll.setStrength(Collator.SECONDARY);
                Collections.sort(files_ar, coll);
                for (int l = 0; l < files_ar.size(); l++) {
                    songContents.append(files_ar.get(l)).append("\n");
                }
            } catch (Exception e) {
                // Error sorting
            }
            songContents.append("\n\n\n\n");
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, Objects.requireNonNull(getActivity()).getString(R.string.app_name) + " " +
                getActivity().getString(R.string.exportsongdirectory));
        intent.putExtra(Intent.EXTRA_TEXT, songContents.toString());

        String title = getActivity().getResources().getString(R.string.export);
        Intent chooser = Intent.createChooser(intent, title);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(chooser);
        }

        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}