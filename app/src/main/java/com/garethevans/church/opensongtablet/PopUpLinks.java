package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;

public class PopUpLinks extends DialogFragment implements PopUpDirectoryChooserFragment.MyInterface {

    ImageButton linkYouTube_ImageButton;
    ImageButton linkWeb_ImageButton;
    ImageButton linkAudio_ImageButton;
    ImageButton linkOther_ImageButton;
    Button linkClose;
    Button linkSave;
    EditText linkYouTube_EditText;
    EditText linkWeb_EditText;
    EditText linkAudio_EditText;
    EditText linkOther_EditText;
    ImageButton linkYouTubeClear_ImageButton;
    ImageButton linkWebClear_ImageButton;
    ImageButton linkAudioClear_ImageButton;
    ImageButton linkOtherClear_ImageButton;

    static PopUpLinks newInstance() {
        PopUpLinks frag;
        frag = new PopUpLinks();
        return frag;
    }

    @Override
    public void updateCustomStorage() {
        Log.d("d","File chosen = "+ FullscreenActivity.filechosen);
    }

    @Override
    public void updateLinksPopUp() {
        // Nothing here
        Log.d("d","Nothing here");
    }

    public interface MyInterface {
        void refreshAll();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.link));

        final View V = inflater.inflate(R.layout.popup_links, container, false);

        // Initialise the views
        linkYouTube_ImageButton = (ImageButton) V.findViewById(R.id.linkYouTube_ImageButton);
        linkWeb_ImageButton = (ImageButton) V.findViewById(R.id.linkWeb_ImageButton);
        linkAudio_ImageButton = (ImageButton) V.findViewById(R.id.linkAudio_ImageButton);
        linkOther_ImageButton = (ImageButton) V.findViewById(R.id.linkOther_ImageButton);
        linkYouTube_EditText = (EditText) V.findViewById(R.id.linkYouTube_EditText);
        linkWeb_EditText = (EditText) V.findViewById(R.id.linkWeb_EditText);
        linkAudio_EditText = (EditText) V.findViewById(R.id.linkAudio_EditText);
        linkOther_EditText = (EditText) V.findViewById(R.id.linkOther_EditText);
        linkYouTubeClear_ImageButton = (ImageButton) V.findViewById(R.id.linkYouTubeClear_ImageButton);
        linkWebClear_ImageButton = (ImageButton) V.findViewById(R.id.linkWebClear_ImageButton);
        linkAudioClear_ImageButton = (ImageButton) V.findViewById(R.id.linkAudioClear_ImageButton);
        linkOtherClear_ImageButton = (ImageButton) V.findViewById(R.id.linkOtherClear_ImageButton);
        linkClose = (Button) V.findViewById(R.id.linkClose);
        linkSave = (Button) V.findViewById(R.id.linkSave);

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

        Log.d("d","filetoselect="+FullscreenActivity.filetoselect);
        if (FullscreenActivity.filechosen!=null) {
            Log.d("d", "filechosen=" + FullscreenActivity.filechosen);
        }
        // If a filetoselect has been set, add this to the view
        if (FullscreenActivity.filetoselect.equals("audiolink") && FullscreenActivity.filechosen!=null) {
            linkAudio_EditText.setText(Uri.fromFile(FullscreenActivity.filechosen).toString());
            // If this is a genuine audio file, give the user the option of setting the song duration to match this file
            MediaPlayer mediafile = new MediaPlayer();
            try {
                mediafile.setDataSource(getActivity(),Uri.parse(FullscreenActivity.filechosen.toString()));
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

        } else if (FullscreenActivity.filetoselect.equals("otherlink") && FullscreenActivity.filechosen!=null) {
            linkOther_EditText.setText(FullscreenActivity.filechosen.toString());
        }
        FullscreenActivity.filechosen = null;
        FullscreenActivity.filetoselect = "";


        linkAudio_EditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.filetoselect = "audiolink";
                DialogFragment newFragment = PopUpDirectoryChooserFragment.newInstance();
                Bundle args = new Bundle();
                args.putString("type", "file");
                newFragment.setArguments(args);
                newFragment.show(getFragmentManager(), "dialog");
                dismiss();
            }
        });
        linkOther_EditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.filetoselect = "otherlink";
                DialogFragment newFragment = PopUpDirectoryChooserFragment.newInstance();
                Bundle args = new Bundle();
                args.putString("type", "file");
                newFragment.setArguments(args);
                newFragment.show(getFragmentManager(), "dialog");
                dismiss();
            }
        });

        // Set up button actions
        linkClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        linkSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the values from the page
                FullscreenActivity.mLinkYouTube = linkYouTube_EditText.getText().toString();
                FullscreenActivity.mLinkWeb = linkWeb_EditText.getText().toString();
                FullscreenActivity.mLinkAudio = linkAudio_EditText.getText().toString();
                FullscreenActivity.mLinkOther = linkOther_EditText.getText().toString();

                // Now resave the song with these new links
                PopUpEditSongFragment.prepareSongXML();
                try {
                    PopUpEditSongFragment.justSaveSongXML();
                    mListener.refreshAll();
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(linkWeb_EditText.getText().toString())));
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
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri2 = Uri.parse(linkAudio_EditText.getText().toString());
                File getfile = new File(linkAudio_EditText.getText().toString());

                String ext = MimeTypeMap.getFileExtensionFromUrl(getfile.getName()).toLowerCase();
                if (ext.isEmpty()) {
                    ext = "";
                }
                String mimeType;
                 try {
                     mimeType = myMime.getMimeTypeFromExtension(ext);
                 } catch (Exception e ) {
                     mimeType = "*/*";
                 }

                if (mimeType == null) {
                    mimeType = "*/*";
                }

                newIntent.setDataAndType(uri2,mimeType);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(newIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        linkOther_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri2 = Uri.parse(linkOther_EditText.getText().toString());
                File getfile = new File(linkOther_EditText.getText().toString());
                String ext = MimeTypeMap.getFileExtensionFromUrl(getfile.getName()).toLowerCase();
                String mimeType = myMime.getMimeTypeFromExtension(ext);

                if (mimeType == null) {
                    mimeType = "*/*";
                }

                Log.d("d","mimeType="+mimeType);
                newIntent.setDataAndType(uri2,mimeType);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(newIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return V;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (intent!=null) {
            Uri uri = intent.getData();
            if (requestCode==0) {
                // Audio
                if (uri!=null) {
                    linkAudio_EditText.setText(uri.toString());
                }
            } else if (requestCode==1) {
                // Document
                if (uri!=null) {
                    linkOther_EditText.setText(uri.toString());
                }
            }
        }
    }

 /*   private String fileExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }*/
}