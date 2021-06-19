package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageFolderDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class StorageManagementFragment extends Fragment {

    private StorageFolderDisplayBinding myView;

    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> infos;
    private ArrayList<String> dismisses;
    private ArrayList<View> views = new ArrayList<>();
    private ArrayList<Boolean> rects = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageFolderDisplayBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.storage));

        // Do this as separate tasks in a new thread
        setUpThread();

        return myView.getRoot();
    }

    private void setUpThread() {

        new Thread(() -> {
            requireActivity().runOnUiThread(() -> {
                String text = "OpenSong\n("+getString(R.string.root)+")";
                myView.rootFolder.setText(text);
                myView.rootFolder.setOnClickListener(v -> showActionDialog(true,false,""));
                text = getString(R.string.songs)+"\n("+getString(R.string.mainfoldername)+")";
                myView.mainFolder.setText(text);
                myView.mainFolder.setOnClickListener(v -> showActionDialog(false,true,""));
                // Now look for subfolders
                createNodes();

                myView.getRoot().invalidate();
                myView.folderList.invalidate();
            });

            // Prepare the showcase
            initialiseShowcaseArrays();
            requireActivity().runOnUiThread(() -> {
                prepareShowcaseViews();
                mainActivityInterface.getShowCase().sequenceShowCase(requireActivity(),views,dismisses,infos,rects,"storageManagement");
            });
        }).start();
    }

    private void createNodes() {
        ArrayList<String> availableFolders = getFoldersFromFile();

        for (String folder: availableFolders) {
            TextView textView = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(24,0,24,24);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            textView.setLayoutParams(lp);
            textView.setText(folder);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setPadding(48,48,48,48);
            textView.setBackgroundColor(getResources().getColor(R.color.blue));
            textView.setOnClickListener(v -> showActionDialog(false,false,folder));
            myView.folderList.addView(textView);
        }
        if (availableFolders.size()>0) {
            myView.subFolderArrow.setVisibility(View.VISIBLE);
            myView.folderList.setVisibility(View.VISIBLE);

        } else {
            myView.subFolderArrow.setVisibility(View.GONE);
            myView.folderList.setVisibility(View.GONE);
        }
    }


    private void initialiseShowcaseArrays() {
        views = new ArrayList<>();
        infos = new ArrayList<>();
        dismisses = new ArrayList<>();
        rects = new ArrayList<>();
        infos.add(getString(R.string.storage_reset));
        dismisses.add(null);
        rects.add(true);
        infos.add(getString(R.string.storage_main));
        dismisses.add(null);
        rects.add(true);
    }

    private void prepareShowcaseViews() {
        views.add(myView.rootFolder);
        views.add(myView.mainFolder);
    }

    private void showActionDialog(boolean root, boolean songs, String folder) {
        FolderManagementDialog dialogFragment = new FolderManagementDialog(this,root,songs,folder);
        dialogFragment.show(requireActivity().getSupportFragmentManager(),"folderManagementDialog");
    }

    public void updateFragment() {
        // Called from MainActivity when change has been made from Dialog
        myView.folderList.removeAllViews();
        setUpThread();
    }

    private ArrayList<String> getFoldersFromFile() {
        // Scan the storage
        ArrayList<String> songIDs = mainActivityInterface.getStorageAccess().listSongs(requireContext(), mainActivityInterface);
        mainActivityInterface.getStorageAccess().writeSongIDFile(requireContext(),mainActivityInterface, songIDs);
        // Each subdir ends with /
        return mainActivityInterface.getStorageAccess().getSongFolders(requireContext(), songIDs,false,null);
    }

}
