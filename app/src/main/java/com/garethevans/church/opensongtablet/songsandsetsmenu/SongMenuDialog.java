package com.garethevans.church.opensongtablet.songsandsetsmenu;

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
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

public class SongMenuDialog extends DialogFragment {

    private MenuSongsDialogBinding myView;
    private MainActivityInterface mainActivityInterface;
    private SetActions setActions;
    private CurrentSet currentSet;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private ProcessSong processSong;
    private SQLiteHelper sqLiteHelper;
    private CommonSQL commonSQL;
    private MakePDF makePDF;
    private OCR ocr;
    private NewNameDialog dialog;
    private Song thisSong;

    private final String folder;
    private final String song;

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

        thisSong = sqLiteHelper.getSpecificSong(getContext(),commonSQL,folder,song);

        return myView.getRoot();
    }

    private void setHelpers() {
        setActions = mainActivityInterface.getSetActions();
        currentSet = mainActivityInterface.getCurrentSet();
        preferences = mainActivityInterface.getPreferences();
        makePDF = mainActivityInterface.getMakePDF();
        ocr = mainActivityInterface.getOCR();
        storageAccess = mainActivityInterface.getStorageAccess();
        processSong = mainActivityInterface.getProcessSong();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        commonSQL = mainActivityInterface.getCommonSQL();
    }

    private void setListeners() {
        // Listeners for the currently loaded song
        myView.editButton.setOnClickListener(new ActionClickListener(R.id.editSongFragment,"editSong"));
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
                    mainActivityInterface.navigateToFragment(id);
                    break;

                case "duplicateSong":
                    dialog = new NewNameDialog(null,"duplicateSong",true,"Songs",mainActivityInterface.getSong().getFolder(),thisSong);
                    dialog.show(getActivity().getSupportFragmentManager(),"NewNameDialog");
                    break;

                case "deleteSong":
                    String str = getString(R.string.delete) + ": " + commonSQL.getAnySongId(mainActivityInterface.getSong().getFolder(),
                            mainActivityInterface.getSong().getFilename());
                    mainActivityInterface.displayAreYouSure("deleteSong", str,null, "SongMenuFragment",null,thisSong);
                    break;

                case "addToSet":
                    Song tsong = new Song();
                    tsong.setFilename(song);
                    tsong.setFolder(folder);
                    tsong.setSongid(commonSQL.getAnySongId(folder,song));
                    setActions.addToSet(getContext(),preferences,currentSet,tsong);
                    mainActivityInterface.refreshSetList();
                    break;

                case "exportSong":
                    // TODO For now, test the pdf creation
                    Uri uri = makePDF.createPDF(getContext(),preferences,storageAccess,processSong,thisSong);
                    Log.d("d","created uri:"+uri);
                    break;

                case "createSong":
                    // The user is first asked for a new song name.  It is then created and then the user is sent to the edit page.
                    dialog = new NewNameDialog(null,"createNewSong",true,"Songs",mainActivityInterface.getSong().getFolder(),thisSong);
                    dialog.show(getActivity().getSupportFragmentManager(),"NewNameDialog");
                    break;

                case "importSong":
                    // This will give the option of importing/downloading my songs, finding a file or downloading from an online service.
                    // mainActivityInterface.navigateToFragment(R.id.importSongs);
                    // TODO for now try reading in a pdf
                    Log.d("SongMwnuDialog","Getting here");
                    mainActivityInterface.navigateToFragment(R.id.importOptionsFragment);
                    /*NavHostFragment.findNavController(callingFragment)
                            .navigate(R.id.ac,null,null);*/
                    //ocr.getTextFromPDF(getContext(),preferences,storageAccess,processSong,mainActivityInterface,"test","MAIN_Give thanks.pdf");
                    break;

            }
            dismiss();
        }
    }

}