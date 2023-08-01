package com.garethevans.church.opensongtablet.links;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetLinksBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LinksBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "LinkBottomSheet";
    private BottomSheetLinksBinding myView;
    private MainActivityInterface mainActivityInterface;
    private MediaPlayer mediaPlayer;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private final String fragName;
    private final Fragment callingFragment;
    private String link_youtube_string="", link_search_youtube_string="", youtube_string="",
            music_string="", link_audio_string="", link_search_document_string="", link_web_string="",
            link_search_web_string="", link_file_string="", success_string="", error_string="",
            nothing_selected_string="", link_error_string="";

    public LinksBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        fragName = "";
        callingFragment = null;
        dismiss();
    }

    LinksBottomSheet(String fragName, Fragment callingFragment) {
        this.fragName = fragName;
        this.callingFragment = callingFragment;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetLinksBinding.inflate(inflater, container, false);

        prepareStrings();

        myView.nestedScrollView.setFabToAnimate(myView.openLink);
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            link_youtube_string = getString(R.string.link_youtube);
            link_search_youtube_string = getString(R.string.link_search_youtube);
            youtube_string = getString(R.string.youtube);
            music_string = getString(R.string.music);
            link_audio_string = getString(R.string.link_audio);
            link_search_document_string = getString(R.string.link_search_document);
            link_web_string = getString(R.string.link_web);
            link_search_web_string = getString(R.string.link_search_web);
            link_file_string = getString(R.string.link_file);
            success_string = getString(R.string.success);
            error_string = getString(R.string.error);
            nothing_selected_string = getString(R.string.nothing_selected);
            link_error_string = getString(R.string.link_error);
        }
    }
    @SuppressLint("WrongConstant") // takeFlags is correct on Google documentation!!!!
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Decide on the type of link and set the appropriate values
        setupViews();

        // Initialise the launcher
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Log.d(TAG,"resultCode()="+result.getResultCode());
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri contentUri = data.getData();

                        // If this is a localised (i.e. inside OpenSong folder), we don't need to take the permissions
                        // There is a limit of 128-512 permissions allowed (depending on Android version).
                        String localisedUri = mainActivityInterface.getStorageAccess().fixUriToLocal(contentUri);
                        if (!localisedUri.contains("../OpenSong/") && getActivity()!=null) {
                            ContentResolver resolver = getActivity().getContentResolver();
                            resolver.takePersistableUriPermission(contentUri, data.getFlags()
                                    & ( Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    + Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            ));

                        }
                        myView.linkLocation.setText(mainActivityInterface.getStorageAccess().fixUriToLocal(contentUri));
                    }
                } catch (Exception e) {
                    badLink();
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupViews() {
        Log.d(TAG,mainActivityInterface.getWhattodo());
        switch (mainActivityInterface.getWhattodo()) {
            case "linkYouTube":
            default:
                myView.dialogHeading.setText(link_youtube_string);
                myView.linkLocation.setText(mainActivityInterface.getSong().getLinkyoutube());
                if (myView.linkLocation.getText().toString().contains("https://music.youtube.com")) {
                    myView.youTubeOrMusic.setSliderPos(1);
                } else {
                    myView.youTubeOrMusic.setSliderPos(0);
                }
                if (getContext()!=null) {
                    myView.openLink.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.youtube));
                }
                myView.openLink.setOnClickListener(view -> openDocument());
                myView.searchLink.setHint(link_search_youtube_string);
                myView.searchLink.setOnClickListener(view -> {
                    if (myView.youTubeOrMusic.getValue()==0) {
                        openBrowser("https://www.youtube.com/search?q=");
                    } else {
                        openBrowser("https://music.youtube.com/search?q=");
                    }
                });
                myView.youTubeOrMusic.setVisibility(View.VISIBLE);
                myView.youTubeOrMusic.setTextRight(youtube_string + " " + music_string);
                break;

            //music.youtube.com/watch?v=
            case "linkAudio":
                myView.dialogHeading.setText(link_audio_string);
                myView.linkLocation.setText(mainActivityInterface.getSong().getLinkaudio());
                if (getContext()!=null) {
                    myView.openLink.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.play));
                }
                myView.padLink.setVisibility(View.VISIBLE);
                myView.padLink.setOnClickListener(view -> setLinkAsPad());
                myView.padLink.setVisibility(View.VISIBLE);
                myView.openLink.setOnClickListener(view -> previewAudio(true));
                myView.searchLink.setHint(link_search_document_string);
                myView.searchLink.setOnClickListener(view -> searchFile("audio/*"));
                break;
            case "linkOnline":
                myView.dialogHeading.setText(link_web_string);
                myView.linkLocation.setText(mainActivityInterface.getSong().getLinkweb());
                if (getContext()!=null) {
                    myView.openLink.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.web));
                }
                myView.openLink.setOnClickListener(view -> openDocument());
                myView.searchLink.setHint(link_search_web_string);
                myView.searchLink.setOnClickListener(view -> openBrowser("https://www.google.com/search?q="));
                break;
            case "linkOther":
                myView.dialogHeading.setText(link_file_string);
                myView.linkLocation.setText(mainActivityInterface.getSong().getLinkother());
                if (getContext()!=null) {
                    myView.openLink.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.xml));
                }
                myView.openLink.setOnClickListener(view -> openDocument());
                myView.searchLink.setHint(link_search_document_string);
                myView.searchLink.setOnClickListener(view -> searchFile("*/*"));
                break;
        }
        myView.resetLink.setOnClickListener(view -> resetLink());
        myView.dialogHeading.setClose(this);
        myView.saveLink.setOnClickListener(view -> updateSong());
    }

    private void openBrowser(String address) {
        address += mainActivityInterface.getSong().getTitle() + " " +
                mainActivityInterface.getSong().getAuthor();

        if (!address.trim().startsWith("http://") && !address.trim().startsWith("https://")) {
            address = "http://"+address.trim();
            myView.linkLocation.setText(address);
        }
        mainActivityInterface.openDocument(address);
    }

    private void searchFile(String mimeType) {
        // Try to open at the default OpenSong location
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        activityResultLauncher.launch(intent);
    }

    private void openDocument() {
        Log.d(TAG,"getLinkText(): "+getLinkText());
        // Try to open the file or webpage if it isn't null
        if (!getLinkText().isEmpty() && !getLinkText().contains("http")) {
            Uri uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(getLinkText());
            mainActivityInterface.openDocument(uri.toString());
        } else if (!getLinkText().isEmpty()) {
            mainActivityInterface.openDocument(getLinkText());
        } else {
            myView.linkLocation.requestFocus();
        }
    }

    private void resetLink() {
        myView.linkLocation.setText("");
        updateSong();
    }

    private String getLinkText() {
        return myView.linkLocation.getText().toString();
    }

    private void updateSong() {
        switch (mainActivityInterface.getWhattodo()) {
            case "linkYouTube":
            default:
                mainActivityInterface.getSong().setLinkyoutube(getLinkText());
                break;
            case "linkAudio":
                mainActivityInterface.getSong().setLinkaudio(getLinkText());
                break;
            case "linkOnline":
                mainActivityInterface.getSong().setLinkweb(getLinkText());
                break;
            case "linkOther":
                mainActivityInterface.getSong().setLinkother(getLinkText());
                break;
        }
        if (mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false)) {
            mainActivityInterface.getShowToast().doItBottomSheet(success_string,myView.getRoot());
            Log.d(TAG,"Success");
        } else {
            mainActivityInterface.getShowToast().doItBottomSheet(error_string,myView.getRoot());
        }
        if (callingFragment!=null) {
            mainActivityInterface.updateFragment(fragName, callingFragment, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activityResultLauncher.unregister();
        myView = null;
    }

    private void previewAudio(boolean doPlay) {
        // For audio files, we will attempt playback here using a mediaplayer
        // If we don't want to play, it is because we're checking the file works
        mediaPlayer = new MediaPlayer();
        if (!getLinkText().isEmpty()) {
            Uri uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(getLinkText());
            if (mainActivityInterface.getStorageAccess().uriExists(uri) && getContext()!=null) {
                try {
                    mediaPlayer.setDataSource(getContext(), uri);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(mediaPlayer -> startAudio(doPlay));
                } catch (Exception e) {
                    e.printStackTrace();
                    badLink();
                }
            } else {
                badLink();
            }
        } else {
            noLink();
        }
    }

    private void startAudio(boolean doPlay) {
        if (doPlay) {
            try {
                mediaPlayer.start();
                if (getContext()!=null) {
                    myView.openLink.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.stop));
                }
            } catch (Exception e) {
                e.printStackTrace();
                badLink();
            }
            myView.openLink.setOnClickListener(view -> {
                try {
                    mediaPlayer.stop();
                    if (getContext() != null) {
                        myView.openLink.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.play));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            // Getting this far means the audio works!
            // This was to save as as the pad link file
            doSetLinkAsPad();
        }

    }

    private void noLink () {
        // Empty - so put the cursor there to alert the user
        mainActivityInterface.getShowToast().doIt(nothing_selected_string);
        myView.linkLocation.requestFocus();
    }

    private void badLink() {
        // Link threw an error (likely invalid)
        mainActivityInterface.getShowToast().doIt(link_error_string);
        myView.linkLocation.requestFocus();
    }

    private void setLinkAsPad() {
        if (!getLinkText().isEmpty()) {
            // Check it works (without playing) and then deal with it there
            previewAudio(false);
        } else {
            noLink();
        }
    }

    private void doSetLinkAsPad() {
        // Passed all the tests, so set it to the song
        // We want to save as the audio link (which is handled in updateSong)
        // But first we also save the info to the pad link
        mainActivityInterface.getSong().setPadfile(getString(R.string.link_audio));
        // Now update the song as normal with the link location and save it
        updateSong();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mediaPlayer!=null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
