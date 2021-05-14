package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.app.Activity.RESULT_OK;

public class PopUpLinks extends DialogFragment {

    private EditText linkYouTube_EditText;
    private EditText linkWeb_EditText;
    private EditText linkAudio_EditText;
    private EditText linkOther_EditText;

    private StorageAccess storageAccess;
    private Preferences preferences;

    private Uri uri;
    private MyInterface mListener;

    static PopUpLinks newInstance() {
        PopUpLinks frag;
        frag = new PopUpLinks();
        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog() == null) {
            dismiss();
        }

        storageAccess = new StorageAccess();
        preferences = new Preferences();

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mListener != null) {
            mListener.pageButtonAlpha("links");
        }

        final View V = inflater.inflate(R.layout.popup_links, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.link));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe, getContext());
            saveMe.setEnabled(false);
            doSave();
        });

        // Initialise the views
        FloatingActionButton linkYouTube_ImageButton = V.findViewById(R.id.linkYouTube_ImageButton);
        FloatingActionButton linkWeb_ImageButton = V.findViewById(R.id.linkWeb_ImageButton);
        FloatingActionButton linkAudio_ImageButton = V.findViewById(R.id.linkAudio_ImageButton);
        FloatingActionButton linkOther_ImageButton = V.findViewById(R.id.linkOther_ImageButton);
        linkYouTube_EditText = V.findViewById(R.id.linkYouTube_EditText);
        linkWeb_EditText = V.findViewById(R.id.linkWeb_EditText);
        linkAudio_EditText = V.findViewById(R.id.linkAudio_EditText);
        linkOther_EditText = V.findViewById(R.id.linkOther_EditText);
        FloatingActionButton linkYouTubeClear_ImageButton = V.findViewById(R.id.linkYouTubeClear_ImageButton);
        FloatingActionButton linkWebClear_ImageButton = V.findViewById(R.id.linkWebClear_ImageButton);
        FloatingActionButton linkAudioClear_ImageButton = V.findViewById(R.id.linkAudioClear_ImageButton);
        FloatingActionButton linkOtherClear_ImageButton = V.findViewById(R.id.linkOtherClear_ImageButton);

        // Put any links in to the text fields
        linkYouTube_EditText.setText(StaticVariables.mLinkYouTube);
        linkWeb_EditText.setText(StaticVariables.mLinkWeb);
        linkAudio_EditText.setText(StaticVariables.mLinkAudio);
        linkOther_EditText.setText(StaticVariables.mLinkOther);

        // Set listeners to clear the fields
        linkYouTubeClear_ImageButton.setOnClickListener(v -> linkYouTube_EditText.setText(""));
        linkWebClear_ImageButton.setOnClickListener(v -> linkWeb_EditText.setText(""));
        linkAudioClear_ImageButton.setOnClickListener(v -> linkAudio_EditText.setText(""));
        linkOtherClear_ImageButton.setOnClickListener(v -> linkOther_EditText.setText(""));

        // Listen for user clicking on EditText that shouldn't really be editable
        // This is because I want a file browser/picker to fill the text in
        linkAudio_EditText.setFocusable(false);
        linkAudio_EditText.setFocusableInTouchMode(false);
        linkOther_EditText.setFocusable(false);
        linkOther_EditText.setFocusableInTouchMode(false);

        linkAudio_EditText.setOnClickListener(v -> {
            FullscreenActivity.whattodo = "filechooser";
            openDocumentPicker("audio/*", StaticVariables.LINK_AUDIO);
        });
        linkOther_EditText.setOnClickListener(v -> {
            FullscreenActivity.whattodo = "filechooser";
            openDocumentPicker("*/*", StaticVariables.LINK_OTHER);
        });

        // Set up button actions
        linkYouTube_ImageButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(linkYouTube_EditText.getText().toString())));
            } catch (Exception e) {
                e.printStackTrace();
                StaticVariables.myToastMessage = getString(R.string.notset);
                ShowToast.showToast(getContext());
            }
        });
        linkWeb_ImageButton.setOnClickListener(v -> {
            try {
                String weblink = linkWeb_EditText.getText().toString();
                if (!weblink.trim().startsWith("http://") && !weblink.trim().startsWith("https://")) {
                    weblink = "http://" + weblink.trim();
                    linkWeb_EditText.setText(weblink);
                }
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(weblink)));
            } catch (Exception e) {
                e.printStackTrace();
                StaticVariables.myToastMessage = getString(R.string.notset);
                ShowToast.showToast(getContext());
            }
        });
        linkAudio_ImageButton.setOnClickListener(v -> {
            String mytext = linkAudio_EditText.getText().toString();
            uri = storageAccess.fixLocalisedUri(getContext(), preferences, mytext);
            Log.d("PopUpLinks", "uri=" + uri);
            if (!mytext.equals("")) {
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                String mimeType = myMime.getMimeTypeFromExtension(mytext);
                Intent newIntent = new Intent(Intent.ACTION_VIEW);

                if (mimeType == null) {
                    // If using a proper content uri, it will be null!
                    mimeType = "audio/*";
                }

                newIntent.setDataAndType(uri, mimeType);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                setAudioLength(uri);
                Log.d("d", "uri=" + uri);
                try {
                    startActivity(newIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                StaticVariables.myToastMessage = getString(R.string.notset);
                ShowToast.showToast(getContext());
            }
        });
        linkOther_ImageButton.setOnClickListener(v -> {
            String mytext = linkOther_EditText.getText().toString();
            if (!mytext.equals("")) {
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                uri = storageAccess.fixLocalisedUri(getContext(), preferences, mytext);
                Log.d("PopUpLinks", "uri=" + uri);
                String mimeType = myMime.getMimeTypeFromExtension(mytext);

                if (mimeType == null) {
                    mimeType = "*/*";
                }

                newIntent.setDataAndType(uri, mimeType);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(newIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                StaticVariables.myToastMessage = getString(R.string.notset);
                ShowToast.showToast(getContext());
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return V;
    }

    private void doSave() {
        // Get the values from the page
        StaticVariables.mLinkYouTube = linkYouTube_EditText.getText().toString();
        StaticVariables.mLinkWeb = linkWeb_EditText.getText().toString();
        StaticVariables.mLinkAudio = linkAudio_EditText.getText().toString();
        StaticVariables.mLinkOther = linkOther_EditText.getText().toString();

        // Now resave the song with these new links
        PopUpEditSongFragment.prepareSongXML();
        try {
            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getContext());
                NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getContext(), storageAccess, preferences, nonOpenSongSQLiteHelper.getSongId());
                nonOpenSongSQLiteHelper.updateSong(getContext(), storageAccess, preferences, nonOpenSongSQLite);
            } else {
                PopUpEditSongFragment.justSaveSongXML(getContext(), preferences);
            }
            mListener.refreshAll();
            dismiss();
        } catch (Exception e) {
            StaticVariables.myToastMessage = getString(R.string.save) + " - " + getString(R.string.error);
            ShowToast.showToast(getContext());
            e.printStackTrace();
        }
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        if (mListener != null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    private void openDocumentPicker(String mimeType, int requestCode) {
        // This uses the new Storage Access Framework to return a uri for a file of the user's choice
        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        Log.d("PopUpLinks", "opening docment picker with requestCode=" + requestCode);
        requireActivity().startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.d("PopUpLinks", "requestCode=" + requestCode);
        Log.d("PopUpLinks", "resultCode=" + resultCode);
        Log.d("PopUpLinks", "resultDate=" + resultData);

        if (resultCode == RESULT_OK) {
            if (resultData != null) {
                uri = resultData.getData();
                Log.d("PopUpLinks", "uri=" + uri);

                // Get persistent permissions
                try {
                    final int takeFlags = resultData.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // Check for the freshest data.
                    requireActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (uri != null) {
                    String uriPath = uri.getPath();
                    if (uriPath != null && uriPath.contains("OpenSong/")) {
                        // This will be a localised file
                        uriPath = storageAccess.fixUriToLocal(uri);

                    } else {
                        uriPath = uri.toString();
                    }

                    if (requestCode == 1000) {
                        // Audio
                        linkAudio_EditText.setText(uriPath);
                        setAudioLength(uri);
                    } else if (requestCode == 1001) {
                        // Document
                        linkOther_EditText.setText(uriPath);
                    }
                }
            }
        }
    }

    private void setAudioLength(Uri uri) {
        // If this is a genuine audio file, give the user the option of setting the song duration to match this file
        MediaPlayer mediafile = new MediaPlayer();
        try {
            mediafile.setDataSource(requireContext(), uri);
            mediafile.prepareAsync();
            mediafile.setOnPreparedListener(mp -> {
                StaticVariables.audiolength = (int) (mp.getDuration() / 1000.0f);
                mp.release();
            });
        } catch (Exception e) {
            e.printStackTrace();
            linkAudio_EditText.setText("");
            StaticVariables.myToastMessage = getString(R.string.not_allowed);
            ShowToast.showToast(getContext());
            mediafile.release();
        }
    }

    public interface MyInterface {
        void refreshAll();

        void pageButtonAlpha(String s);
    }
}