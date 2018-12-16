package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;

import static android.app.Activity.RESULT_OK;

public class PopUpLinks extends DialogFragment {

    FloatingActionButton linkYouTube_ImageButton;
    FloatingActionButton linkWeb_ImageButton;
    FloatingActionButton linkAudio_ImageButton;
    FloatingActionButton linkOther_ImageButton;
    EditText linkYouTube_EditText;
    EditText linkWeb_EditText;
    EditText linkAudio_EditText;
    EditText linkOther_EditText;
    FloatingActionButton linkYouTubeClear_ImageButton;
    FloatingActionButton linkWebClear_ImageButton;
    FloatingActionButton linkAudioClear_ImageButton;
    FloatingActionButton linkOtherClear_ImageButton;

    StorageAccess storageAccess;

    Uri uri;

    static PopUpLinks newInstance() {
        PopUpLinks frag;
        frag = new PopUpLinks();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
        void pageButtonAlpha(String s);
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
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog()==null) {
            dismiss();
        }

        storageAccess = new StorageAccess();

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mListener!=null) {
            mListener.pageButtonAlpha("links");
        }

        final View V = inflater.inflate(R.layout.popup_links, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.link));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the views
        linkYouTube_ImageButton = V.findViewById(R.id.linkYouTube_ImageButton);
        linkWeb_ImageButton = V.findViewById(R.id.linkWeb_ImageButton);
        linkAudio_ImageButton = V.findViewById(R.id.linkAudio_ImageButton);
        linkOther_ImageButton = V.findViewById(R.id.linkOther_ImageButton);
        linkYouTube_EditText = V.findViewById(R.id.linkYouTube_EditText);
        linkWeb_EditText = V.findViewById(R.id.linkWeb_EditText);
        linkAudio_EditText = V.findViewById(R.id.linkAudio_EditText);
        linkOther_EditText = V.findViewById(R.id.linkOther_EditText);
        linkYouTubeClear_ImageButton = V.findViewById(R.id.linkYouTubeClear_ImageButton);
        linkWebClear_ImageButton = V.findViewById(R.id.linkWebClear_ImageButton);
        linkAudioClear_ImageButton = V.findViewById(R.id.linkAudioClear_ImageButton);
        linkOtherClear_ImageButton = V.findViewById(R.id.linkOtherClear_ImageButton);

        // Put any links in to the text fields
        linkYouTube_EditText.setText(FullscreenActivity.mLinkYouTube);
        linkWeb_EditText.setText(FullscreenActivity.mLinkWeb);
        linkAudio_EditText.setText(FullscreenActivity.mLinkAudio);
        linkOther_EditText.setText(FullscreenActivity.mLinkOther);

        // Set listeners to clear the fields
        linkYouTubeClear_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkYouTube_EditText.setText("");
            }
        });
        linkWebClear_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkWeb_EditText.setText("");
            }
        });
        linkAudioClear_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkAudio_EditText.setText("");
            }
        });
        linkOtherClear_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkOther_EditText.setText("");
            }
        });

        // Listen for user clicking on EditText that shouldn't really be editable
        // This is because I want a file browser/picker to fill the text in
        linkAudio_EditText.setFocusable(false);
        linkAudio_EditText.setFocusableInTouchMode(false);
        linkOther_EditText.setFocusable(false);
        linkOther_EditText.setFocusableInTouchMode(false);

        linkAudio_EditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.filetoselect = "audiolink";
                FullscreenActivity.whattodo = "filechooser";
                openDocumentPicker("audio/*",0);
            }
        });
        linkOther_EditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.filetoselect = "otherlink";
                FullscreenActivity.whattodo = "filechooser";
                openDocumentPicker("*/*",1);
            }
        });

        // Set up button actions
        linkYouTube_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(linkYouTube_EditText.getText().toString())));
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                    ShowToast.showToast(getActivity());
                }
            }
        });
        linkWeb_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String weblink = linkWeb_EditText.getText().toString();
                    if (!weblink.trim().startsWith("http://") && !weblink.trim().startsWith("https://")) {
                        weblink = "http://"+weblink.trim();
                        linkWeb_EditText.setText(weblink);
                    }
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(weblink)));
                } catch (Exception e) {
                    e.printStackTrace();
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                    ShowToast.showToast(getActivity());
                }
            }
        });
        linkAudio_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mytext = linkAudio_EditText.getText().toString();
                uri = storageAccess.fixLocalisedUri(getActivity(),mytext);
                if (!mytext.equals("")) {
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    String mimeType = myMime.getMimeTypeFromExtension(mytext);
                    Intent newIntent = new Intent(Intent.ACTION_VIEW);

                    if (mimeType == null) {
                        mimeType = "*/*";
                    }

                    newIntent.setDataAndType(uri, mimeType);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    setAudioLength(uri);
                    Log.d("d","uri="+uri);
                    try {
                        startActivity(newIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                    ShowToast.showToast(getActivity());
                }
            }
        });
        linkOther_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mytext = linkOther_EditText.getText().toString();
                if (!mytext.equals("")) {
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    Intent newIntent = new Intent(Intent.ACTION_VIEW);
                    uri = storageAccess.fixLocalisedUri(getActivity(),mytext);
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
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                    ShowToast.showToast(getActivity());
                }
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void doSave() {
        // Get the values from the page
        FullscreenActivity.mLinkYouTube = linkYouTube_EditText.getText().toString();
        FullscreenActivity.mLinkWeb = linkWeb_EditText.getText().toString();
        FullscreenActivity.mLinkAudio = linkAudio_EditText.getText().toString();
        FullscreenActivity.mLinkOther = linkOther_EditText.getText().toString();

        // Now resave the song with these new links
        PopUpEditSongFragment.prepareSongXML();
        try {
            PopUpEditSongFragment.justSaveSongXML(getActivity());
            mListener.refreshAll();
            dismiss();
        } catch (Exception e) {
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.savesong) + " - " +
                    getActivity().getResources().getString(R.string.error);
            ShowToast.showToast(getActivity());
            e.printStackTrace();
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void openDocumentPicker(String mimeType, int requestCode) {
        // This uses the new Storage Access Framework to return a uri for a file of the user's choice
        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK) {
            if (resultData!=null) {
                uri = resultData.getData();
                if (uri!=null) {
                    String uriPath = uri.getPath();
                    Log.d("d", "uriPath=" + uriPath);
                    if (uriPath.contains("OpenSong/Media/")) {
                        // This will be a localised file
                        uriPath = uriPath.substring(uriPath.indexOf("OpenSong/Media/") + 15);
                        uriPath = "../OpenSong/Media/" + uriPath;
                    } else {
                        uriPath = uri.toString();
                    }

                    if (requestCode == 0) {
                        // Audio
                        linkAudio_EditText.setText(uriPath);
                        setAudioLength(uri);
                    } else if (requestCode == 1) {
                        // Document
                        linkOther_EditText.setText(uriPath);
                    }
                }
            }
        }
    }



    void setAudioLength(Uri uri) {
        // If this is a genuine audio file, give the user the option of setting the song duration to match this file
        MediaPlayer mediafile = new MediaPlayer();
        try {
            mediafile.setDataSource(getActivity(),uri);
            mediafile.prepareAsync();
            mediafile.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    FullscreenActivity.audiolength = (int) (mp.getDuration() / 1000.0f);
                    mp.release();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            linkAudio_EditText.setText("");
            FullscreenActivity.myToastMessage = getString(R.string.not_allowed);
            ShowToast.showToast(getActivity());
            mediafile.release();
        }
    }
}