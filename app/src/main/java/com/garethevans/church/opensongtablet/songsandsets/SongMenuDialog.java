package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSongsDialogBinding;
import com.garethevans.church.opensongtablet.export.MakePDF;
import com.garethevans.church.opensongtablet.filemanagement.NewNameDialog;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.getnewsongs.OCR;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

public class SongMenuDialog extends DialogFragment {

    MenuSongsDialogBinding myView;
    MainActivityInterface mainActivityInterface;
    SongForSet songForSet;
    Preferences preferences;
    SetActions setActions;
    StorageAccess storageAccess;
    ProcessSong processSong;
    SQLiteHelper sqLiteHelper;
    CommonSQL commonSQL;
    MakePDF makePDF;
    OCR ocr;
    NewNameDialog dialog;
    SQLite thisSongSQL;

    private String folder, song;

    SongMenuDialog (String folder, String song) {
        this.folder = folder;
        this.song = song;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
        mainActivityInterface.songMenuActionButtonShow(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = MenuSongsDialogBinding.inflate(inflater,container,false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setGravity(Gravity.BOTTOM|Gravity.END);

        // Display current song at the top
        myView.songSelected.setText(song);

        setHelpers();
        setListeners();

        thisSongSQL = sqLiteHelper.getSpecificSong(getActivity(),commonSQL,folder,song);

        return myView.getRoot();
    }

    private void setHelpers() {
        songForSet = new SongForSet();
        preferences = new Preferences();
        setActions = new SetActions();
        makePDF = new MakePDF();
        ocr = new OCR();
        storageAccess = new StorageAccess();
        processSong = new ProcessSong();
        sqLiteHelper = new SQLiteHelper(getActivity());
        commonSQL = new CommonSQL();
    }

    private void setListeners() {
        // Listeners for the currently loaded song
        myView.editButton.setOnClickListener(new ActionClickListener(R.id.nav_editSong,"editSong"));
        myView.duplicateButton.setOnClickListener(new ActionClickListener(0, "duplicateSong"));
        myView.deleteButton.setOnClickListener(new ActionClickListener(0, "deleteSong"));
        myView.setButton.setOnClickListener(v -> new ActionClickListener(0,"addToSet"));
        myView.exportButton.setOnClickListener(new ActionClickListener(0,"exportSong"));

        // Listeners to add/create new songs
        myView.createButton.setOnClickListener(new ActionClickListener(0,"createSong"));
        myView.importButton.setOnClickListener(new ActionClickListener(0,"importSong"));
    }

    private class ActionClickListener implements View.OnClickListener {

        final int id;
        final String str;
        ActionClickListener(int id, String str) {
            this.id = id;
            this.str = str;
        }
        @Override
        public void onClick(View v) {
            switch (str) {
                case "editSong":
                    StaticVariables.whichSongFolder = folder;
                    StaticVariables.songfilename = song;
                    mainActivityInterface.navigateToFragment(id);
                    break;

                case "duplicateSong":
                    dialog = new NewNameDialog(null,"duplicateSong",true,"Songs",StaticVariables.whichSongFolder);
                    dialog.show(getActivity().getSupportFragmentManager(),"NewNameDialog");
                    break;

                case "deleteSong":
                    String str = getString(R.string.delete) + ": " +
                            StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
                    mainActivityInterface.displayAreYouSure("deleteSong", str,null, "SongMenuFragment",null);
                    break;

                case "addToSet":
                    songForSet.addToSet(requireContext(),preferences,setActions,StaticVariables.whichSongFolder,StaticVariables.songfilename);
                    mainActivityInterface.refreshSetList();
                    break;

                case "exportSong":
                    // TODO For now, test the pdf creation
                    Uri uri = makePDF.createPDF(getActivity(),preferences,storageAccess,processSong,thisSongSQL);
                    Log.d("d","created uri:"+uri);
                    break;

                case "createSong":
                    // The user is first asked for a new song name.  It is then created and then the user is sent to the edit page.
                    dialog = new NewNameDialog(null,"createNewSong",true,"Songs",StaticVariables.whichSongFolder);
                    dialog.show(getActivity().getSupportFragmentManager(),"NewNameDialog");
                    break;

                case "importSong":
                    // This will give the option of importing/downloading my songs, finding a file or downloading from an online service.
                    // mainActivityInterface.navigateToFragment(R.id.importSongs);
                    // TODO for now try reading in a pdf
                    ocr.getTextFromPDF(getActivity(),preferences,storageAccess,processSong,mainActivityInterface,"test","MAIN_Give thanks.pdf");
                    break;

            }
            dismiss();
        }
    }

}